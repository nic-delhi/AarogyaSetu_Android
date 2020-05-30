package nic.goi.aarogyasetu.network

import android.text.TextUtils

import nic.goi.aarogyasetu.BuildConfig

import java.util.concurrent.Executors

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    private val BASE_URL = "https://" + BuildConfig.HOST_URL + "/"
    private var retrofit: Retrofit? = null

    /*
    This public static method will return Retrofit client
    anywhere in the appplication
    Configuring SSL Pinning
    */
    @JvmOverloads
    fun getRetrofitClient(
        zip: Boolean, isExecutor: Boolean, shouldUseSSL: Boolean, baseURLOverride: String,
        needsAuthenticator: Boolean = true
    ): Retrofit? {


        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val httpClient = OkHttpClient.Builder()
        if (zip) {
            httpClient.addInterceptor(GzipRequestInterceptor())
        }
        if (BuildConfig.DEBUG) {
            httpClient.addInterceptor(logging)
        }

        if (shouldUseSSL) {
            val certPinner = CertificatePinner.Builder()
                .add(BuildConfig.HOST_URL, BuildConfig.SSL_PUBLIC_KEY, BuildConfig.SSL_BACKUP_KEY)
                .add(BuildConfig.AUTH_HOST_URL, BuildConfig.SSL_AUTH_KEY, BuildConfig.SSL_AUTH_BACKUP_KEY)
                .build()
            httpClient.certificatePinner(certPinner)
        }
        httpClient.addInterceptor(SupportInterceptor())
        if (needsAuthenticator) {
            httpClient.authenticator(SupportInterceptor())
        }
        //Defining the Retrofit using Builder
        val finalBaseURL = if (TextUtils.isEmpty(baseURLOverride)) BASE_URL else baseURLOverride
        val retrofitbuilder = Retrofit.Builder()
            .baseUrl(finalBaseURL)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
        if (isExecutor) {
            retrofitbuilder.callbackExecutor(Executors.newSingleThreadExecutor())
        }
        retrofit = retrofitbuilder.build()
        return retrofit
    }
}
