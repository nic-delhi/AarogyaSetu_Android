/*
 * Copyright 2020 Government of India
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
      if (originalRequest.body() == null || originalRequest.header(Constants.CONTENT_ENCODING) != null) {
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