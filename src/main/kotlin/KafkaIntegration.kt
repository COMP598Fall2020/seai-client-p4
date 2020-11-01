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

import se4ai.group4.model.* 

val team = InetAddress.getLocalHost().hostName.substringAfterLast("-")
val teamTopic = "movielog$team"
val kafkaServer = "fall2020-comp598.cs.mcgill.ca:9092"


fun main(args: Array<String>)  {
    messages = attachToKafkaServerUsingKotkaClient()
    print(messages.first().name)
}

// Documentation: https://github.com/blueanvil/kotka
fun attachToKafkaServerUsingKotkaClient() {
    val messages = Collections.synchronizedList(ArrayList<TestMessage>())

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
        messages.add(message)
    }

    wait(15, 500, "Consumer hasn't finished") { messages.size == 1 }
    return messages
}

data class Message(val name: String, val age: Int)