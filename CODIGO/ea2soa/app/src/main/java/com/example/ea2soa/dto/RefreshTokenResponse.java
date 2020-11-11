package com.example.ea2soa.dto;

public class RefreshTokenResponse {

    private Boolean success;
    private String token;
    private String token_refresh;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenRefresh() {
        return token_refresh;
    }

    public void setTokenRefresh(String token_refresh) {
        this.token_refresh = token_refresh;
    }
}
