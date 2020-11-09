import com.blueanvil.kotka.Kotka
import com.blueanvil.kotka.KotkaConfig
import com.sun.net.httpserver.HttpServer
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration
import org.apache.kafka.streams.KafkaStreams

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.kstream.*
import java.util.*
import kotlin.*
import kotlin.text.*

import java.io.FileWriter
import java.io.IOException

import se4ai.group4.model.*

//val team = InetAddress.getLocalHost().hostName.substringAfterLast("-")
val teamTopic = "movielog4"
val kafkaServer = "fall2020-comp598.cs.mcgill.ca:9092"

fun main(args: Array<String>)  {
    val messages = attachToKafkaServerUsingKotkaClient()

    val CSV_HEADER = "userID, movie, rating"
    var fileWriter: FileWriter? = null;    try {

        fileWriter = FileWriter("ratings.csv")
        fileWriter.append(CSV_HEADER)
        fileWriter.append('\n')

        for (message in messages) {
            println(message.name)
            val info = message.name.split(",| |/|=|\\n".toRegex());
            //# info[4] is user id\n",
            //# info[7] is rate or data\n",
            //# info[8] is movie title if rate, otherwise if data is info[9]\n",
            //# info[9] is rating if rate\n",

            if (info[7] == "rate"){
                fileWriter.append(info[4])
                fileWriter.append(',')
                fileWriter.append(info[8])
                fileWriter.append(',')
                fileWriter.append(info[9])
                fileWriter.append("\n")
            }
        }

        println("Write CSV successfully!" )

    } catch (e: Exception) {
        println("Writing CSV error!")
        e.printStackTrace()
    } finally {
        try {
            fileWriter!!.flush()
            fileWriter.close()
        } catch (e: IOException) {
            println("Flushing/closing error!")
            e.printStackTrace()
        }
    }
}



// Documentation: https://github.com/blueanvil/kotka
fun attachToKafkaServerUsingKotkaClient(): List<Message> {
    val messages = Collections.synchronizedList(ArrayList<Message>())

    val kafka = Kotka(
        kafkaServers = kafkaServer, config = KotkaConfig(
            partitionCount = 2,
            replicationFactor = 1,
            consumerProps = mapOf("max.poll.records" to "1", "auto.offset.reset" to "earliest").toProperties(),
            producerProps = mapOf("batch.size" to "1").toProperties(),
            pollTimeout = Duration.ofMillis(100)
        )
    )
    kafka.consumer(topic = teamTopic, threads = 2, messageClass = Message::class, pubSub = true) { message ->
        messages.add(message)
    }

    //wait(15, 500, "Consumer hasn't finished") { messages.size == 1 }
    while (messages.size < 5){
    }
    return messages
}

data class Message(val name: String, val age: Int)