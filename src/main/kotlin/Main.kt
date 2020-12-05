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

    // create API for monitoring
    HttpServer.create(InetSocketAddress(port), 0).apply {
        createContext("/monitoring") { http ->
            http.responseHeaders.add("Content-type", "text/html")
            http.sendResponseHeaders(200, 0)
            PrintWriter(http.responseBody).use { out ->
                println("Received monitoring request")

                // draw graph based on database data
                // first connect to database

                //connect to database
                val url = "jdbc:postgresql://localhost:5432/se4ai_t4?user=postgres&password=team_jelly"
                lateinit var conn: Connection
                try {
                    conn = DriverManager.getConnection(url);
                }
                catch (e: SQLException) { println(e.message) }

                try {
                    val stmt: Statement = conn.createStatement()
                    val sql = "SELECT * FROM public.performance ORDER BY time"
                    val result = stmt.executeQuery(sql)

                    // process the result set
                    var rmse = mutableListOf<Float>()
                    var pre = mutableListOf<Float>()
                    var recall = mutableListOf<Float>()
                    var f1 = mutableListOf<Float>()
                    var time = mutableListOf<String>()
                    while (result.next()) {
                        rmse.add(result.getFloat(1))
                        pre.add(result.getFloat(2))
                        recall.add(result.getFloat(3))
                        f1.add(result.getFloat(4))
                        time.add(result.getString(5))
                    }
                    conn.close()

                    out.println(plotPage(rmse, pre, recall, f1, time))

                }
                catch (e: SQLException) {
                    out.println("Fail to query, " + e.message)
                }
            }
        }

        // createContext("/recommend") { http ->
        //     http.responseHeaders.add("Content-type", "text/plain")
        //     http.sendResponseHeaders(200, 0)
        //     PrintWriter(http.responseBody).use { out ->
        //         val userId = http.requestURI.path.substringAfterLast("/")
        //         println("Received recommendation request for user $userId")

        //         // ==================
        //         // YOUR CODE GOES HERE
        //         //val recommendations = listOf(20,22,23)
        //         //connect to database
        //         val url = "jdbc:postgresql://localhost:5432/se4ai_t4?user=postgres&password=team_jelly"
        //         try {
        //             val conn = DriverManager.getConnection(url);
        //             println("connecting to database...")

        //             val list = model.predict(userId, 20, false)
        //             //println(list)

        //             // each row has "rating", "movieId", "movieName"
        //             // recommendations.map(it -> String.toDouble(it.second))
        //             val recommendations : MutableList<Int> = mutableListOf()
        //             val ratings : MutableList<Double> = mutableListOf()
        //             for (i in list) {
        //                 recommendations.add(i.second.toString().toInt())
        //                 ratings.add(String.format("%.3f", i.first.toString().toDouble()).toDouble())
        //             }
        //             out.print(recommendations.toList().joinToString(","))
        //             println("Recommended watchlist for user $userId: $recommendations")

        //             // add to database
        //             //println(list)
        //             val stmt = conn.prepareStatement(
        //                 """
        //                     INSERT INTO public.recommendations 
        //                     (uid, recommendations, score, recommend_time) 
        //                     VALUES (?,?,?,?)
        //                 """.trimIndent()
        //             )
                    
        //             val arr1 : java.sql.Array = conn.createArrayOf("INT", recommendations.toTypedArray())
        //             val arr2 : java.sql.Array = conn.createArrayOf("VARCHAR", ratings.toTypedArray())
        //             stmt.setInt(1, userId.toString().toInt())
        //             stmt.setArray(2, arr1)
        //             stmt.setArray(3, arr2)
        //             val timestamp : Timestamp = Timestamp(System.currentTimeMillis())
        //             stmt.setTimestamp(4, timestamp)

        //             try {
        //                 val success = stmt.executeUpdate()
        //             } catch (e: SQLException) { println(e.message) }
        //             conn.close()
        //         }
        //         catch (e: SQLException) { println(e.message) }
        //     }
        // }

        start()

    }
}

