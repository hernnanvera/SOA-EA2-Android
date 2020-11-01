package com.example.ea2soa.dto;

public class LoginResponse {
    private Boolean success;
    private String token;
    private String token_refresh;

    public  Boolean getSuccess(){return success;};

    public  void setSuccess(Boolean success){this.success = success;};

    public  String getToken(){ return token;}

    public  void setToken(String token){this.token = token;}

    public  String getToken_refresh(){ return token_refresh;}

    public  void setToken_refresh(String token_refresh){this.token = token_refresh;}
}
