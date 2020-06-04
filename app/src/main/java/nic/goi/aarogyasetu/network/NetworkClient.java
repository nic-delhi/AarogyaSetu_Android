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

import android.text.TextUtils;

import nic.goi.aarogyasetu.BuildConfig;

import java.util.concurrent.Executors;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    private static final String BASE_URL = "https://" + BuildConfig.HOST_URL + "/";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitClient(boolean zip, boolean isExecutor, boolean shouldUseSSL, String baseURLOverride) {
        return getRetrofitClient(zip, isExecutor, shouldUseSSL, baseURLOverride, true);
    }

    /*
    This public static method will return Retrofit client
    anywhere in the appplication
    Configuring SSL Pinning
    */
    public static Retrofit getRetrofitClient(boolean zip, boolean isExecutor, boolean shouldUseSSL, String baseURLOverride,
                                             boolean needsAuthenticator) {


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        if (zip) {
            httpClient.addInterceptor(new GzipRequestInterceptor());
        }
        if (BuildConfig.DEBUG) {
            httpClient.addInterceptor(logging);
        }

        if (shouldUseSSL) {
            CertificatePinner certPinner = new CertificatePinner.Builder()
                    .add(BuildConfig.HOST_URL, BuildConfig.SSL_PUBLIC_KEY, BuildConfig.SSL_BACKUP_KEY)
                    .add(BuildConfig.AUTH_HOST_URL, BuildConfig.SSL_AUTH_KEY, BuildConfig.SSL_AUTH_BACKUP_KEY)
                    .build();
            httpClient.certificatePinner(certPinner);
        }
        httpClient.addInterceptor(new SupportInterceptor());
        if (needsAuthenticator) {
            httpClient.authenticator(new SupportInterceptor());
        }
        //Defining the Retrofit using Builder
        String finalBaseURL = TextUtils.isEmpty(baseURLOverride) ? BASE_URL : baseURLOverride;
        Retrofit.Builder retrofitbuilder = new Retrofit.Builder()
                .baseUrl(finalBaseURL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create());
        if (isExecutor) {
            retrofitbuilder.callbackExecutor(Executors.newSingleThreadExecutor());
        }
        retrofit = retrofitbuilder.build();
        return retrofit;
    }
}
