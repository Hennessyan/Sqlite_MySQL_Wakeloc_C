package com.llu17.youngq.sqlite_gps.upload;

import android.content.ContentValues;
import android.database.SQLException;
import android.util.Log;

import com.llu17.youngq.sqlite_gps.data.GpsContract;

import java.util.TimerTask;

import static com.llu17.youngq.sqlite_gps.CollectorService.id;
import static com.llu17.youngq.sqlite_gps.CollectorService.mDb;


/**
 * Created by youngq on 17/3/26.
 */

public class UploadGyro extends TimerTask {
    int count = 0;
    double[] nums2;

    public UploadGyro(double[] array2){
        nums2 = array2;
    }


    @Override
    public void run() {
        count++;
        long temp_time = System.currentTimeMillis();

        ContentValues cv_gyro = new ContentValues();
        cv_gyro.put(GpsContract.GyroscopeEntry.COLUMN_ID,id);
        cv_gyro.put(GpsContract.GyroscopeEntry.COLUMN_TIMESTAMP,temp_time);
        cv_gyro.put(GpsContract.GyroscopeEntry.COLUMN_X, nums2[0]);
        cv_gyro.put(GpsContract.GyroscopeEntry.COLUMN_Y, nums2[1]);
        cv_gyro.put(GpsContract.GyroscopeEntry.COLUMN_Z, nums2[2]);

        try
        {
            mDb.beginTransaction();
            mDb.insert(GpsContract.GyroscopeEntry.TABLE_NAME, null, cv_gyro);
            mDb.setTransactionSuccessful();
            Log.e("***insert gyro***","success!" + count);
        }
        catch (SQLException e) {
            //too bad :(
        }
        finally
        {
            mDb.endTransaction();
        }
    }

}