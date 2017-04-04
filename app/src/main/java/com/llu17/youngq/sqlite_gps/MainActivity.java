package com.llu17.youngq.sqlite_gps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;


import okhttp3.OkHttpClient;



public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

//    private final static String tag = "Gps_SQLite";
//    static ImageView iv_gray1, iv_gray2, iv_gray3, iv_gray4, iv_gray5, iv_pink1;
    private String acce_sr, gyro_sr, gps_sr, step_sr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        acce_sr = sharedPreferences.getString(getResources().getString(R.string.sr_key_accelerometer),"1000");
        Log.e("-----Acce SR-----",""+acce_sr);
        gyro_sr = sharedPreferences.getString(getResources().getString(R.string.sr_key_gyroscope),"1000");
        Log.e("-----Gyro SR-----",""+gyro_sr);
        gps_sr = sharedPreferences.getString(getResources().getString(R.string.sr_key_gps),"1000");
        Log.e("-----Gps SR-----",""+gps_sr);
        step_sr = sharedPreferences.getString(getResources().getString(R.string.sr_key_step),"1000");
        Log.e("-----Step SR-----",""+step_sr);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        //////////
        /*===check sqlite data using "chrome://inspect"===*/
        Stetho.initializeWithDefaults(this);
        new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
        //////////

//        iv_gray1 = (ImageView)findViewById(R.id.gray1);
//        iv_gray2 = (ImageView)findViewById(R.id.gray2);
//        iv_gray3 = (ImageView)findViewById(R.id.gray3);
//        iv_gray4 = (ImageView)findViewById(R.id.gray4);
//        iv_gray5 = (ImageView)findViewById(R.id.gray5);
//        iv_pink1 = (ImageView)findViewById(R.id.pink1);
//
//        iv_gray1.setVisibility(android.view.View.GONE);
//        iv_gray2.setVisibility(android.view.View.GONE);
//        iv_gray3.setVisibility(android.view.View.GONE);
//        iv_gray4.setVisibility(android.view.View.GONE);
//        iv_gray5.setVisibility(android.view.View.GONE);
//        iv_pink1.setVisibility(android.view.View.GONE);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}, 10);
//            Toast.makeText(this, "Check_Permission", Toast.LENGTH_SHORT).show();
        }


        int v = 0;
        try {
            v = getPackageManager().getPackageInfo("com.google.android.gms", 0 ).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.e("version:-------",""+ v);

    }
    /*Sampling rate menu*/
    //Add the menu to the menu bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sampling_rate_menu, menu);
        return true;
    }
    //When the "Settings" menu item is pressed, open SettingsActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.sr_key_accelerometer))) {
            acce_sr = sharedPreferences.getString(key, "1000");
            Log.e("-----Acce SR-----","changed: "+acce_sr);
        }
        if (key.equals(getString(R.string.sr_key_gyroscope))) {
            gyro_sr = sharedPreferences.getString(key, "1000");
            Log.e("-----Gyro SR-----","changed: "+gyro_sr);
        }
        if (key.equals(getString(R.string.sr_key_gps))) {
            gps_sr = sharedPreferences.getString(key, "1000");
            Log.e("-----Gps SR-----","changed: "+gps_sr);
        }
        if(key.equals(getString(R.string.sr_key_step))){
            step_sr = sharedPreferences.getString(key, "1000");
            Log.e("-----Step SR-----","changed: "+step_sr);
        }
    }

    public void startService(View view) {

//        intent=new Intent();
//        intent.setClass(MainActivity.this, CollectorService.class);//从一个activity跳转到另一个activity
//        intent.putExtra("acce", acce_sr);//给intent添加额外数据，key为“str”,key值为"Intent Demo"
//        intent.putExtra("gyro", gyro_sr);
//        intent.putExtra("gps", gps_sr);
//        intent.putExtra("step", step_sr);
//        startService(intent);

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        Log.e("unregister","success");

//        getPreferenceScreen().findPreference(getString(R.string.sr_key_accelerometer)).setEnabled(false);
//        getPreferenceScreen().findPreference(getString(R.string.sr_key_accelerometer)).setShouldDisableView(true);

        startService(new Intent(getBaseContext(), CollectorService.class));
        startService(new Intent(getBaseContext(), Activity_Tracker.class));
        Toast.makeText(this, "Starting the service", Toast.LENGTH_SHORT).show();
    }

    // Method to stop the service
    public void stopService(View view) {
        Toast.makeText(this, "Stopping the service", Toast.LENGTH_SHORT).show();
        stopService(new Intent(getBaseContext(), CollectorService.class));
        stopService(new Intent(getBaseContext(), Activity_Tracker.class));
        stopService(new Intent(getBaseContext(), HandleActivity.class));

    }

    public void uploadService(View view){
        Toast.makeText(this, "Begin to upload data", Toast.LENGTH_SHORT).show();
        startService(new Intent(getBaseContext(), UploadService.class));
    }

    public void breakService(View view){
        Toast.makeText(this, "Break the connection with MySQL", Toast.LENGTH_SHORT).show();
        stopService(new Intent(getBaseContext(), UploadService.class));
    }
}
