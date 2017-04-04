package com.llu17.youngq.sqlite_gps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.llu17.youngq.sqlite_gps.data.GpsContract;
import com.llu17.youngq.sqlite_gps.data.GpsDbHelper;
import com.llu17.youngq.sqlite_gps.table.ACCELEROMETER;
import com.llu17.youngq.sqlite_gps.table.GPS;
import com.llu17.youngq.sqlite_gps.table.GYROSCOPE;
import com.llu17.youngq.sqlite_gps.table.MOTIONSTATE;
import com.llu17.youngq.sqlite_gps.table.STEP;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * Created by youngq on 17/3/28.
 */

public class UploadService extends Service{

    private PowerManager.WakeLock wakeLock = null;

    private GpsDbHelper dbHelper;
    private SQLiteDatabase db;

    int count = 0;  //used to calculate num of star (num of tables finished upload)
    boolean gray1,gray2,gray3,gray4,gray5;
    ArrayList<GPS> gpses;
    ArrayList<ACCELEROMETER> acces;
    ArrayList<GYROSCOPE> gyros;
    ArrayList<MOTIONSTATE> motions;
    ArrayList<STEP> steps;

    /****MySQL****/
    private static final String REMOTE_IP = "localhost:33104";//这里是映射地址，可以随意写，不是服务器地址
    private static final String URL = "jdbc:mysql://" + REMOTE_IP + "/smartpark_lulu_test";
    private static final String USER = "smartpark";
    private static final String PASSWORD = "Shuwie4Eofei";
    public Connection conn;


    public void onConnSsh() {   //connect ssh then connect MySQL

        new Thread() {
            public void run() {
                Log.e("============", "预备连接服务器");
                Util.go();
                Log.e("============", "预备连接数据库");
                conn = Util.openConnection(URL, USER, PASSWORD);
            }
        }.start();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        acquireWakeLock();
        onConnSsh();
        /*make sure MySQL is connected*/
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(conn != null) {
            Log.e("===conn===", "===not null===");
        }
        else{
            Toast.makeText(this, "No Connection", Toast.LENGTH_LONG).show();
            onDestroy();
            Log.e("===conn onStart===","===null===");
            return START_NOT_STICKY;
        }
        gpses = find_all_gps();
        acces = find_all_acce();
        gyros = find_all_gyro();
        motions = find_all_motion();
        steps = find_all_step();

        Thread t1 = new Thread() {
            public void run() {
                for(int i = 0 ; i < gpses.size(); i++)
                    onInsertGps(gpses.get(i).getId(),gpses.get(i).getTimestamp(),gpses.get(i).getLatitude(),gpses.get(i).getLongitude());
                gray1 = true;
            }
        };
        t1.start();
        Thread t2 = new Thread() {
            public void run() {
                for(int i = 0; i < acces.size(); i++)
                    onInsertAcce(acces.get(i).getId(),acces.get(i).getTimestamp(),acces.get(i).getX(),acces.get(i).getY(),acces.get(i).getZ());
                gray2 = true;
            }
        };
        t2.start();
        Thread t3 = new Thread() {
            public void run() {
                for(int i = 0; i < gyros.size(); i++)
                    onInsertGyro(gyros.get(i).getId(),gyros.get(i).getTimestamp(),gyros.get(i).getX(),gyros.get(i).getY(),gyros.get(i).getZ());
                gray3 = true;
            }
        };
        t3.start();
        Thread t4 = new Thread() {
            public void run() {
                for(int i = 0; i < motions.size(); i++)
                    onInsertMotion(motions.get(i).getId(),motions.get(i).getTimestamp(),motions.get(i).getState());
                gray4 = true;
            }
        };
        t4.start();
        Thread t5 = new Thread() {
            public void run() {
                for(int i = 0; i < steps.size(); i++)
                    onInsertStep(steps.get(i).getId(),steps.get(i).getTimestamp(),steps.get(i).getCount());
                gray5 = true;
            }
        };
        t5.start();

        Log.e("From Sqlite --->>>"," To MySQL");
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                conn = null;
            } finally {
                conn = null;
            }
        }
        Log.e("===conn onDestroy===", "===null===");

