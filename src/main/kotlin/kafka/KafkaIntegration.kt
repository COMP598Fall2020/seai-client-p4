package kafka

import com.blueanvil.kotka.Consumer
import com.blueanvil.kotka.Kotka
import com.blueanvil.kotka.KotkaConfig
import com.blueanvil.kotka.Producer
import krangl.DataFrame
import krangl.readCSV
import java.time.Duration
import org.apache.kafka.clients.consumer.ConsumerConfig
import java.util.*
import kotlin.*
import kotlin.text.*
import java.io.FileWriter
import java.io.IOException
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import java.io.BufferedReader
import java.io.FileReader
import java.util.ArrayList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.File

//val team = InetAddress.getLocalHost().hostName.substringAfterLast("-")
val teamTopic = "movielog4"
val kafkaServer = "fall2020-comp598.cs.mcgill.ca:9092"
val messages = Collections.synchronizedList(ArrayList<List<String>>())
val batch_size = 5000
val formatter = DateTimeFormatter.BASIC_ISO_DATE
val current = LocalDateTime.now().format(formatter)
//val current = "20201103" (can be used to set maximum date)
var pointer_date = "0"

fun main(args: Array<String>)  {


    var fileWriter: FileWriter? = null;    try {

        if (File("data/ratings_$current.csv").exists()){
            fileWriter = FileWriter("data/ratings_$current.csv", true)
        } else {
            fileWriter = FileWriter("data/ratings_$current.csv", true)
            fileWriter.append("userID, movieID, movieTitle, rating")
            fileWriter.append('\n')
        }


        attachToKafkaServerUsingKotkaClient()

        while (messages.size < batch_size && pointer_date.toInt() <= current.toInt()) {
        }

        println("Writing to file...")
        for (info in messages) {
            // info[0] is time
            // info[1] is userid,
            // info[2] is 'GET'
            // info[3] is ''
            // info[4] is 'data' or 'rate'

            //if data
            // info[5] is 'm'
            // info[6] is movie title
            // info[7] is timepoint

            //if rate
            // info[5] is movie title
            // info[6] is rating from 1 to 5

            if (info[4] == "rate") {
                fileWriter.append(info[1])
                fileWriter.append(',')
                fileWriter.append(getMovieID(info[5]))
                fileWriter.append(',')
                fileWriter.append(info[5])
                fileWriter.append(',')
                fileWriter.append(info[6])

                fileWriter.append("\n")
            }
        }

    } catch (e: Exception) {
        println("Error!")
        e.printStackTrace()
    } finally {
        try {
            fileWriter!!.flush()
            fileWriter.close()
        } catch (e: IOException) {
            println("Flushing/closing error!")
            e.printStackTrace() }
    }
}

fun getMovieID(title: String): String{
    var fileReader: BufferedReader? = null
    try {
        var line: String?
        fileReader = BufferedReader(FileReader("data/movies.csv"))
        // Read CSV header
        fileReader.readLine()
        // Read the file line by line starting from the second line
        line = fileReader.readLine()
        while (line != null) {
            val tokens = line.split(",")
            if (tokens.size > 0) {
                if (tokens[1] == title){
                    return tokens[0]
                }
            }
            line = fileReader.readLine()
        }
    } catch (e: Exception) {
        println("Reading CSV Error!")
        e.printStackTrace()
    } finally {
        try {
            fileReader!!.close()
        } catch (e: IOException) {
            println("Closing fileReader Error!")
            e.printStackTrace()
        }
    }
    return "-1"
}

// Documentation: https://github.com/blueanvil/kotka
fun attachToKafkaServerUsingKotkaClient() {
    val kafka = CustomKotka(
        kafkaServers = kafkaServer, config = KotkaConfig(
            partitionCount = 2,
            replicationFactor = 1,
            consumerProps = mapOf("max.poll.records" to "1", "auto.offset.reset" to "earliest").toProperties(),
            producerProps = mapOf("batch.size" to "1").toProperties(),
            pollTimeout = Duration.ofMillis(100)
        )
    )
    kafka.consumer(topic = teamTopic, threads = 2, messageClass = Message::class, pubSub = false) { message ->
    }
}

data class Message(val name: String, val age: Int)

