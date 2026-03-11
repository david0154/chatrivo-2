package com.teamxdevelopers.SuperChat.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * WeatherManager - Fetches real-time weather using Open-Meteo (FREE, no API key needed)
 *
 * Usage in AI chat: When user asks "What's the weather in Kolkata?"
 * 1. Use getCoordinates() to resolve city name -> lat/lon
 * 2. Use getWeather() with those coords
 * 3. Pass the result to SarvamAIManager.chat() as context
 */
object WeatherManager {

    private val client = OkHttpClient()

    // WMO weather code descriptions
    private val wmoDescriptions = mapOf(
        0 to "Clear sky", 1 to "Mainly clear", 2 to "Partly cloudy", 3 to "Overcast",
        45 to "Fog", 48 to "Icy fog",
        51 to "Light drizzle", 53 to "Moderate drizzle", 55 to "Dense drizzle",
        61 to "Slight rain", 63 to "Moderate rain", 65 to "Heavy rain",
        71 to "Slight snow", 73 to "Moderate snow", 75 to "Heavy snow",
        80 to "Slight showers", 81 to "Moderate showers", 82 to "Violent showers",
        95 to "Thunderstorm", 96 to "Thunderstorm with hail", 99 to "Heavy thunderstorm with hail"
    )

    data class WeatherInfo(
        val cityName: String,
        val temperature: Double,
        val feelsLike: Double,
        val humidity: Int,
        val windSpeed: Double,
        val description: String,
        val isDay: Boolean
    ) {
        fun toNaturalLanguage(): String {
            val timeOfDay = if (isDay) "daytime" else "night"
            return "Current weather in $cityName: $description, ${temperature}°C " +
                "(feels like ${feelsLike}°C), humidity ${humidity}%, " +
                "wind ${windSpeed} km/h, $timeOfDay."
        }

        fun toFormattedString(): String {
            val emoji = if (isDay) "☀️" else "🌙"
            return """$emoji Weather in $cityName
🌡️ Temperature: ${temperature}°C (feels like ${feelsLike}°C)
💧 Humidity: ${humidity}%
💨 Wind: ${windSpeed} km/h
🌤️ Condition: $description"""
        }
    }

    /**
     * Get coordinates for Indian cities (built-in map, no geocoding API needed)
     */
    fun getCityCoordinates(cityName: String): Pair<Double, Double>? {
        val indianCities = mapOf(
            "kolkata" to Pair(22.5726, 88.3639),
            "mumbai" to Pair(19.0760, 72.8777),
            "delhi" to Pair(28.6139, 77.2090),
            "new delhi" to Pair(28.6139, 77.2090),
            "bangalore" to Pair(12.9716, 77.5946),
            "bengaluru" to Pair(12.9716, 77.5946),
            "chennai" to Pair(13.0827, 80.2707),
            "hyderabad" to Pair(17.3850, 78.4867),
            "pune" to Pair(18.5204, 73.8567),
            "ahmedabad" to Pair(23.0225, 72.5714),
            "jaipur" to Pair(26.9124, 75.7873),
            "lucknow" to Pair(26.8467, 80.9462),
            "surat" to Pair(21.1702, 72.8311),
            "kanpur" to Pair(26.4499, 80.3319),
            "nagpur" to Pair(21.1458, 79.0882),
            "patna" to Pair(25.5941, 85.1376),
            "bhopal" to Pair(23.2599, 77.4126),
            "visakhapatnam" to Pair(17.6868, 83.2185),
            "indore" to Pair(22.7196, 75.8577),
            "thane" to Pair(19.2183, 72.9781),
            "guwahati" to Pair(26.1445, 91.7362),
            "chandigarh" to Pair(30.7333, 76.7794),
            "coimbatore" to Pair(11.0168, 76.9558),
            "kochi" to Pair(9.9312, 76.2673),
            "bhubaneswar" to Pair(20.2961, 85.8245)
        )
        return indianCities[cityName.lowercase().trim()]
    }

    /**
     * Fetch weather data from Open-Meteo API (no API key required)
     * @param lat Latitude
     * @param lon Longitude
     * @param cityName Display name for the city
     */
    suspend fun getWeather(
        lat: Double,
        lon: Double,
        cityName: String
    ): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature," +
                "weather_code,wind_speed_10m,is_day" +
                "&wind_speed_unit=kmh&timezone=Asia%2FKolkata"

            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val json = JSONObject(response.body?.string() ?: "")
                val current = json.getJSONObject("current")

                val wmoCode = current.getInt("weather_code")
                WeatherInfo(
                    cityName = cityName,
                    temperature = current.getDouble("temperature_2m"),
                    feelsLike = current.getDouble("apparent_temperature"),
                    humidity = current.getInt("relative_humidity_2m"),
                    windSpeed = current.getDouble("wind_speed_10m"),
                    description = wmoDescriptions[wmoCode] ?: "Unknown",
                    isDay = current.getInt("is_day") == 1
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convenience: get weather by city name (Indian cities)
     */
    suspend fun getWeatherByCity(cityName: String): WeatherInfo? {
        val coords = getCityCoordinates(cityName) ?: return null
        return getWeather(coords.first, coords.second, cityName.replaceFirstChar { it.uppercase() })
    }
}
