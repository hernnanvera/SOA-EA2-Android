package com.example.ea2soa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ea2soa.dto.RegistroRequest;
import com.example.ea2soa.dto.RegistroResponse;
import com.example.ea2soa.services.SoaService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistroActivity extends AppCompatActivity {

    private static String TAG = RegistroActivity.class.getName();

    private EditText txtNombre;
    private EditText txtApellido;
    private EditText txtDni;
    private EditText txtMail;
    private EditText txtEnv;
    private EditText txtPassword;
    private EditText txtComision;

    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        txtNombre = (EditText) findViewById(R.id.txtNombre);
        txtApellido = (EditText) findViewById(R.id.txtApellido);
        txtDni = (EditText) findViewById(R.id.txtDni);
        txtMail = (EditText) findViewById(R.id.txtMail);
        txtComision = (EditText) findViewById(R.id.txtComision);
        txtPassword = (EditText) findViewById(R.id.txtPassword);

        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(HandlerBtnRegistrar);

    }

    //actua como listener de los eventos que ocurren en los componentes de graficos de las activity
    private View.OnClickListener HandlerBtnRegistrar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!isConnect()) {
                Toast.makeText(RegistroActivity.this, "No hay conexion de internet.", Toast.LENGTH_LONG).show();
                return;
            }

            if (txtPassword.getText().toString().length() < 8) {
                Toast.makeText(RegistroActivity.this, "El campo Password requiere al menos 8 caracteres.", Toast.LENGTH_LONG).show();
                return;
            }

            RegistroRequest request = new RegistroRequest();
            request.setEnv("PROD");//PROD
            request.setName(txtNombre.getText().toString());
            request.setLastname(txtApellido.getText().toString());
            request.setDni(Long.parseLong(txtDni.getText().toString()));
            request.setEmail(txtMail.getText().toString());
            request.setPassword(txtPassword.getText().toString());
            request.setCommission(Long.parseLong(txtComision.getText().toString()));

            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(getString(R.string.retrofit_server))
                    .build();

            SoaService soaService = retrofit.create(SoaService.class);

            retrofit2.Call<RegistroResponse> call = soaService.register(request);
            call.enqueue(new Callback<RegistroResponse>() {
                @Override
                public void onResponse(Call<RegistroResponse> call, Response<RegistroResponse> response) {

                    if (response.isSuccessful()) {

                        Intent intentSensors;
                        intentSensors = new Intent(RegistroActivity.this, SensorsActivity.class);
                        startActivity(intentSensors);

                        Log.i(TAG, response.body().getToken());
                    } else {
                        Log.i(TAG, response.errorBody().toString());
                    }

                    Log.i(TAG, "Mensaje finalizado");
                }

                @Override
                public void onFailure(Call<RegistroResponse> call, Throwable t) {
                    Log.e(TAG, t.getMessage());
                }
            });

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

}
