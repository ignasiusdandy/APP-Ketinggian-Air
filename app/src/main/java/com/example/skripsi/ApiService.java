package com.example.skripsi;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @GET("api/data/statusUtama")
    Call<StatusUtamaResponseModel> getStatusUtama(
            @Header("Authorization") String token
    );

    @GET("api/kendaraan/user")
    Call<KendaraanUserResponseModel> getKendaraanUserSPK(
            @Header("Authorization") String token,
            @Query("id_lokasi") String idLokasi
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

    @DELETE("api/kendaraan/hapus/{id_kendaraan}")
    Call<ResponseBody> hapusKendaraan(
            @Header("Authorization") String token,
            @Path("id_kendaraan") String idKendaraan
    );


    @GET("api/data/chartAll")
    Call<ChartAllResponseModel> getChartData();

    @POST("api/kendaraan/tambahKendaraanUser")
    Call<ResponseBody> tambahKendaraan(
            @Header("Authorization") String token,
            @Body TambahKendaraanRequestModel request
    );

    @PUT("api/kendaraan/updateKendaraan/{id}")
    Call<ResponseBody> updateKendaraan(
            @Header("Authorization") String token,
            @Path("id") String idKendaraanLama,
            @Body EditKendaraanRequestModel request
    );

}
