import com.sun.net.httpserver.HttpServer
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.*
import kotlin.text.*

import model.CFmodel


val model = CFmodel()

fun main(args: Array<String>)  {


    val port = 8082
    println("Starting server at ${InetAddress.getLocalHost().hostName}:${port}")

    //train the model
    model.train()

    HttpServer.create(InetSocketAddress(port), 0).apply {
        createContext("/recommend") { http ->
            http.responseHeaders.add("Content-type", "text/plain")
            http.sendResponseHeaders(200, 0)
            PrintWriter(http.responseBody).use { out ->
                val userId = http.requestURI.path.substringAfterLast("/")
                println("Received recommendation request for user $userId")

                // ==================
                // YOUR CODE GOES HERE
                //val recommendations = listOf(20,22,23)

                val list = model.predict(userId, 20, false)  
                //println(list)

                // // each row has "rating", "movieId", "movieName"
                // recommendations.map(it -> String.toDouble(it.second))
                val recommendations : MutableList<Int> = mutableListOf()
                for (i in list) {
                    recommendations.add(i.second.toString().toInt())
                }

                // ==================

                out.print(recommendations.joinToString(","))
                println("Recommended watchlist for user $userId: $recommendations")
            }
        }

        start()
    }
}
