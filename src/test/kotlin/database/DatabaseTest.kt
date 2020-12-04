package database

import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.sql.*
import java.sql.Array
import java.text.SimpleDateFormat

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseTest {

//    @AfterAll
//    fun cleanup() {
//        val url = "jdbc:postgresql://localhost:5432/se4ai_t4?user=postgres&password=team_jelly"
//        val conn:Connection = DriverManager.getConnection(url);
//        val stmt:Statement = conn.createStatement()
//        val sql = "Delete FROM public.movies"
//        try {
//            stmt.executeUpdate(sql)
//        }
//        catch (e:SQLException) {
//            println(e.message + ", " + e.errorCode)
//        }
//        finally {
//            stmt.close()
//            conn.close()
//        }
//    }
//    var sdf: SimpleDateFormat? = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
//
//    @Test
//    @DisplayName("database connection test")
//    fun databaseConnectionTest() {
//        println("start database connection test...")
//        val url = "jdbc:postgresql://localhost:5432/se4ai_t4?user=postgres&password=team_jelly"
//        try {
//            val conn:Connection = DriverManager.getConnection(url);
//            conn.close()
//        }
//        catch (e:SQLException) {
//            fail<Any?>(e.message + ", " + e.errorCode)
//        }
//    }
//
//    @Test
//    @DisplayName("database insertion test")
//    fun databaseInsertionTest() {
//        println("start database insertion test...")
//        try {
//            val url = "jdbc:postgresql://localhost:5432/se4ai_t4?user=postgres&password=team_jelly"
//            val userId = 1
//            val recommendations = arrayOf(1, 2, 3)
//            val ratings = arrayOf(3.0F, 3.0F, 3.0F)
//            val conn:Connection = DriverManager.getConnection(url);
//            conn.use {
//                val stmt = it.prepareStatement(
//                        """
//                            INSERT INTO public.recommendations
//                            (uid, movie_list, ranking_score, recommend_time)
//                            VALUES (?,?,?,?)
//                        """.trimIndent()
//                )
//                val arr1 : Array = it.createArrayOf("VARCHAR", recommendations)
//                val arr2 : Array = it.createArrayOf("VARCHAR", ratings)
//                stmt.setInt(1, userId)
//                stmt.setArray(2, arr1)
//                stmt.setArray(3, arr2)
//                val timestamp : Timestamp = Timestamp(System.currentTimeMillis())
//                stmt.setTimestamp(4, timestamp)
//
//                try {
//                    val success = stmt.executeUpdate()
//                } catch (e: SQLException) {
//                    fail<Any?>(e.message + ", " + e.errorCode)
//                }
//
//                stmt.executeUpdate("DELETE FROM public.recommendations")
//                stmt.close()
//            }
//        }
//        catch (e:SQLException) {
//            fail<Any?>(e.message + ", " + e.errorCode)
//        }
//
//    }
//
//    @Test
//    @DisplayName("database query test")
//    fun databaseQueryTest() {
//        println("start database query test...")
//        try {
//            val url = "jdbc:postgresql://localhost:5432/se4ai_t4?user=postgres&password=team_jelly"
//            val conn:Connection = DriverManager.getConnection(url);
//            val stmt:Statement = conn.createStatement()
//            val sql = "SELECT * FROM public.movies"
//            try {
//                val result:ResultSet = stmt.executeQuery(sql)
//                while (result.next()) {
//                    val name = result.getString(result.findColumn("name"))
//                    val id = result.getInt(result.findColumn("id_long"))
//                    println("$name, $id")
//                    result.next()
//                }
//            }
//            catch (e:SQLException) { fail<Any?>(e.message + ", " + e.errorCode) }
//            finally {
//                stmt.close()
//                conn.close()
//            }
//
//        }
//        catch (e:SQLException) {
//            fail<Any?>(e.message + ", " + e.errorCode)
//        }
//    }
}