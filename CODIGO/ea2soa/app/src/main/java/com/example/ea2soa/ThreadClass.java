package com.example.ea2soa;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.ea2soa.dto.EventRegisterRequest;
import com.example.ea2soa.dto.EventRegisterResponse;
import com.example.ea2soa.dto.SharedPreferencesManager;
import com.example.ea2soa.services.SoaService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ThreadClass extends AsyncTask<String, String, String> {

    private Integer idThread = 0;
    private Context context;
    private int shakes;

    private SharedPreferencesManager sharedPreferencesManager;


    public ThreadClass(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        sharedPreferencesManager = new SharedPreferencesManager(context);
    }

    @Override

    protected String doInBackground(String... params) {
        idThread++;
        int shakes;

        shakes = sharedPreferencesManager.getShakes();

        shakes = shakes+1;
        sharedPreferencesManager.guardarShakes(shakes);

        registrarEventoShake();

        //String mensaje =params[0]+idThread;
        return Integer.toString(shakes);
    }

    @Override

    protected void onPostExecute(String result) {
        Toast.makeText(context, "Shakes realizados: " + result.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override

    protected void onProgressUpdate(String... params) {
        Toast.makeText(context,params[0], Toast.LENGTH_SHORT).show();
    }

    private void registrarEventoShake() {
        EventRegisterRequest request = new EventRegisterRequest();
        request.setEnv("PROD");
        request.setTypeEvents("SENSOR");
        request.setDescription("El usuario ha sacudido el telefono");

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://so-unlam.net.ar/api/")
                .build();

        SoaService soaService = retrofit.create(SoaService.class);

        //buscamos en sharedPreferencesManager el token que nos devolvio el logueo
        Call<EventRegisterResponse> call = soaService.registerEvent("Bearer " + sharedPreferencesManager.getToken(), request);
        call.enqueue(new Callback<EventRegisterResponse>() {
            @Override
            public void onResponse(Call<EventRegisterResponse> call, Response<EventRegisterResponse> response) {

                if (!response.isSuccessful()) {
                    publishProgress("Hubo un error al tratar de registrar el shake.");
                    Log.d("Shake", "Hubo un problema al gaurdar el evento");
                }

                Log.d("Shake", "Guardo el evento correctamente");
            }

            @Override
            public void onFailure(Call<EventRegisterResponse> call, Throwable t) {
                publishProgress("Hubo un error al tratar de registrar el shake.");
                Log.d("Shake", "Hubo un problema al gaurdar el evento");
            }
        });

    }


}


