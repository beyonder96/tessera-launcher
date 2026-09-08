package com.tessera.launcher.data.helper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class WeatherInfo(
    val temperature: Double,
    val apparentTemperature: Double,
    val weatherCode: Int,
    val condition: String,
    val cityName: String,
    val humidity: Int,
    val isCelsius: Boolean = true
) {
    val displayTemperature: String
        get() = if (isCelsius) {
            "${temperature.toInt()}°C"
        } else {
            val fahrenheit = (temperature * 9 / 5) + 32
            "${fahrenheit.toInt()}°F"
        }

    val displayApparent: String
        get() = if (isCelsius) {
            "${apparentTemperature.toInt()}°"
        } else {
            val fahrenheit = (apparentTemperature * 9 / 5) + 32
            "${fahrenheit.toInt()}°"
        }
}

class WeatherHelper(private val context: Context) {

    fun hasLocationPermission(): Boolean {
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarse || fine
    }

    private fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        val hasFine = hasFineLocationPermission()
        val providers = buildList {
            // "gps" provider strictly requires ACCESS_FINE_LOCATION on Android 12+ (API 31+)
            if (hasFine) {
                add(LocationManager.GPS_PROVIDER)
            }
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)
        }

        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                val isEnabled = try {
                    lm.isProviderEnabled(provider)
                } catch (_: SecurityException) {
                    false
                } catch (_: Exception) {
                    false
                }

                if (isEnabled) {
                    val loc = try {
                        lm.getLastKnownLocation(provider)
                    } catch (_: SecurityException) {
                        null
                    } catch (_: Exception) {
                        null
                    }
                    if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                        bestLocation = loc
                    }
                }
            } catch (_: Exception) {
            }
        }
        return bestLocation
    }

    private suspend fun resolveCityName(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        kotlinx.coroutines.withTimeoutOrNull(2500L) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull()
                address?.locality ?: address?.subAdminArea ?: address?.adminArea ?: "Minha Cidade"
            } catch (_: Exception) {
                "Local Atual"
            }
        } ?: "Local Atual"
    }

    suspend fun fetchWeather(isCelsius: Boolean = true): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val location = try {
                getLastKnownLocation()
            } catch (_: Exception) {
                null
            }
            val lat = location?.latitude ?: -23.5505 // Fallback padrão (São Paulo)
            val lon = location?.longitude ?: -46.6333
            val city = if (location != null) resolveCityName(lat, lon) else "São Paulo"

            val endpoint = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code&timezone=auto"

        var connection: HttpURLConnection? = null
        try {
            val url = URL(endpoint)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 6000
            connection.readTimeout = 6000
            connection.setRequestProperty("User-Agent", "TesseraLauncher/1.7.3")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val current = json.getJSONObject("current")
                val temp = current.getDouble("temperature_2m")
                val apparent = current.optDouble("apparent_temperature", temp)
                val code = current.getInt("weather_code")
                val humidity = current.optInt("relative_humidity_2m", 50)
                val condition = getWeatherConditionDescription(code)

                WeatherInfo(
                    temperature = temp,
                    apparentTemperature = apparent,
                    weatherCode = code,
                    condition = condition,
                    cityName = city,
                    humidity = humidity,
                    isCelsius = isCelsius
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    } catch (_: Exception) {
        null
    }
}

    fun toJson(info: WeatherInfo): String {
        return JSONObject().apply {
            put("temperature", info.temperature)
            put("apparentTemperature", info.apparentTemperature)
            put("weatherCode", info.weatherCode)
            put("condition", info.condition)
            put("cityName", info.cityName)
            put("humidity", info.humidity)
            put("isCelsius", info.isCelsius)
        }.toString()
    }

    fun fromJson(jsonStr: String?): WeatherInfo? {
        if (jsonStr.isNullOrBlank()) return null
        return try {
            val json = JSONObject(jsonStr)
            WeatherInfo(
                temperature = json.getDouble("temperature"),
                apparentTemperature = json.getDouble("apparentTemperature"),
                weatherCode = json.getInt("weatherCode"),
                condition = json.getString("condition"),
                cityName = json.getString("cityName"),
                humidity = json.getInt("humidity"),
                isCelsius = json.optBoolean("isCelsius", true)
            )
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        fun getWeatherConditionDescription(code: Int): String {
            return when (code) {
                0 -> "Céu limpo"
                1 -> "Principalmente limpo"
                2 -> "Parcialmente nublado"
                3 -> "Nublado"
                45, 48 -> "Nevoeiro"
                51, 53, 55 -> "Garoa leve"
                56, 57 -> "Garoa congelante"
                61 -> "Chuva leve"
                63 -> "Chuva moderada"
                65 -> "Chuva forte"
                66, 67 -> "Chuva congelante"
                71, 73, 75 -> "Neve"
                77 -> "Grãos de neve"
                80, 81, 82 -> "Pancadas de chuva"
                85, 86 -> "Pancadas de neve"
                95 -> "Tempestade"
                96, 99 -> "Tempestade com granizo"
                else -> "Tempo ameno"
            }
        }
    }
}
