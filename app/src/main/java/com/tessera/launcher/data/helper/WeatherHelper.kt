package com.tessera.launcher.data.helper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
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
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
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

        val provider = when {
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            hasFineLocationPermission() && lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) -> LocationManager.PASSIVE_PROVIDER
            else -> null
        } ?: return@withContext null

        kotlinx.coroutines.withTimeoutOrNull(3000L) {
            try {
                kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                    val signal = androidx.core.os.CancellationSignal()
                    cont.invokeOnCancellation { signal.cancel() }
                    @Suppress("DEPRECATION")
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
                Log.w(TAG, "Falha ao requisitar localizacao do dispositivo: ${e.message}")
                null
            }
        }
    }

    private suspend fun resolveCityName(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        val geocoderResult = kotlinx.coroutines.withTimeoutOrNull(2000L) {
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

        val reverseHttpResult = kotlinx.coroutines.withTimeoutOrNull(2500L) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$latitude&longitude=$longitude&localityLanguage=pt")
                conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 2500
                    readTimeout = 2500
                    setRequestProperty("User-Agent", USER_AGENT)
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

    private fun getTimeZoneLocation(): TargetLocation {
        val tzId = try {
            TimeZone.getDefault().id
        } catch (_: Exception) {
            "America/Sao_Paulo"
        }

        return when {
            tzId.contains("Sao_Paulo", ignoreCase = true) -> TargetLocation(-23.5505, -46.6333, "São Paulo")
            tzId.contains("Fortaleza", ignoreCase = true) -> TargetLocation(-3.7319, -38.5267, "Fortaleza")
            tzId.contains("Recife", ignoreCase = true) -> TargetLocation(-8.0476, -34.8770, "Recife")
            tzId.contains("Salvador", ignoreCase = true) || tzId.contains("Bahia", ignoreCase = true) -> TargetLocation(-12.9777, -38.5016, "Salvador")
            tzId.contains("Manaus", ignoreCase = true) -> TargetLocation(-3.1190, -60.0217, "Manaus")
            tzId.contains("Belem", ignoreCase = true) -> TargetLocation(-1.4558, -48.4902, "Belém")
            tzId.contains("Cuiaba", ignoreCase = true) -> TargetLocation(-15.6014, -56.0979, "Cuiabá")
            tzId.contains("Campo_Grande", ignoreCase = true) -> TargetLocation(-20.4697, -54.6201, "Campo Grande")
            tzId.contains("Porto_Velho", ignoreCase = true) -> TargetLocation(-8.7619, -63.9039, "Porto Velho")
            tzId.contains("Boa_Vista", ignoreCase = true) -> TargetLocation(2.8235, -60.6758, "Boa Vista")
            tzId.contains("Rio_Branco", ignoreCase = true) -> TargetLocation(-9.9753, -67.8249, "Rio Branco")
            tzId.contains("Maceio", ignoreCase = true) -> TargetLocation(-9.6498, -35.7089, "Maceió")
            tzId.contains("Araguaina", ignoreCase = true) -> TargetLocation(-7.1911, -48.2072, "Araguaína")
            tzId.contains("Lisbon", ignoreCase = true) || tzId.contains("Lisboa", ignoreCase = true) -> TargetLocation(38.7223, -9.1393, "Lisboa")
            tzId.contains("London", ignoreCase = true) -> TargetLocation(51.5074, -0.1278, "Londres")
            tzId.contains("New_York", ignoreCase = true) -> TargetLocation(40.7128, -74.0060, "Nova York")
            tzId.contains("Buenos_Aires", ignoreCase = true) -> TargetLocation(-34.6037, -58.3816, "Buenos Aires")
            else -> TargetLocation(-23.5505, -46.6333, "São Paulo")
        }
    }

    private suspend fun fetchJsonWithTimeout(urlString: String, timeoutMs: Int): JSONObject? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                JSONObject(text)
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Falha em $urlString: ${e.message}")
            null
        } finally {
            conn?.disconnect()
        }
    }

    private suspend fun obtainIpLocation(): TargetLocation? = withContext(Dispatchers.IO) {
        // 1. ipwho.is
        val res1 = fetchJsonWithTimeout("https://ipwho.is/", timeoutMs = 3500)
        if (res1 != null && res1.optBoolean("success", true)) {
            val lat = res1.optDouble("latitude", Double.NaN)
            val lon = res1.optDouble("longitude", Double.NaN)
            val city = res1.optString("city").ifBlank { res1.optString("region") }
            if (!lat.isNaN() && !lon.isNaN()) {
                return@withContext TargetLocation(lat, lon, city.ifBlank { "Local Atual" })
            }
        }

        // 2. freeipapi.com
        val res2 = fetchJsonWithTimeout("https://freeipapi.com/api/json", timeoutMs = 3500)
        if (res2 != null) {
            val lat = res2.optDouble("latitude", Double.NaN)
            val lon = res2.optDouble("longitude", Double.NaN)
            val city = res2.optString("cityName").ifBlank { res2.optString("regionName") }
            if (!lat.isNaN() && !lon.isNaN()) {
                return@withContext TargetLocation(lat, lon, city.ifBlank { "Local Atual" })
            }
        }

        // 3. ip-api.com
        val res3 = fetchJsonWithTimeout("http://ip-api.com/json", timeoutMs = 3500)
        if (res3 != null) {
            val lat = res3.optDouble("lat", Double.NaN)
            val lon = res3.optDouble("lon", Double.NaN)
            val city = res3.optString("city")
            if (!lat.isNaN() && !lon.isNaN()) {
                return@withContext TargetLocation(lat, lon, city.ifBlank { "Local Atual" })
            }
        }

        null
    }

    private suspend fun resolveTargetLocation(): TargetLocation = withContext(Dispatchers.IO) {
        // Nível 1: Dispositivo com GPS / Rede Celular
        if (hasLocationPermission()) {
            val devLoc = obtainDeviceLocation()
            if (devLoc != null) {
                val city = resolveCityName(devLoc.latitude, devLoc.longitude)
                Log.d(TAG, "Localizacao do dispositivo: $city (${devLoc.latitude}, ${devLoc.longitude})")
                return@withContext TargetLocation(devLoc.latitude, devLoc.longitude, city)
            }
        }

        // Nível 2: Geolocalizacao rapida por IP
        val ipLoc = obtainIpLocation()
        if (ipLoc != null) {
            Log.d(TAG, "Localizacao por IP: ${ipLoc.cityName} (${ipLoc.latitude}, ${ipLoc.longitude})")
            return@withContext ipLoc
        }

        // Nível 3: Failsafe instantaneo por Fuso Horario (0ms, sempre valido)
        val tzLoc = getTimeZoneLocation()
        Log.d(TAG, "Localizacao por Fuso Horario: ${tzLoc.cityName} (${tzLoc.latitude}, ${tzLoc.longitude})")
        tzLoc
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
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", USER_AGENT)
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

    suspend fun fetchWeather(isCelsius: Boolean = true): WeatherInfo = withContext(Dispatchers.IO) {
        val target = resolveTargetLocation()

        // 1. Tentar obter da Open-Meteo
        val liveWeather = fetchFromOpenMeteo(target, isCelsius)
        if (liveWeather != null) return@withContext liveWeather

        // 2. Failsafe inteligente offline (caso o celular esteja sem internet no momento)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val defaultTemp = when (hour) {
            in 0..5 -> 18.0
            in 6..11 -> 22.0
            in 12..16 -> 26.0
            in 17..20 -> 23.0
            else -> 20.0
        }
        val defaultCode = when (hour) {
            in 6..18 -> 1
            else -> 0
        }

        WeatherInfo(
            temperature = defaultTemp,
            apparentTemperature = defaultTemp,
            weatherCode = defaultCode,
            condition = getWeatherConditionDescription(defaultCode),
            cityName = target.cityName,
            humidity = 60,
            isCelsius = isCelsius
        )
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
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

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
