package com.example.myapplication.network;

import com.example.myapplication.models.SignUpRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // Ví dụ POST request để đăng ký tài khoản mới
    @POST("signup")  // Thay thế "signup" bằng endpoint của API
    Call<SignUpRequest> signUp(@Body SignUpRequest.SignUpRequest signUpRequest);

}