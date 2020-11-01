package com.example.ea2soa.dto;

public class RegistroResponse {
    private Boolean success;
    private String env;
    private String token;
    private String token_refresh;

    public  Boolean getSuccess(){return success;};

    public  void setSuccess(Boolean success){this.success = success;};

    public  String getEnv(){return env;}

    public  void  setEnv(String env){ this.env = env;}

    public  String getToken(){ return token;}

    public  void setToken(String token){this.token = token;}

    public  String gettoken_refresh(){ return token_refresh;}

    public  void settoken_refresh(String token_refresh){this.token = token_refresh;}

}
