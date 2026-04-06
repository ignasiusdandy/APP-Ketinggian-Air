package com.example.skripsi;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
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

    @FormUrlEncoded
    @POST("api/auth/login")
    Call<LoginResponse> loginUser(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("api/auth/kendaraan")
    Call<KendaraanResponse> getDataKendaraan();

    @GET("api/kendaraan/utama")
    Call<KendaraanUtamaResponseModel> getKendaraanUtama(
            @Header("Authorization") String token
    );

    @GET("api/kendaraan/user")
    Call<KendaraanUserResponseModel> getKendaraanUser(
            @Header("Authorization") String token
    );

    @FormUrlEncoded
    @PATCH("api/kendaraan/update")
    Call<UpdatePengaturanAkunModel> updateUser(
            @Header("Authorization") String token,
            @Field("nama") String nama,
            @Field("id_kendaraan") String idKendaraan
    );
}
