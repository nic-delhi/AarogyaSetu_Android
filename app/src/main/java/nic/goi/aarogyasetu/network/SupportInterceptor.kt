package nic.goi.aarogyasetu.network

import nic.goi.aarogyasetu.BuildConfig
import nic.goi.aarogyasetu.CoronaApplication
import nic.goi.aarogyasetu.utility.AuthUtility
import nic.goi.aarogyasetu.utility.Constants
import okhttp3.*
import java.io.IOException


class SupportInterceptor : Interceptor, Authenticator {

    /**
     * Interceptor class for setting of the headers for every request
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val newBuilder = request.newBuilder()
        if (chain.request().header(Constants.AUTH).isNullOrBlank()) {
            val token = AuthUtility.getToken()
            if (!token.isNullOrBlank()) {
                newBuilder.addHeader(Constants.AUTH, token)
            }
        }
        newBuilder.addHeader(
            Constants.PLATFORM,
            BuildConfig.PLATFORM_KEY
        )
        newBuilder.addHeader(
            Constants.VERSION,
            BuildConfig.VERSION_CODE.toString()
        )
        request = newBuilder
            .build()
        return chain.proceed(request)
    }

    /**
     * Authenticator for when the authToken need to be refresh and updated
     * everytime we get a 401 error code
     */
    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response?): Request? {
        var requestAvailable: Request? = null
        try {
            var isTokenUpdated = false
            for (i in 0..2) {
                val isNewTokenUpdated: Boolean = updateToken()
                if (isNewTokenUpdated) {
                    isTokenUpdated = true
                    break
                }
            }
            if (!isTokenUpdated) {
                throw IOException()
            }
            requestAvailable = response?.request()?.newBuilder()
                ?.addHeader(Constants.AUTH, AuthUtility.getToken()?:"")
                ?.build()
            return requestAvailable
        } catch (ex: IOException) {
            AuthUtility.logout(CoronaApplication.instance)
            throw ex
        }
    }

    private fun updateToken(): Boolean {
        try {
            AuthUtility.updateToken()
        } catch (ee: IOException) {
            return false
        }
        return true
    }

}
