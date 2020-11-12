package kafka
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

internal class KafkaIntegrationTest {

    @Test
    @DisplayName("Should return 1")
    fun getMovieID() {
        val kafkaIntegration = KafkaIntegration()
        val movieTitle = "toy+story+1995"
        assertTrue(kafkaIntegration.getMovieID(movieTitle) == "1")
    }
    @Test
    @DisplayName("Should return -1")
    fun testFalseMovie() {
        val kafkaIntegration = KafkaIntegration()
        val movieTitle = "fbshbdfuavsdfsfsasdffhdsg"
        assertTrue(kafkaIntegration.getMovieID(movieTitle) == "-1")
    }

    @Test
    @DisplayName("Should return true")
    fun attachToKafkaServerUsingKotkaClient() {
        val kafkaIntegration = KafkaIntegration()
        assertTrue(kafkaIntegration.attachToKafkaServerUsingKotkaClient(), "Should return 'true'")
    }
}