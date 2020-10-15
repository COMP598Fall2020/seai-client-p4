import com.blueanvil.kotka.Kotka
import com.blueanvil.kotka.KotkaConfig
import com.sun.net.httpserver.HttpServer
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration

fun main(args: Array<String>) {
    attachToKafkaServer()

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

fun attachToKafkaServer() {
    val kafka = Kotka(
        kafkaServers = "fall2020-comp598.cs.mcgill.ca:9092", config = KotkaConfig(
            partitionCount = 2,
            replicationFactor = 1,
            consumerProps = mapOf("max.poll.records" to "1").toProperties(),
            producerProps = mapOf("batch.size" to "1").toProperties(),
            pollTimeout = Duration.ofMillis(100)
        )
    )

    val team = InetAddress.getLocalHost().hostName.substringAfterLast("-")
    kafka.consumer(topic = "movielog$team", threads = 2, messageClass = Message::class) { message ->
        println(message.name)
    }
}

data class Message(val name: String, val age: Int)