package uk.ashleybye.models

/**
 * Data class to store *SunWeatherInfo*.
 *
 * Stores @see[sunInfo] and temperature.
 *
 * @property @see[sunInfo]
 * @property temperature
 */
data class SunWeatherInfo(val sunInfo: SunInfo, val temperature: Double)