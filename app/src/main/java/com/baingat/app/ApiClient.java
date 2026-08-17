package com.baingat.app;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.io.IOException;

public class ApiClient {
    public static final String BASE_URL = "https://apidandy.baingat.my.id";
//    public static final String BASE_URL = "http://172.24.67.169:3001/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    Response response = chain.proceed(request);

                    if (response.code() == 401) {
                        String path = request.url().encodedPath();
                        if (path != null && !path.contains("login")) {
                            Context context = MyApplication.getContext();
                            if (context != null) {
                                SessionManager sessionManager = new SessionManager(context);
                                sessionManager.logoutUser();
                                
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);
                            }
                        }
                    }
                    return response;
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
