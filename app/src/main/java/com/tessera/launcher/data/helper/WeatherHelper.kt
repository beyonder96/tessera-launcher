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
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.zip.GZIPInputStream

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

    @SuppressLint("MissingPermission")
    private suspend fun obtainLocation(): Location? = withContext(Dispatchers.IO) {
        val cached = getLastKnownLocation()
        if (cached != null) return@withContext cached
        if (!hasLocationPermission()) return@withContext null

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return@withContext null
        val provider = if (hasFineLocationPermission() && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationManager.GPS_PROVIDER
        } else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            LocationManager.NETWORK_PROVIDER
        } else if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            LocationManager.PASSIVE_PROVIDER
        } else {
            null
        } ?: return@withContext null

        kotlinx.coroutines.withTimeoutOrNull(3000L) {
            try {
                kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                    val signal = androidx.core.os.CancellationSignal()
                    cont.invokeOnCancellation { signal.cancel() }
                    androidx.core.location.LocationManagerCompat.getCurrentLocation(
                        lm,
                        provider,
                        signal,
                        ContextCompat.getMainExecutor(context)
                    ) { loc ->
                        if (cont.isActive) {
                            cont.resume(loc) {}
                        }
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun resolveCityName(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        kotlinx.coroutines.withTimeoutOrNull(2000L) {
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

    private fun getInputStream(conn: HttpURLConnection): InputStream {
        val isGzip = "gzip".equals(conn.contentEncoding, ignoreCase = true)
        return if (isGzip) GZIPInputStream(conn.inputStream) else conn.inputStream
    }

    private fun mapWttrToWmoCode(wttrCode: Int): Int {
        return when (wttrCode) {
            113 -> 0 // Céu limpo
            116 -> 2 // Parcialmente nublado
            119, 122 -> 3 // Nublado
            143, 248, 260 -> 45 // Nevoeiro
            176, 263, 266, 281, 284, 293, 296, 299, 302, 305, 308, 311, 314, 353, 356, 359 -> 61 // Chuva
            179, 182, 185, 227, 230, 320, 323, 326, 329, 332, 335, 338, 350, 368, 371 -> 71 // Neve
            200, 386, 389, 392, 395 -> 95 // Tempestade
            else -> 1
        }
    }

    private fun fetchFromWttr(location: Location?, isCelsius: Boolean): WeatherInfo? {
        var connection: HttpURLConnection? = null
        return try {
            val urlString = if (location != null) {
                "https://wttr.in/${location.latitude},${location.longitude}?format=j1"
            } else {
                "https://wttr.in/?format=j1"
            }
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4500
                readTimeout = 4500
                setRequestProperty("User-Agent", "TesseraLauncher/1.7.7")
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(getInputStream(connection)))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val currArray = json.optJSONArray("current_condition")
                val curr = currArray?.optJSONObject(0) ?: return null

                val tempC = curr.optDouble("temp_C", Double.NaN)
                if (tempC.isNaN()) return null

                val feelsLikeC = curr.optDouble("FeelsLikeC", tempC)
                val humidity = curr.optInt("humidity", 50)
                val wttrCode = curr.optInt("weatherCode", 113)
                val wmoCode = mapWttrToWmoCode(wttrCode)
                val condition = getWeatherConditionDescription(wmoCode)

                val nearestAreaArray = json.optJSONArray("nearest_area")
                val nearestArea = nearestAreaArray?.optJSONObject(0)
                val areaNameArray = nearestArea?.optJSONArray("areaName")
                val resolvedCity = areaNameArray?.optJSONObject(0)?.optString("value")?.takeIf { it.isNotBlank() }
                    ?: "Local Atual"

                WeatherInfo(
                    temperature = tempC,
                    apparentTemperature = feelsLikeC,
                    weatherCode = wmoCode,
                    condition = condition,
                    cityName = resolvedCity,
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
    }

    private fun fetchFromOpenMeteo(location: Location?, isCelsius: Boolean): WeatherInfo? {
        var connection: HttpURLConnection? = null
        return try {
            val lat = location?.latitude ?: -23.5505
            val lon = location?.longitude ?: -46.6333
            val city = if (location != null) "Local Atual" else "São Paulo"

            val endpoint = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code&timezone=auto"
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "TesseraLauncher/1.7.7")
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(getInputStream(connection)))
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
    }

    suspend fun fetchWeather(isCelsius: Boolean = true): WeatherInfo? = withContext(Dispatchers.IO) {
        val location = try {
            obtainLocation()
        } catch (_: Exception) {
            null
        }

        // 1. Motor primário ultrarrápido: wttr.in (< 1s com suporte a IP)
        val wttrResult = fetchFromWttr(location, isCelsius)
        if (wttrResult != null) return@withContext wttrResult

        // 2. Motor secundário de contingência: Open-Meteo
        val openMeteoResult = fetchFromOpenMeteo(location, isCelsius)
        if (openMeteoResult != null) return@withContext openMeteoResult

        null
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
