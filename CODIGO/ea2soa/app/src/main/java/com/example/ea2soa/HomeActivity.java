package com.example.ea2soa;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ea2soa.dto.EventRegisterRequest;
import com.example.ea2soa.dto.EventRegisterResponse;
import com.example.ea2soa.dto.LoginRequest;
import com.example.ea2soa.dto.LoginResponse;
import com.example.ea2soa.dto.SharedPreferencesManager;
import com.example.ea2soa.services.SoaService;
import com.google.firebase.iid.FirebaseInstanceId;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends Activity {

    private static String TAG = HomeActivity.class.getName();

    private EditText txtEmail;
    private EditText txtPassword;
    private String sToken;
    private String sTokenRefresh;
    private SharedPreferencesManager sharedPreferencesManager;

    Button btnIniciarSesion;
    Button btnRegistrarUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPreferencesManager = new SharedPreferencesManager(getApplicationContext());

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(token ->
        {
            Log.e(TAG, token.getToken());
        });

        txtEmail = (EditText) findViewById(R.id.txtUsuario);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnIniciarSesion = (Button) findViewById(R.id.btnIniciarSesion);
        btnRegistrarUsuario = (Button) findViewById(R.id.btnRegistrarse);

        btnIniciarSesion.setOnClickListener(botonesListeners);
        btnRegistrarUsuario.setOnClickListener(botonesListeners);

        verificarBateria();

        Log.i(TAG, "Ejecuto onCreate");

    }

    @Override
    protected void onStart() {
        Log.i(TAG, "Ejecuto OnStart");
        super.onStart();
    }


    @Override
    protected void onResume() {
        Log.i(TAG, "Ejecuto onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Ejecuto onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "Ejecuto onStop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "Ejecuto onRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "Ejecuto OnDestroy");
        super.onDestroy();
    }

    //actua como listerner de los eventos que ocurren en los componentes graficos de la activity
    private View.OnClickListener botonesListeners = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent;

            if (!isConnect()) {
                Toast.makeText(HomeActivity.this, "No hay conexion de internet.", Toast.LENGTH_LONG).show();
                return;
            }

            //Se determina que componente genero un evento
            switch (v.getId()) {
                //Si ocurrio un evento en Iniciar Sesion
                case R.id.btnRegistrarse:

                    //se le agrega un intent para lanzar la activity de registro
                    intent = new Intent(HomeActivity.this, RegistroActivity.class);

                    Log.i(TAG, "Presiono Registrarse");

                    startActivity(intent);
                    break;

                case R.id.btnIniciarSesion:

                    if (!validarCampos()) {
                        return;
                    }

                    LoginRequest request = new LoginRequest();
                    request.setEmail(txtEmail.getText().toString());
                    request.setPassword(txtPassword.getText().toString());

                    Log.i(TAG, "Presiono Iniciar Sesion");

                    Retrofit retrofit = new Retrofit.Builder()
                            .addConverterFactory(GsonConverterFactory.create())
                            .baseUrl(getString(R.string.retrofit_server))
                            .build();

                    SoaService soaService = retrofit.create(SoaService.class);

                    retrofit2.Call<LoginResponse> call = soaService.register(request);
                    call.enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                            if (response.isSuccessful()) {

                                //guardamos el token del logueo de usuario
                                sharedPreferencesManager.guardarToken(response.body().getToken());
                                sharedPreferencesManager.guardarTokenRefresh(response.body().getTokenRefresh());

                                //registramos el evento de inicio de sesion
                                registrarEvento();

                                //se le agrega un intent para lanzar la activity de registro
                                Intent intentSensors;
                                intentSensors = new Intent(HomeActivity.this, MainActivity.class);

                                startActivity(intentSensors);

                            } else {
                                Log.i(TAG, response.errorBody().toString());
                                Toast.makeText(getApplicationContext(), "Error en logueo de usuario", Toast.LENGTH_LONG).show();
                            }

                            Log.i(TAG, "Mensaje finalizado");
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            Log.e(TAG, t.getMessage());
                            Toast.makeText(getApplicationContext(), "Hubo un error al tratar de iniciar sesión", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;

                default:
                    Log.i(TAG, "Error en listener de botones");
                    Toast.makeText(getApplicationContext(), "Error en listener de botones", Toast.LENGTH_LONG).show();
            }
        }
    };

    private boolean isConnect() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private void registrarEvento() {
        EventRegisterRequest request = new EventRegisterRequest();
        request.setEnv("PROD");
        request.setTypeEvents("LOGIN");
        request.setDescription("El usuario se ha logueado exitosamente");

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getString(R.string.retrofit_server))
                .build();

        SoaService soaService = retrofit.create(SoaService.class);

        //buscamos en sharedPreferencesManager el token que nos devolvio el logueo
        Call<EventRegisterResponse> call = soaService.registerEvent("Bearer " + sharedPreferencesManager.getToken(), request);
        call.enqueue(new Callback<EventRegisterResponse>() {
            @Override
            public void onResponse(Call<EventRegisterResponse> call, Response<EventRegisterResponse> response) {

                if (!response.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Hubo un error al tratar de registrar el evento de logueo", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<EventRegisterResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Hubo un error al tratar de registrar el evento de logueo.", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void verificarBateria() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //Intent estado = contex.registerReceiver(null, intentFilter);
        Intent estado = getBaseContext().registerReceiver(null, intentFilter);
        int nivel = estado.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int escala = estado.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int porcentaje = nivel * 100 / escala;

        Toast.makeText(this, "El nivel de bateria es del " + porcentaje + "%.", Toast.LENGTH_SHORT).show();
    }

    private boolean validarCampos() {
        if (txtEmail.getText().toString().equals("")) {
            Toast.makeText(HomeActivity.this, "Es necesario ingresar el mail de usuario.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (txtPassword.getText().toString().equals("")) {
            Toast.makeText(HomeActivity.this, "Es necesario ingresar la contraseña.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}