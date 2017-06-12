package uk.ashleybye.services

import com.github.kittinunf.fuel.httpGet
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import uk.ashleybye.models.SunInfo
import java.nio.charset.Charset
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Service for obtaining sun information.
 *
 * Obtains sunrise and sunset times for a given latitude and longitude.
 *
 * @constructor creates an instance of the service
 */
class SunService {

    /**
     * Gets sunrise and sunset times.
     *
     * Obtains the sunrise and sunset times for the given latitude and longitude.
     *
     * @param lat - the latitude
     * @param lon - the longitude
     * @return nl.komponents.kovenant.Promise<SunInfo, Exception>
     */
    fun getSunInfo(lat: Double, lon: Double): Promise<SunInfo, Exception> = task {
        val url = "http://api.sunrise-sunset.org/json?lat=$lat&lng=$lon&formatted=0"
        val (request, response, result) = url.httpGet().responseString()

        val jsonString = String(response.data, Charset.forName("UTF-8"))
        val json = JsonParser().parse(jsonString).obj

        val sunrise = json["results"]["sunrise"].string
        val sunset = json["results"]["sunset"].string

        /* This uses the Joda Time library; commented out in favour of Java 8 API.
//            val sunriseTime = DateTime.parse(sunrise)
//            val sunsetTime = DateTime.parse(sunset)
//            val formatter = DateTimeFormat
//                    .forPattern("HH:mm:ss")
//                    .withZone(DateTimeZone
//                            .forID("Australia/Sydney"))
//            SunInfo(formatter.print(sunriseTime), formatter.print(sunsetTime))
*/
        val sunriseTime = ZonedDateTime.parse(sunrise)
        val sunsetTime = ZonedDateTime.parse(sunset)
        val formatter = DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.of("Australia/Sydney"))

        SunInfo(sunriseTime.format(formatter), sunsetTime.format(formatter))
    }
}
