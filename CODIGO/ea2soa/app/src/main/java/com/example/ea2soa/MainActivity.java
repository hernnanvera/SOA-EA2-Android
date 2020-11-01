package com.example.ea2soa;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ea2soa.dto.LoginRequest;
import com.example.ea2soa.dto.LoginResponse;
import com.example.ea2soa.services.SoaService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity {

    private static String TAG = MainActivity.class.getName();

    private EditText txtEmail;
    private EditText txtPassword;
    Button btnIniciarSesion;
    Button btnRegistrarUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtEmail = (EditText) findViewById(R.id.txtUsuario);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnIniciarSesion = (Button) findViewById(R.id.btnIniciarSesion);
        btnRegistrarUsuario = (Button) findViewById(R.id.btnRegistrarse);

        btnIniciarSesion.setOnClickListener(botonesListeners);
        btnRegistrarUsuario.setOnClickListener(botonesListeners);

        Log.i( "Ejecuto", "Ejecuto onCreate");

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    //actua como listerner de los eventos que ocurren en los componentes graficos de la activity
    private View.OnClickListener botonesListeners = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent;

            if (!isConnect()) {
                Toast.makeText(MainActivity.this, "No hay conexion de internet.", Toast.LENGTH_LONG).show();
                return;
            }

            //Se determina que componente genero un evento
            switch (v.getId()) {
                //Si ocurrio un evento en Iniciar Sesion
                case R.id.btnRegistrarse:

                    //se le agrega un intent para lanzar la activity de registro
                    intent = new Intent( MainActivity.this, RegistroActivity.class);

                    startActivity(intent);
                    break;

                case R.id.btnIniciarSesion:

                    LoginRequest request = new LoginRequest();
                    request.setEmail(txtEmail.getText().toString());
                    request.setPassword(txtPassword.getText().toString());

                    Retrofit retrofit = new  Retrofit.Builder()
                            .addConverterFactory(GsonConverterFactory.create())
                            .baseUrl(getString(R.string.retrofit_server))
                            .build();

                    SoaService soaService = retrofit.create(SoaService.class);

                    retrofit2.Call<LoginResponse> call = soaService.register(request);
                    call.enqueue(new Callback<LoginResponse>(){
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                            if(response.isSuccessful()){
                                //TextView textEnv = findViewById(R.id.txtEnv);
                                //TextView textToken = findViewById(R.id.txtToken);
                                //TextView textTokenRefresh = findViewById(R.id.txtTokenRefresh);

                                //textEnv.setText(response.body().getEnv());
                                //textToken.setText(response.body().getToken());
                                //textTokenRefresh.setText(response.body().gettoken_refresh());

                                //se le agrega un intent para lanzar la activity de registro
                                Intent intentSensors;
                                intentSensors = new Intent(MainActivity.this, SensorsActivity.class);
                                startActivity(intentSensors);

                                Log.i(TAG,response.body().getToken());

                            }else
                            {
                                Log.i(TAG,response.errorBody().toString());
                                Toast.makeText(getApplicationContext(),  "Error en logueo de usuario", Toast.LENGTH_LONG).show();
                            }

                            Log.i(TAG,"Mensaje finalizado");
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            Log.e(TAG,t.getMessage());
                        }
                    });
                    break;

                default:
                    Toast.makeText(getApplicationContext(),  "Error en listener de botones", Toast.LENGTH_LONG).show();
            }
        }
    };

    private boolean isConnect(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }else{
            return false;
        }
    }
}