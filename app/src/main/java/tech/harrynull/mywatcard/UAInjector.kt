package tech.harrynull.mywatcard

import android.icu.util.ULocale.getLanguage
import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.*

/**
 * Adds a custom `User-Agent` header to OkHttp requests.
 * From https://gist.github.com/twaddington/e66e495e14950b4437216ab5c704835b
 */
class UAInjector() : Interceptor {
    private val userAgent: String = String.format(
        Locale.US,
        "MyWatCard/%s",
        BuildConfig.VERSION_NAME
    )

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val userAgentRequest = chain.request()
            .newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(userAgentRequest)
    }
}