package nic.goi.aarogyasetu.network

import nic.goi.aarogyasetu.utility.Constants

import java.io.IOException

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.GzipSink
import okio.Okio

internal class GzipRequestInterceptor : Interceptor {
    @Override
    @Throws(IOException::class)
    fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        if (originalRequest.body() == null || originalRequest.header(Constants.CONTENT_ENCODING) != null) {
            return chain.proceed(originalRequest)
        }

        val compressedRequest = originalRequest.newBuilder()
            .header(Constants.CONTENT_ENCODING, Constants.GZIP_VAL)
            .method(originalRequest.method(), gzip(originalRequest.body()))
            .build()
        return chain.proceed(compressedRequest)
    }

    private fun gzip(body: RequestBody): RequestBody {
        return object : RequestBody() {
            @Override
            fun contentType(): MediaType {
                return body.contentType()
            }

            @Override
            fun contentLength(): Long {
                return -1 // We don't know the compressed length in advance!
            }

            @Override
            @Throws(IOException::class)
            fun writeTo(sink: BufferedSink) {
                val gzipSink = Okio.buffer(GzipSink(sink))
                body.writeTo(gzipSink)
                gzipSink.close()
            }
        }
    }
}