class CustomKotka(private val kafkaServers: String,
            private val config: KotkaConfig) {

    private val producer = Producer(kafkaServers, config.objectMapper, config.producerProps)
    private val consumers = ConcurrentHashMap<String, CustomConsumer>()

    fun <T : Any> send(topic: String, message: T) {
        producer.send(topic, message)
    }

    fun <T : Any> consumer(topic: String,
                           threads: Int,
                           messageClass: KClass<T>,
                           pubSub: Boolean = false,
                           messageHandler: (T) -> Unit) {
        createTopic(topic)
        val consumer = consumers.getOrPut(topic) {
            CustomConsumer(kafkaServers = kafkaServers,
                    topic = topic,
                    threads = threads,
                    pubSub = pubSub,
                    config = config)
        }
        consumer.addHandler(messageClass, messageHandler)
    }

    private fun createTopic(topic: String) {
        val properties = Properties()
        properties.setProperty("bootstrap.servers", kafkaServers)
        val client = AdminClient.create(properties)

        if (client.listTopics().names().get().contains(topic)) {
            log.info("Topic $topic already exists. Skipping")
        } else {
            client.createTopics(listOf(NewTopic(topic, config.partitionCount, config.replicationFactor))).values()[topic]!!.get()
            log.info("Created topic $topic")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Kotka::class.java)
    }
}

class CustomConsumer(private val kafkaServers: String,
               private val topic: String,
               private val threads: Int,
               private val pubSub: Boolean = false,
               private val config: KotkaConfig) {

    private val handlers = ConcurrentHashMap<String, MessageHandler<*>>()

    @Volatile
    private var stopped: Boolean = false

    init {
        val threadCount = AtomicInteger(1)
        val threadPool = Executors.newFixedThreadPool(threads) { runnable ->
            val thread = Thread(Thread.currentThread().threadGroup, runnable, "kotka.$topic.${threadCount.getAndIncrement()}", 0)
            thread.isDaemon = true
            thread
        }

        val futures = ArrayList<Future<*>>()

        repeat(threads) {
            val groupId = if (pubSub) "$topic.${uuid()}" else "$topic.competing-consumer"
            val allProps = allProps(groupId)

            val kafkaConsumer = KafkaConsumer<String, String>(allProps)
            kafkaConsumer.subscribe(listOf(topic))
            val future = threadPool.submit {
                runConsumer(kafkaConsumer, groupId)
            }
            futures.add(future)
        }

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                stopped = true
                threadPool.shutdown()
                threadPool.awaitTermination(30, TimeUnit.SECONDS)
            }
        })
    }

    fun <T : Any> addHandler(messageClass: KClass<T>, handlerFunction: (T) -> Unit) {
        handlers[messageClass.qualifiedName!!] = MessageHandler(messageClass, handlerFunction)
    }

    private fun allProps(groupId: String): Properties {
        val allProps = Properties()
        if (config.consumerProps != null) {
            allProps.putAll(config.consumerProps!!)
        }
        allProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaServers
        allProps[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        allProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        allProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        allProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
        allProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        return allProps
    }

    private fun runConsumer(kafkaConsumer: KafkaConsumer<String, String>, groupId: String) {
        log.info("Running consumer topic=$topic, threads=$threads, pubSub=$pubSub, groupId=$groupId")
        while (!stopped) {
            val records = kafkaConsumer.poll(config.pollTimeout)
            log.trace("($topic/$groupId) Received ${records.count()} messages after poll")
            processRecords(records)
        }

        kafkaConsumer.unsubscribe()
        kafkaConsumer.close()
        log.info("Stopped consumer for topic $topic and group $groupId")
    }
    private fun processRecords(records: ConsumerRecords<String, String>) {
        for (record in records) {
            if (stopped || messages.size >= batch_size || pointer_date.toInt() > current.toInt()) {
                break
            }
            val msgStr = record.value()
            val info = msgStr.split("[, /=\\n]".toRegex())
            pointer_date = info[0].substring(0, 4) + info[0].substring(5, 7) + info[0].substring(8, 10)
            if (info[4] == "rate") {
                messages.add(info)
                println(info)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Consumer::class.java)
    }
}

fun uuid(): String = UUID.randomUUID().toString().toLowerCase().replace("-", "")

data class MessageHandler<T : Any>(val messageClass: KClass<T>,
                                   val handlerFunction: (T) -> Unit) {

    fun invoke(config: KotkaConfig, messageStr: String) {
        val message = config.objectMapper.readValue(messageStr, messageClass.javaObjectType)
        handlerFunction(message)
    }
}