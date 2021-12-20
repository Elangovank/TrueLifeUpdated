package com.truelife.chat.utils;

import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.truelife.TLApplication;

public class JobSchedulerSingleton {
    private static JobScheduler jobScheduler;

    private JobSchedulerSingleton() {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static JobScheduler getInstance(){
        if (jobScheduler == null){
            jobScheduler = (JobScheduler) TLApplication.Companion.context().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        return jobScheduler;
    }
}
