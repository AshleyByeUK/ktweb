package uk.ashleybye.services

import com.github.kittinunf.fuel.httpGet
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import java.nio.charset.Charset

/**
 * Service for obtaining weather information.
 *
 * Gets the current temperature at a given latitude and longitude.
 *
 * @constructor create an instance of the service
 */
class WeatherService(private val openWeatherMapAppId: String) {

    /**
     * Gets the current temperature.
     *
     * Gets the current temperature for the given latitude and longitude.
     *
     * @param lat - the latitude
     * @param lon - the longitude
     * @return nl.komponents.kovenant.Promise<Double, Exception>
     */
    fun getTemperature(lat: Double, lon: Double): Promise<Double, Exception> = task {
        val url = "http://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&APPID=$openWeatherMapAppId"
        val (request, response, result) = url.httpGet().responseString()

        val jsonString = String(response.data, Charset.forName("UTF-8"))
        val json = JsonParser().parse(jsonString).obj
        json["main"]["temp"].double
    }
}
