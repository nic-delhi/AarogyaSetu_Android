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