fun plotPage(rmse:List<Float>, pre:List<Float>, recall:List<Float>, f1:List<Float>, time:List<String>) : String {
    var rmse_data : String = ""
    var pre_data : String = ""
    var recall_data : String = ""
    var f1_data : String = ""

    for (i in 0..time.size) {
        val list = time[i].substringBefore(" ").split('-')
        println(list)
        rmse_data += "\t\t\t{ x: new Date(" + list[0] + "," + list[1] + "," + list[2] + "), y: " + rmse[i] + " },\n"
        pre_data += "\t\t\t{ x: new Date(" + list[0] + "," + list[1] + "," + list[2] + "), y: " + pre[i] + " },\n"
        recall_data += "\t\t\t{ x: new Date(" + list[0] + "," + list[1] + "," + list[2] + "), y: " + recall[i] + " },\n"
        f1_data += "\t\t\t{ x: new Date(" + list[0] + "," + list[1] + "," + list[2] + "), y: " + f1[i] + " },\n"
    }

    return "<!DOCTYPE HTML>\n" +
            "<html>\n" +
            "<head>  \n" +
            "<script>\n" +
            "window.onload = function () {\n" +
            "\n" +
            "var chart = new CanvasJS.Chart(\"chartContainer\", {\n" +
            "\tanimationEnabled: true,\n" +
            "\ttitle:{\n" +
            "\t\ttext: \"Model performance over time\"\n" +
            "\t},\n" +
            "\taxisX: {\n" +
            "\t\tvalueFormatString: \"DD MMM,YY\"\n" +
            "\t},\n" +
            "\taxisY: {\n" +
            "\t\ttitle: \"Metric score\",\n" +
            "\t},\n" +
            "\tlegend:{\n" +
            "\t\tcursor: \"pointer\",\n" +
            "\t\tfontSize: 16,\n" +
            "\t\titemclick: toggleDataSeries\n" +
            "\t},\n" +
            "\ttoolTip:{\n" +
            "\t\tshared: true\n" +
            "\t},\n" +
            "\tdata: [{\n" +
            "\t\tname: \"RMSE\",\n" +
            "\t\ttype: \"spline\",\n" +
            "\t\tshowInLegend: true,\n" +
            "\t\tdataPoints: [\n" +
            rmse_data +
            "\t\t]\n" +
            "\t},\n" +
            "\t{\n" +
            "\t\tname: \"Precision\",\n" +
            "\t\ttype: \"spline\",\n" +
            "\t\tshowInLegend: true,\n" +
            "\t\tdataPoints: [\n" +
            pre_data +
            "\t\t]\n" +
            "\t},\n" +
            "\t{\n" +
            "\t\tname: \"Recall\",\n" +
            "\t\ttype: \"spline\",\n" +
            "\t\tshowInLegend: true,\n" +
            "\t\tdataPoints: [\n" +
            recall_data +
            "\t\t]\n" +
            "\t},\n" +
            "\t{\n" +
            "\t\tname: \"F1\",\n" +
            "\t\ttype: \"spline\",\n" +
            "\t\tshowInLegend: true,\n" +
            "\t\tdataPoints: [\n" +
            f1_data +
            "\t\t]\n" +
            "\t}]\n" +
            "});\n" +
            "chart.render();\n" +
            "\n" +
            "function toggleDataSeries(e){\n" +
            "\tif (typeof(e.dataSeries.visible) === \"undefined\" || e.dataSeries.visible) {\n" +
            "\t\te.dataSeries.visible = false;\n" +
            "\t}\n" +
            "\telse{\n" +
            "\t\te.dataSeries.visible = true;\n" +
            "\t}\n" +
            "\tchart.render();\n" +
            "}\n" +
            "\n" +
            "}\n" +
            "</script>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div id=\"chartContainer\" style=\"height: 370px; width: 100%;\"></div>\n" +
            "<script src=\"https://canvasjs.com/assets/script/canvasjs.min.js\"></script>\n" +
            "</body>\n" +
            "</html>"

}