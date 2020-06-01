package nic.goi.aarogyasetu.network;

import nic.goi.aarogyasetu.utility.Constants;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

class GzipRequestInterceptor implements Interceptor {
    @Override public Response intercept(Chain chain) throws IOException {
      Request originalRequest = chain.request();
      if (originalRequest.body() == null || originalRequest.header(Constants.CONTENT_ENCODING) == null) {
        return chain.proceed(originalRequest);
      }

      Request compressedRequest = originalRequest.newBuilder()
          .header(Constants.CONTENT_ENCODING, Constants.GZIP_VAL)
          .method(originalRequest.method(), gzip(originalRequest.body()))
          .build();
      return chain.proceed(compressedRequest);
    }

    private RequestBody gzip(final RequestBody body) {
      return new RequestBody() {
        @Override public MediaType contentType() {
          return body.contentType();
        }

        @Override public long contentLength() {
          return -1; // We don't know the compressed length in advance!
        }

        @Override public void writeTo(BufferedSink sink) throws IOException {
          BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
          body.writeTo(gzipSink);
          gzipSink.close();
        }
      };
    }
  }