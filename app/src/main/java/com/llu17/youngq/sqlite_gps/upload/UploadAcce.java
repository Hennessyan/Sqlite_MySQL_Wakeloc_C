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

public class UploadAcce extends TimerTask {
    int count = 0;
    double[] nums1;

    public UploadAcce(double[] array1){
        nums1 = array1;
    }


    @Override
    public void run() {
        count++;
        long temp_time = System.currentTimeMillis();
        ContentValues cv_acce = new ContentValues();
        cv_acce.put(GpsContract.AccelerometerEntry.COLUMN_ID,id);
        cv_acce.put(GpsContract.AccelerometerEntry.COLUMN_TIMESTAMP,temp_time);
        cv_acce.put(GpsContract.AccelerometerEntry.COLUMN_X, nums1[0]);
        cv_acce.put(GpsContract.AccelerometerEntry.COLUMN_Y, nums1[1]);
        cv_acce.put(GpsContract.AccelerometerEntry.COLUMN_Z, nums1[2]);

        try
        {
            mDb.beginTransaction();
            mDb.insert(GpsContract.AccelerometerEntry.TABLE_NAME, null, cv_acce);
            mDb.setTransactionSuccessful();
            Log.e("===insert acce===","success!" + count);
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