import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import io.kotlintest.Duration
import io.kotlintest.eventually
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import uk.ashleybye.services.SunService
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Client implementation for Fuel which removes need to query remote servers.
 */
class FuelTestClient(val testResponse: Response) : Client {

    override fun executeRequest(request: Request): Response {
        return testResponse
    }
}

class ApplicationSpec : StringSpec() {
    init {
        "DateTimeFormatter must return 1970 as the beginning of epoch" {
            val beginning = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneId.systemDefault())
            val formattedYear = beginning.format(DateTimeFormatter.ofPattern("yyyy"))
            formattedYear shouldBe "1970"
        }

        "SunService must receive correct sunset and sunrise information" {
            val json = """{
                    "results":{
                        "sunrise":"2017-06-12T20:57:17+00:00",
                        "sunset":"2017-06-12T06:52:49+00:00"
                    }
                }"""

            val testResponse = Response()
            testResponse.data = json.toByteArray()

            val testClient = FuelTestClient(testResponse)
            FuelManager.instance.client = testClient

            val lat = -33.8830
            val lon = 151.2167
            val sunService = SunService()
            val resultPromise = sunService.getSunInfo(lat, lon)

            eventually(Duration(5, TimeUnit.SECONDS)) {
                val sunInfo = resultPromise.get()
                sunInfo.sunrise shouldBe "06:57:17"
                sunInfo.sunset shouldBe "16:52:49"
            }
        }
    }
}