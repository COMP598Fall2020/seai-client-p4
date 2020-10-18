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

val team = InetAddress.getLocalHost().hostName.substringAfterLast("-")
val teamTopic = "movielog$team"
val kafkaServer = "fall2020-comp598.cs.mcgill.ca:9092"

fun main(args: Array<String>)  {
//    attachToKafkaServerUsingKotkaClient()
    attachToKafkaServerUsingDefaultClient()

    val port = 8082
    println("Starting server at ${InetAddress.getLocalHost().hostName}:${port}")

    HttpServer.create(InetSocketAddress(port), 0).apply {
        createContext("/recommend") { http ->
            http.responseHeaders.add("Content-type", "text/plain")
            http.sendResponseHeaders(200, 0)
            PrintWriter(http.responseBody).use { out ->
                val userId = http.requestURI.path.substringAfterLast("/")
                println("Received recommendation request for user $userId")

                // ==================
                // YOUR CODE GOES HERE

                val recommendations = (0..20).toList().joinToString(",")

                // ==================

                out.println(recommendations)
                println("Recommended watchlist for user $userId: $recommendations")
            }
        }

        start()
    }
}

// Documentation: https://kafka.apache.org/documentation/streams/
fun attachToKafkaServerUsingDefaultClient() {
    val props = Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "seai-application")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer)
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)

    // The following line resets the application to reprocess the data from the start
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val builder = StreamsBuilder()
    val textLines = builder.stream<String, String>("movielog4")
    textLines.print(Printed.toSysOut())

    val streams = KafkaStreams(builder.build(), props)
    streams.cleanUp();
    streams.start()
}

// Documentation: https://github.com/blueanvil/kotka
fun attachToKafkaServerUsingKotkaClient() {
    val kafka = Kotka(
        kafkaServers = kafkaServer, config = KotkaConfig(
            partitionCount = 2,
            replicationFactor = 1,
            consumerProps = mapOf("max.poll.records" to "1").toProperties(),
            producerProps = mapOf("batch.size" to "1").toProperties(),
            pollTimeout = Duration.ofMillis(100)
        )
    )

    kafka.consumer(topic = teamTopic, threads = 2, messageClass = Message::class) { message ->
        // YOUR CODE GOES HERE
    }
}

data class Message(val name: String, val age: Int)