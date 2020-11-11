package com.example.ea2soa;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ea2soa.dto.EventRegisterRequest;
import com.example.ea2soa.dto.EventRegisterResponse;
import com.example.ea2soa.dto.RefreshTokenResponse;
import com.example.ea2soa.dto.SharedPreferencesManager;
import com.example.ea2soa.services.SoaService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private SensorManager mSensorManager;
    private TextView acelerometro;
    private TextView giroscopio;
    private TextView lblNumero1;
    private TextView lblNumero2;
    private TextView lblNumero3;
    private String sTokenRefresh;
    private long ultimaActualizacion = 0;
    private long ultimaActualizacionGiroscopio = 0;
    private float ultimoX, ultimoY, ultimoZ;
    private float ultimoXG, ultimoYG, ultimoZG;
    private static final int UMBRAL_SHAKE = 200;
    private static final int UMBRAL_GIROSCOPIO = 200;
    private static final int VALOR_INICIAL = 0;
    FirebaseDatabase database;
    DatabaseReference myRef;

    private SharedPreferencesManager sharedPreferencesManager;

    private ThreadClass threadClass;

    DecimalFormat dosDecimales = new DecimalFormat("###.###");

    private static String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferencesManager = new SharedPreferencesManager(getApplicationContext());

        threadClass = new ThreadClass(getApplicationContext());

        sharedPreferencesManager.guardarShakes(VALOR_INICIAL);

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Registro de jugadas");

        acelerometro = (TextView) findViewById(R.id.lblAcelerometro);
        giroscopio = (TextView) findViewById(R.id.lblGiroscopio);
        lblNumero1 = (TextView) findViewById(R.id.lblNumero1);
        lblNumero2 = (TextView) findViewById(R.id.lblNumero2);
        lblNumero3 = (TextView) findViewById(R.id.lblNumero3);

        Log.d("onCreate", String.valueOf(this.getTaskId()));

        //accedemos al servicio de sensores
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    @Override
    protected void onStart() {
        Log.i(TAG, "Ejecuto OnStart");
        super.onStart();
    }


    @Override
    protected void onStop() {
        Log.i(TAG, "Ejecuto onStop");
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        Log.i(TAG, "Ejecuto OnDestroy");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {

        Log.i(TAG, "Ejecuto onRestart");
        ini_sensores();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Ejecuto onResume");
        super.onResume();
        ini_sensores();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Ejecuto onPause");
        super.onPause();
        parar_sensores();
    }

    protected void ini_sensores() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void parar_sensores() {
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String txt = "";


        synchronized (this) {

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:

                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    txt += "Aceleroetro:\n";
                    txt += "x: " + dosDecimales.format(x) + "m/seg2\n";
                    txt += "y:" + dosDecimales.format(y) + "m/seg2\n";
                    txt += "z:" + dosDecimales.format(z) + "m/seg2\n";
                    acelerometro.setText(txt);

                    long tiempoActual = System.currentTimeMillis();

                    if ((tiempoActual - ultimaActualizacion) > 200) {

                        Log.d("sensor", event.sensor.getName());
                        Log.d("sensor id", String.valueOf(this.getTaskId()));

                        long diffTime = (tiempoActual - ultimaActualizacion);
                        ultimaActualizacion = tiempoActual;

                        float speed = Math.abs(x + y + z - ultimoX - ultimoY - ultimoZ) / diffTime * 10000;

                        if (speed > UMBRAL_SHAKE) {

                            Log.d("sensor", "Paso umbral acelerometro");

                            //verificamos si la sesion expiro
                            verificarYRefrescarToken();

                            new ThreadClass(getApplicationContext()).execute();
                            getRandomNumber();
                            registrarEventoSensor("Acelerometro");


                            registrarJugadaEnFirebase();

                        }

                        ultimoX = x;
                        ultimoY = y;
                        ultimoZ = z;
                    }
                    //threadClass.execute();
                    break;

                case Sensor.TYPE_GYROSCOPE:

                    float xG = event.values[0];
                    float yG = event.values[1];
                    float zG = event.values[2];

                    txt += "Giroscopio :\n";
                    txt += "x: " + dosDecimales.format(xG) + "deg/s\n";
                    txt += "y:" + dosDecimales.format(yG) + "deg/s\n";
                    txt += "z:" + dosDecimales.format(zG) + "deg/s\n";

                    giroscopio.setText(txt);

                    long tiempoActualGiroscopio = System.currentTimeMillis();

                    if ((tiempoActualGiroscopio - ultimaActualizacionGiroscopio) > 200) {
                        Log.d("sensor", event.sensor.getName());
                        Log.d("sensor id", String.valueOf(this.getTaskId()));

                        long diffTime = (tiempoActualGiroscopio - ultimaActualizacionGiroscopio);
                        ultimaActualizacionGiroscopio = tiempoActualGiroscopio;

                        float speedGiroscopio = Math.abs(xG + yG + zG - ultimoXG - ultimoYG - ultimoZG) / diffTime * 10000;

                        if (speedGiroscopio > UMBRAL_GIROSCOPIO) {

                            Log.d("sensor", "Paso umbral giroscopio");
                            registrarEventoSensor("Giroscopio");

                        }

                    }

                    break;
            }
        }

    }

    private void getRandomNumber() {
        ArrayList numerosGenerados = new ArrayList();

        for (int i = 0; i < 3; i++) {
            Random randNum = new Random();
            int iNum = randNum.nextInt(9998) + 1;

            if (!numerosGenerados.contains(iNum)) {
                numerosGenerados.add(iNum);
            } else {
                i--;
            }
        }

        lblNumero1.setText(numerosGenerados.get(0).toString());
        lblNumero2.setText(numerosGenerados.get(1).toString());
        lblNumero3.setText(numerosGenerados.get(2).toString());

    }

    private void registrarEventoSensor(String tipoSensor) {
        EventRegisterRequest request = new EventRegisterRequest();
        request.setEnv("PROD");
        request.setTypeEvents("SENSOR");
        request.setDescription("El usuario ha utilizado el sensor" + tipoSensor);

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
                    Toast.makeText(MainActivity.this, "Hubo un error al tratar de registrar los eventos del sensor", Toast.LENGTH_LONG).show();
                    Log.d("sensor", "Hubo un error al tratar de registrar el evento generado por el " + tipoSensor);
                }

                Log.d("sensor", "Registro el evento generado por el " + tipoSensor + "correctamente.");
            }

            @Override
            public void onFailure(Call<EventRegisterResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Hubo un error al tratar de registrar los eventos del sensor -Failure", Toast.LENGTH_LONG).show();
                Log.d("sensor", "Hubo un error al tratar de registrar el evento generado por el " + tipoSensor);
            }
        });

    }

    public void verificarYRefrescarToken() {
        try {
            if (!revovarToken()) {
                refreshToken();
            }
        } catch (Exception e) {
            refreshToken();
        }

    }

    public boolean revovarToken() throws ParseException {

        Log.d("LOG_SENSOR", "ejecuto metodo renovarToken");
        String sHoraToken = sharedPreferencesManager.getTokenHour();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date dtHoraToken = formatter.parse(sHoraToken);
        Date now = Calendar.getInstance().getTime();

        long millis = now.getTime() - dtHoraToken.getTime();
        long minutos = (millis / (1000 * 60)) % 60;

        if (minutos < 30) {
            return true;
        } else {
            return false;
        }
    }

    public void refreshToken() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getString(R.string.retrofit_server))
                .build();

        SoaService soaService = retrofit.create(SoaService.class);


        retrofit2.Call<RefreshTokenResponse> call = soaService.refreshToken(sharedPreferencesManager.getTokenRefresh());


        call.enqueue(new Callback<RefreshTokenResponse>() {
            @Override
            public void onResponse(Call<RefreshTokenResponse> call, Response<RefreshTokenResponse> response) {
                if (response.isSuccessful()) {
                    sharedPreferencesManager.guardarToken(response.body().getToken());
                    sharedPreferencesManager.guardarTokenRefresh(response.body().getTokenRefresh());
                    Log.d("LOG_SENSOR", "Se actualizo el token correctamente");
                } else {
                    Toast.makeText(MainActivity.this, String.format("Informacion erronea"), Toast.LENGTH_LONG).show();
                    Log.d("LOG_SENSOR", "Ha ocurrido un error al actualizar el token");
                }
            }

            @Override
            public void onFailure(Call<RefreshTokenResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ha ocurrido un error al actualizar el token", Toast.LENGTH_LONG).show();
                Log.d("LOG_SENSOR", "Ha ocurrido un error al actualizar el token");
            }
        });

    }

    public  void registrarJugadaEnFirebase(){
        myRef.push().setValue("Han salido los numeros " +
                "Primer Lugar:  "  +  lblNumero1.getText().toString() + ", " +
                "Segundo Lugar: " + lblNumero2.getText().toString() + ", " +
                "Tercer Lugar:  "  + lblNumero3.getText().toString() +
                " en la jugada nro: " + sharedPreferencesManager.getShakes());
    }

}