import com.sun.net.httpserver.HttpServer
import kafka.filename
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.*
import kotlin.text.*

import model.CFmodel
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime

val model = CFmodel()

fun main(args: Array<String>)  {


    val port = 8082
    println("Starting server at ${InetAddress.getLocalHost().hostName}:${port}")
    var current = LocalDateTime.now()
    //train the model
    model.train()
    var fileWriter: FileWriter? = null;
    try {
        fileWriter = FileWriter("data/recommendations_" + port.toString() + "_" + current +".out", true)
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
                    val recommendations: MutableList<Int> = mutableListOf()
                    for (i in list) {
                        recommendations.add(i.second.toString().toInt())
                    }

                    // ==================
                    current = LocalDateTime.now()
                    out.print(recommendations.joinToString(","))
                    println("Recommended watchlist for user $userId at time $current: $recommendations")
                    fileWriter.append("Recommended watchlist for user $userId at time $current: $recommendations \n")
                }
            }

            start()

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
            e.printStackTrace()
        }
    }
}
