package com.tessera.launcher.data.helper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
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

private data class TargetLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)

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

        val providers = buildList {
            add(LocationManager.NETWORK_PROVIDER)
            if (hasFineLocationPermission()) {
                add(LocationManager.GPS_PROVIDER)
            }
            add(LocationManager.PASSIVE_PROVIDER)
        }

        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                if (lm.isProviderEnabled(provider)) {
                    val loc = lm.getLastKnownLocation(provider)
                    if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                        bestLocation = loc
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao obter lastKnownLocation para $provider: ${e.message}")
            }
        }
        return bestLocation
    }

    @SuppressLint("MissingPermission")
    private suspend fun obtainDeviceLocation(): Location? = withContext(Dispatchers.IO) {
        val cached = getLastKnownLocation()
        if (cached != null) return@withContext cached
        if (!hasLocationPermission()) return@withContext null

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return@withContext null

        // Priorizar NETWORK_PROVIDER (torres e Wi-Fi) para retorno imediato (< 1s mesmo em ambientes internos)
        val provider = when {
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            hasFineLocationPermission() && lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) -> LocationManager.PASSIVE_PROVIDER
            else -> null
        } ?: return@withContext null

        kotlinx.coroutines.withTimeoutOrNull(2000L) {
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
            } catch (e: Exception) {
                Log.w(TAG, "Timeout ou falha ao requisitar localizacao do dispositivo: ${e.message}")
                null
            }
        }
    }

    private suspend fun resolveCityName(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        // 1. Tentar Geocoder nativo do Android
        val geocoderResult = kotlinx.coroutines.withTimeoutOrNull(1800L) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull()
                address?.locality ?: address?.subAdminArea ?: address?.adminArea
            } catch (e: Exception) {
                Log.w(TAG, "Geocoder nativo falhou: ${e.message}")
                null
            }
        }
        if (!geocoderResult.isNullOrBlank()) return@withContext geocoderResult

        // 2. Fallback de geocodificacao reversa via HTTP (BigDataCloud, sem API key)
        val reverseHttpResult = kotlinx.coroutines.withTimeoutOrNull(2000L) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$latitude&longitude=$longitude&localityLanguage=pt")
                conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 2000
                    readTimeout = 2000
                    setRequestProperty("User-Agent", "TesseraLauncher/1.7.9")
                }
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(text)
                    val locality = json.optString("locality").takeIf { it.isNotBlank() }
                    val city = json.optString("city").takeIf { it.isNotBlank() }
                    val region = json.optString("principalSubdivision").takeIf { it.isNotBlank() }
                    locality ?: city ?: region
                } else null
            } catch (_: Exception) {
                null
            } finally {
                conn?.disconnect()
            }
        }
        if (!reverseHttpResult.isNullOrBlank()) return@withContext reverseHttpResult

        "Local Atual"
    }

    /**
     * Fallback Instantaneo via IP Geolocation (ipwho.is com fallback para ip-api.com).
     * Retorna latitude, longitude e nome da cidade em ~40ms sem requerer permissao de GPS!
     */
    private suspend fun obtainIpLocation(): TargetLocation? = withContext(Dispatchers.IO) {
        // 1. Motor IP primario: ipwho.is (rapido, HTTPS, sem autenticacao)
        val ipwhoisResult = kotlinx.coroutines.withTimeoutOrNull(2500L) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https://ipwho.is/")
                conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 2000
                    readTimeout = 2000
                    setRequestProperty("User-Agent", "TesseraLauncher/1.7.9")
                    setRequestProperty("Accept", "application/json")
                }
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(text)
                    if (json.optBoolean("success", true)) {
                        val lat = json.optDouble("latitude", Double.NaN)
                        val lon = json.optDouble("longitude", Double.NaN)
                        val city = json.optString("city").ifBlank { json.optString("region") }.ifBlank { "Local Atual" }
                        if (!lat.isNaN() && !lon.isNaN()) {
                            TargetLocation(lat, lon, city)
                        } else null
                    } else null
                } else null
            } catch (e: Exception) {
                Log.w(TAG, "ipwho.is falhou: ${e.message}")
                null
            } finally {
                conn?.disconnect()
            }
        }
        if (ipwhoisResult != null) return@withContext ipwhoisResult

        // 2. Motor IP secundario: ip-api.com
        kotlinx.coroutines.withTimeoutOrNull(2500L) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("http://ip-api.com/json")
                conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 2000
                    readTimeout = 2000
                    setRequestProperty("User-Agent", "TesseraLauncher/1.7.9")
                }
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(text)
                    val lat = json.optDouble("lat", Double.NaN)
                    val lon = json.optDouble("lon", Double.NaN)
                    val city = json.optString("city").ifBlank { "Local Atual" }
                    if (!lat.isNaN() && !lon.isNaN()) {
                        TargetLocation(lat, lon, city)
                    } else null
                } else null
            } catch (e: Exception) {
                Log.w(TAG, "ip-api.com falhou: ${e.message}")
                null
            } finally {
                conn?.disconnect()
            }
        }
    }

    private suspend fun resolveTargetLocation(): TargetLocation? = withContext(Dispatchers.IO) {
        // Nivel 1: Dispositivo com GPS / Rede do Sistema
        if (hasLocationPermission()) {
            val devLoc = obtainDeviceLocation()
            if (devLoc != null) {
                val city = resolveCityName(devLoc.latitude, devLoc.longitude)
                Log.d(TAG, "Localizacao obtida via dispositivo: $city (${devLoc.latitude}, ${devLoc.longitude})")
                return@withContext TargetLocation(devLoc.latitude, devLoc.longitude, city)
            }
        }

        // Nivel 2: Fallback Ultrarrapido por IP (Zero permissao necessaria)
        val ipLoc = obtainIpLocation()
        if (ipLoc != null) {
            Log.d(TAG, "Localizacao obtida via IP: ${ipLoc.cityName} (${ipLoc.latitude}, ${ipLoc.longitude})")
            return@withContext ipLoc
        }

        null
    }

    private fun getSafeInputStream(conn: HttpURLConnection): InputStream {
        val isGzip = "gzip".equals(conn.contentEncoding, ignoreCase = true)
        val raw = conn.inputStream
        return if (isGzip) {
            try {
                GZIPInputStream(raw)
            } catch (_: Exception) {
                raw
            }
        } else {
            raw
        }
    }

    private fun fetchFromOpenMeteo(target: TargetLocation, isCelsius: Boolean): WeatherInfo? {
        var connection: HttpURLConnection? = null
        return try {
            val endpoint = "https://api.open-meteo.com/v1/forecast?latitude=${target.latitude}&longitude=${target.longitude}&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code&timezone=auto"
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "TesseraLauncher/1.7.9")
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(getSafeInputStream(connection), Charsets.UTF_8))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val current = json.getJSONObject("current")
                val temp = current.getDouble("temperature_2m")
                val apparent = current.optDouble("apparent_temperature", temp)
                val code = current.getInt("weather_code")
                val humidity = current.optInt("relative_humidity_2m", 50)
                val condition = getWeatherConditionDescription(code)

                Log.d(TAG, "Clima obtido com sucesso para ${target.cityName}: $temp°C, $condition")

                WeatherInfo(
                    temperature = temp,
                    apparentTemperature = apparent,
                    weatherCode = code,
                    condition = condition,
                    cityName = target.cityName,
                    humidity = humidity,
                    isCelsius = isCelsius
                )
            } else {
                Log.w(TAG, "Open-Meteo HTTP erro: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha na requisicao Open-Meteo: ${e.message}")
            null
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun fetchWeather(isCelsius: Boolean = true): WeatherInfo? = withContext(Dispatchers.IO) {
        val target = resolveTargetLocation() ?: return@withContext null
        fetchFromOpenMeteo(target, isCelsius)
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
        private const val TAG = "WeatherHelper"

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
