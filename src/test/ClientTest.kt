import org.junit.After
import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClientTest {
    val endpoint = "http://localhost:8082/recommend"
    val reccomendationService = startRecommendationService()

    @Test
    fun testRecommendationServiceReplies() =
        assertTrue("No reply received...") {
            URL("$endpoint/1").readText().isNotEmpty()
        }

    @Test
    fun testRecommendationServiceReturns20Movies() {
        val movies = URL("$endpoint/1").readText().split(",")

        assertEquals(20, movies.size)
    }

    @After
    fun shutdown() = reccomendationService.stop(0)
}