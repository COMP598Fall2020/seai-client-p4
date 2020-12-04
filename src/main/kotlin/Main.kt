import com.sun.net.httpserver.HttpServer
import model.CFmodel
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.sql.*

import kotlin.*
import kotlin.io.println


val model = CFmodel()

fun main(args: Array<String>)  {


    val port = 8082
    println("Starting server at ${InetAddress.getLocalHost().hostName}:${port}")

    //train the model
    model.initDataSet()
    model.train()

    //connect to database
    val url = "jdbc:postgresql://localhost:5432/se4ai_t4?user=postgres&password=team_jelly"
    lateinit var conn: Connection
    try {
        conn = DriverManager.getConnection(url);
    }
    catch (e: SQLException) { print(e.message) }

    // create API for monitoring
    HttpServer.create(InetSocketAddress(port), 0).apply {
        createContext("/monitoring") { http ->
            http.responseHeaders.add("Content-type", "text/html")
            http.sendResponseHeaders(200, 0)
            PrintWriter(http.responseBody).use { out ->
                println("Received monitoring request")

                // draw graph based on database data
                // first connect to database

                try {
                    val stmt: Statement = conn.createStatement()
                    val sql = "SELECT * FROM public.performance ORDER BY time"
                    val result = stmt.executeQuery(sql)

                    // process the result set
                    var rmse = mutableListOf<Float>()
                    var pre = mutableListOf<Float>()
                    var recall = mutableListOf<Float>()
                    var f1 = mutableListOf<Float>()
                    var time = mutableListOf<Timestamp>()
                    while (result.next()) {
                        rmse.add(result.getFloat(1))
                        pre.add(result.getFloat(2))
                        recall.add(result.getFloat(3))
                        f1.add(result.getFloat(4))
                        time.add(result.getTimestamp(5))
                    }

                    out.println("<!DOCTYPE HTML>\n" +
                            "<html>\n" +
                            "<head>\n" +
                            "<script>\n" +
                            "window.onload = function() {\n" +
                            "\n" +
                            "var dataPoints = [];\n" +
                            "\n" +
                            "var chart = new CanvasJS.Chart(\"chartContainer\", {\n" +
                            "\tanimationEnabled: true,\n" +
                            "\ttheme: \"light2\",\n" +
                            "\ttitle: {\n" +
                            "\t\ttext: \"Daily Sales Data\"\n" +
                            "\t},\n" +
                            "\taxisY: {\n" +
                            "\t\ttitle: \"Units\",\n" +
                            "\t\ttitleFontSize: 24,\n" +
                            "\t\tincludeZero: true\n" +
                            "\t},\n" +
                            "\tdata: [{\n" +
                            "\t\ttype: \"column\",\n" +
                            "\t\tyValueFormatString: \"#,### Units\",\n" +
                            "\t\tdataPoints: dataPoints\n" +
                            "\t}]\n" +
                            "});\n" +
                            "\n" +
                            "function addData(data) {\n" +
                            "\tfor (var i = 0; i < data.length; i++) {\n" +
                            "\t\tdataPoints.push({\n" +
                            "\t\t\tx: new Date(data[i].date),\n" +
                            "\t\t\ty: data[i].units\n" +
                            "\t\t});\n" +
                            "\t}\n" +
                            "\tchart.render();\n" +
                            "\n" +
                            "}\n" +
                            "\n" +
                            "\$.getJSON(\"https://canvasjs.com/data/gallery/javascript/daily-sales-data.json\", addData);\n" +
                            "\n" +
                            "}\n" +
                            "</script>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "<div id=\"chartContainer\" style=\"height: 370px; width: 100%;\"></div>\n" +
                            "<script src=\"https://canvasjs.com/assets/script/jquery-1.11.1.min.js\"></script>\n" +
                            "<script src=\"https://canvasjs.com/assets/script/canvasjs.min.js\"></script>\n" +
                            "</body>\n" +
                            "</html>")

                }
                catch (e: SQLException) {
                    out.println("Fail to query, " + e.message)
                }
            }
        }
    }

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

                // each row has "rating", "movieId", "movieName"
                // recommendations.map(it -> String.toDouble(it.second))
                val recommendations : MutableList<Int> = mutableListOf()
                val ratings : MutableList<Int> = mutableListOf()
                for (i in list) {
                    recommendations.add(i.second.toString().toInt())
                    ratings.add(i.first.toString().toInt())
                }

                // add to database
                val stmt = conn.prepareStatement(
                    """
                        INSERT INTO public.recommendations 
                        (uid, movie_list, ranking_score, recommend_time) 
                        VALUES (?,?,?,?)
                    """.trimIndent()
                )
                val arr1 : java.sql.Array = conn.createArrayOf("VARCHAR", recommendations.toTypedArray())
                val arr2 : java.sql.Array = conn.createArrayOf("VARCHAR", ratings.toTypedArray())
                stmt.setInt(1, userId.toString().toInt())
                stmt.setArray(2, arr1)
                stmt.setArray(3, arr2)
                val timestamp : Timestamp = Timestamp(System.currentTimeMillis())
                stmt.setTimestamp(4, timestamp)

                try {
                    val success = stmt.executeUpdate()
                } catch (e: SQLException) { }

                out.println(recommendations.toList().joinToString(","))
                println("Recommended watchlist for user $userId: $recommendations")
            }
        }

        start()
        conn.close()
    }
}
