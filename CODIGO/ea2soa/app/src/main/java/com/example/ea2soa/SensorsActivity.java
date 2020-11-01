package com.example.ea2soa;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.text.DecimalFormat;

public class SensorsActivity extends AppCompatActivity implements SensorEventListener{


    private SensorManager mSensorManager;
    private TextView    acelerometro;
    private TextView    giroscopio;

    DecimalFormat   dosDecimales = new DecimalFormat("###.###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        acelerometro = (TextView) findViewById(R.id.lblAcelerometro);
        giroscopio = (TextView) findViewById(R.id.lblGiroscopio);

        Log.d("onCreate",String.valueOf(this.getTaskId()));

        //accedemos al servicio de sensores
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    @Override
    protected void onRestart() {

        ini_sensores();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ini_sensores();
    }

    @Override
    protected void onPause() {
        super.onPause();
        parar_sensores();
    }

    protected  void ini_sensores()
    {
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected  void parar_sensores()
    {
        mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String txt = "";

        synchronized (this)
        {
            Log.d("sensor",event.sensor.getName());
            Log.d("sensor id",String.valueOf(this.getTaskId()));

            switch (event.sensor.getType())
            {
                case Sensor.TYPE_ACCELEROMETER:
                    txt += "Aceleroetro:\n";
                    txt += "x: " + dosDecimales.format(event.values[0]) + "m/seg2\n";
                    txt += "y:" + dosDecimales.format(event.values[1]) + "m/seg2\n";
                    txt += "z:" + dosDecimales.format(event.values[2]) + "m/seg2\n";
                    acelerometro.setText(txt);
                break;

                case Sensor.TYPE_GYROSCOPE:
                    txt += "Giroscopio :\n";
                    txt += "x: " + dosDecimales.format(event.values[0]) + "deg/s\n";
                    txt += "y:" + dosDecimales.format(event.values[1]) + "deg/s\n";
                    txt += "z:" + dosDecimales.format(event.values[2]) + "deg/s\n";
                    giroscopio.setText(txt);
                break;
            }
        }
    }
}
