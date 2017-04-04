package com.llu17.youngq.sqlite_gps.upload;

import android.content.ContentValues;
import android.database.SQLException;
import android.util.Log;

import com.llu17.youngq.sqlite_gps.data.GpsContract;

import java.util.TimerTask;

import static com.llu17.youngq.sqlite_gps.CollectorService.id;
import static com.llu17.youngq.sqlite_gps.CollectorService.mDb;

/**
 * Created by youngq on 17/4/3.
 */

public class UploadStep extends TimerTask {
    int count = 0;
    double[] nums3;
    int step = 0;

    public UploadStep(double[] array1){
        nums3 = array1;
    }


    @Override
    public void run() {
        count++;
        long temp_time = System.currentTimeMillis();
        step = (int)nums3[1];
        ContentValues cv_step = new ContentValues();
        cv_step.put(GpsContract.StepEntry.COLUMN_ID,id);
        cv_step.put(GpsContract.StepEntry.COLUMN_TIMESTAMP,temp_time);
        cv_step.put(GpsContract.StepEntry.COLUMN_COUNT, step);

        try
        {
            mDb.beginTransaction();
            mDb.insert(GpsContract.StepEntry.TABLE_NAME, null, cv_step);
            mDb.setTransactionSuccessful();
            Log.e("###insert step###","success!" + count);
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