//        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    private ArrayList<GPS> find_all_gps(){
        dbHelper = new GpsDbHelper(this);
        db = dbHelper.getReadableDatabase();
        String s = "select Id, timestamp, latitude, longitude from gps_location;";
        Cursor c = db.rawQuery(s,null);
        ArrayList<GPS> gpslist = new ArrayList<>();
        GPS gps = null;
        while(c.moveToNext()){
            gps = new GPS();
            gps.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.GpsEntry.COLUMN_ID)));
            gps.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.GpsEntry.COLUMN_TIMESTAMP)));
            gps.setLatitude(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GpsEntry.COLUMN_LATITUDE)));
            gps.setLongitude(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GpsEntry.COLUMN_LONGITUDE)));
            gpslist.add(gps);
//            onInsertGps(gps.getId(),gps.getTimestamp(),gps.getLatitude(),gps.getLongitude());
        }
        c.close();
        db.close();
        return gpslist;
    }

    private ArrayList<ACCELEROMETER> find_all_acce(){
        dbHelper = new GpsDbHelper(this);
        db = dbHelper.getReadableDatabase();
        String s = "select Id, timestamp, X, Y, Z from accelerometer;";
        Cursor c = db.rawQuery(s,null);
        ArrayList<ACCELEROMETER> accelist = new ArrayList<>();
        ACCELEROMETER acce = null;
        while(c.moveToNext()){
            acce = new ACCELEROMETER();
            acce.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.AccelerometerEntry.COLUMN_ID)));
            acce.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.AccelerometerEntry.COLUMN_TIMESTAMP)));
            acce.setX(c.getDouble(c.getColumnIndexOrThrow(GpsContract.AccelerometerEntry.COLUMN_X)));
            acce.setY(c.getDouble(c.getColumnIndexOrThrow(GpsContract.AccelerometerEntry.COLUMN_Y)));
            acce.setZ(c.getDouble(c.getColumnIndexOrThrow(GpsContract.AccelerometerEntry.COLUMN_Z)));
            accelist.add(acce);
//            onInsertGps(gps.getId(),gps.getTimestamp(),gps.getLatitude(),gps.getLongitude());
        }
        c.close();
        db.close();
        return accelist;
    }
    private ArrayList<GYROSCOPE> find_all_gyro(){
        dbHelper = new GpsDbHelper(this);
        db = dbHelper.getReadableDatabase();
        String s = "select Id, timestamp, X, Y, Z from gyroscope;";
        Cursor c = db.rawQuery(s,null);
        ArrayList<GYROSCOPE> gyrolist = new ArrayList<>();
        GYROSCOPE gyro = null;
        while(c.moveToNext()){
            gyro = new GYROSCOPE();
            gyro.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.GyroscopeEntry.COLUMN_ID)));
            gyro.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.GyroscopeEntry.COLUMN_TIMESTAMP)));
            gyro.setX(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GyroscopeEntry.COLUMN_X)));
            gyro.setY(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GyroscopeEntry.COLUMN_Y)));
            gyro.setZ(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GyroscopeEntry.COLUMN_Z)));
            gyrolist.add(gyro);
//            onInsertGps(gps.getId(),gps.getTimestamp(),gps.getLatitude(),gps.getLongitude());
        }
        c.close();
        db.close();
        return gyrolist;
    }
    private ArrayList<MOTIONSTATE> find_all_motion(){
        dbHelper = new GpsDbHelper(this);
        db = dbHelper.getReadableDatabase();
        String s = "select Id, timestamp, state from motionstate;";
        Cursor c = db.rawQuery(s,null);
        ArrayList<MOTIONSTATE> motionlist = new ArrayList<>();
        MOTIONSTATE motion = null;
        while(c.moveToNext()){
            motion = new MOTIONSTATE();
            motion.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.MotionStateEntry.COLUMN_ID)));
            motion.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.MotionStateEntry.COLUMN_TIMESTAMP)));
            motion.setState(c.getInt(c.getColumnIndexOrThrow(GpsContract.MotionStateEntry.COLUMN_STATE)));
            motionlist.add(motion);
