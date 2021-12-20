package com.truelife.chat.job;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.truelife.chat.job.DailyBackupJob;
import com.truelife.chat.job.DeleteStatusJob;
import com.truelife.chat.job.JobIds;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class FireJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {

//            case JobIds.JOB_TAG_SYNC_CONTACTS:
//                return new SyncContactsDailyJob();

            case com.truelife.chat.job.JobIds.JOB_TAG_DELETE_STATUS:
                return new DeleteStatusJob();


            case JobIds.JOB_TAG_BACKUP_MESSAGES:
                return new DailyBackupJob();

        }
        return null;
    }
}
