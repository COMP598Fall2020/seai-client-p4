package database

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.lang.IllegalStateException
import java.sql.*

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

    @Test
    @DisplayName("database connection test")
    fun databaseConnectionTest() {
        println("start database connection test...")
        val url = "jdbc:postgresql://localhost:5432/se4ai_t4?user=postgres&password=team_jelly"
        try {
            val conn:Connection = DriverManager.getConnection(url);
            conn.close()
        }
        catch (e:SQLException) {
            fail<Any?>(e.message + ", " + e.errorCode)
        }
    }

    @Test
    @DisplayName("database insertion test")
    fun databaseInsertionTest() {
        println("start database insertion test...")
        try {
            val url = "jdbc:postgresql://localhost:5432/se4ai_t4?user=postgres&password=team_jelly"
            val conn:Connection = DriverManager.getConnection(url);
            val stmt:Statement = conn.createStatement()
            val sql = "INSERT INTO public.movies (name, id_long) VALUES ('helloworld', 1)"

            try {
                val success = stmt.executeUpdate(sql)
            }
            catch (e:SQLException) {
                fail<Any?>(e.message + ", " + e.errorCode)
            }
        }
        catch (e:SQLException) {
            fail<Any?>(e.message + ", " + e.errorCode)
        }

    }

    @Test
    @DisplayName("database query test")
    fun databaseQueryTest() {
        println("start database query test...")
        try {
            val url = "jdbc:postgresql://localhost:5432/se4ai_t4?user=postgres&password=team_jelly"
            val conn:Connection = DriverManager.getConnection(url);
            val stmt:Statement = conn.createStatement()
            val sql = "SELECT * FROM public.movies"
            try {
                val result:ResultSet = stmt.executeQuery(sql)
                while (result.next()) {
                    val name = result.getString(result.findColumn("name"))
                    val id = result.getInt(result.findColumn("id_long"))
                    println("$name, $id")
                    result.next()
                }
            }
            catch (e:SQLException) { fail<Any?>(e.message + ", " + e.errorCode) }
            finally {
                stmt.close()
                conn.close()
            }

        }
        catch (e:SQLException) {
            fail<Any?>(e.message + ", " + e.errorCode)
        }
    }
}