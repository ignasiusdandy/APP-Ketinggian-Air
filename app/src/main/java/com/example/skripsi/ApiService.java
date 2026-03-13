package com.example.skripsi;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("api/auth/register")
    Call<ResponseBody> registerUser(
            @Field("username") String nama,
            @Field("email") String email,
            @Field("password") String password,
            @Field("id_kendaraan") String idKendaraan
    );

    @GET("api/auth/kendaraan")
    Call<KendaraanResponse> getDataKendaraan();
}
