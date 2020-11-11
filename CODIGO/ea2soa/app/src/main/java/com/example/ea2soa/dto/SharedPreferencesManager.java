package com.example.ea2soa.dto;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class SharedPreferencesManager {

    private Context context;
    private SharedPreferences sharedPreferences;


    public SharedPreferencesManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
    }

    public void guardarToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Date tokenHour = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String tokenHourFormatted = formatter.format(tokenHour);

        editor.putString("hour_token", tokenHourFormatted);
        editor.putString("token", token);
        editor.commit();
    }

    public void guardarTokenRefresh(String tokenRefresh) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token_refresh", tokenRefresh);
        editor.commit();
    }


    public void guardarShakes(int shakes) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.putString("shakes", shakes);
        editor.putInt("shakes",shakes);
        editor.commit();
    }


    public String getToken() {
        String token = sharedPreferences.getString("token", null);
        return token;
    }

    public String getTokenHour() {
        String tokenHour = sharedPreferences.getString("hour_token", null);
        return tokenHour;
    }

    public String getTokenRefresh() {
        String tokenRefresh = sharedPreferences.getString("token_refresh", null);
        return tokenRefresh;
    }

    public int getShakes() {
        int shakes = sharedPreferences.getInt("shakes", 0);
        return shakes;
    }
}
