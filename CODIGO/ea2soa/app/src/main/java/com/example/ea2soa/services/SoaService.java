package com.example.ea2soa.services;

import com.example.ea2soa.dto.RegistroRequest;
import com.example.ea2soa.dto.RegistroResponse;
import com.example.ea2soa.dto.LoginRequest;
import com.example.ea2soa.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SoaService {

    @POST("api/register")
    Call<RegistroResponse> register(@Body RegistroRequest request);

    @POST("api/login")
    Call<LoginResponse> register(@Body LoginRequest request);

}
