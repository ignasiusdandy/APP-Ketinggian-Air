package com.baingat.app;

import com.google.gson.annotations.SerializedName;

public class TokenFcmResponse {
    @SerializedName("fcm_token")
    private String fcmToken;

    public String getFcmToken(){
        return fcmToken;
    }

}