//            onInsertGps(gps.getId(),gps.getTimestamp(),gps.getLatitude(),gps.getLongitude());
        }
        c.close();
        db.close();
        return motionlist;
    }
    private ArrayList<STEP> find_all_step(){
        dbHelper = new GpsDbHelper(this);
        db = dbHelper.getReadableDatabase();
        String s = "select Id, timestamp, Count from step;";
        Cursor c = db.rawQuery(s,null);
        ArrayList<STEP> steplist = new ArrayList<>();
        STEP step = null;
        while(c.moveToNext()){
            step = new STEP();
            step.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.StepEntry.COLUMN_ID)));
            step.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.StepEntry.COLUMN_TIMESTAMP)));
            step.setCount(c.getInt(c.getColumnIndexOrThrow(GpsContract.StepEntry.COLUMN_COUNT)));
            steplist.add(step);
//            onInsertGps(gps.getId(),gps.getTimestamp(),gps.getLatitude(),gps.getLongitude());
        }
        c.close();
        db.close();
        return steplist;
    }

    private void onInsertGps(String s, long l, double lat, double lon) {
        final String id = s;
        final long timestampe = l;
        final double latitude = lat;
        final double longitude = lon;
//        new Thread() {
//            public void run() {
//                Log.e("============", "预备插入");
                long time = System.currentTimeMillis();
//                Log.e("======Time======", "Gps");
                String sql = "insert into Gps values(" + "null," + "\"" + id +  "\"" + ", " + timestampe + ", " + latitude + ", " + longitude+ ");";
                Util.execSQL(conn, sql);
//            }
//        }.start();
    }
    private void onInsertAcce(String s, long l, double x1, double y1, double z1) {
        final String id = s;
        final long timestampe = l;
        final double x = x1;
        final double y = y1;
        final double z = z1;
//        new Thread() {
//            public void run() {
//                Log.e("============", "预备插入");
                long time = System.currentTimeMillis();
//                Log.e("======Time======", "Acce");
                String sql = "insert into Accelerometer values("+ "null,"  + "\"" + id +  "\"" + ", " + timestampe + ", " + x + ", " + y + ", " + z + ");";
                Util.execSQL(conn, sql);
//            }
//        }.start();
    }
    private void onInsertGyro(String s, long l, double x1, double y1, double z1) {
        final String id = s;
        final long timestampe = l;
        final double x = x1;
        final double y = y1;
        final double z = z1;
//        new Thread() {
//            public void run() {
//                Log.e("============", "预备插入");
                long time = System.currentTimeMillis();
//                Log.e("======Time======", "Gryo");
                String sql = "insert into Gyroscope values("+ "null,"  + "\"" + id +  "\"" + ", " + timestampe + ", " + x + ", " + y + ", " + z + ");";
                Util.execSQL(conn, sql);
//            }
//        }.start();
    }
    private void onInsertMotion(String s, long l, int s1) {
        final String id = s;
        final long timestampe = l;
        final int state = s1;
//        new Thread() {
//            public void run() {
//        Log.e("============", "预备插入");
        long time = System.currentTimeMillis();
//        Log.e("======Time======", "MotionState");
        String sql = "insert into MotionState values(" + "null," + "\"" + id + "\"" + ", " + timestampe + ", " + state + ");";
        Util.execSQL(conn, sql);
//            }
//        }.start();
    }
    private void onInsertStep(String s, long l, int c) {
        final String id = s;
        final long timestampe = l;
        final int count = c;
//        new Thread() {
//            public void run() {
//                Log.e("============", "预备插入");
                long time = System.currentTimeMillis();
//                Log.e("======Time======", "Step");
                String sql = "insert into Step values("+ "null,"  + "\"" + id +  "\"" + ", " + timestampe + ", " + count + ");";
                Util.execSQL(conn, sql);
//            }
//        }.start();
    }
    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }
    //释放设备电源锁
    private void releaseWakeLock()
    {
        if (null != wakeLock)
        {
            wakeLock.release();
            wakeLock = null;
        }
    }

}
