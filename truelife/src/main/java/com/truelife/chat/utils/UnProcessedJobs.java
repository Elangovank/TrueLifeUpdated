package com.truelife.chat.utils;

import android.app.job.JobInfo;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.truelife.chat.model.constants.PendingGroupTypes;
import com.truelife.chat.model.realms.Message;
import com.truelife.chat.model.realms.PendingGroupJob;
import com.truelife.chat.model.realms.UnUpdatedStat;
import com.truelife.chat.model.realms.UnUpdatedVoiceMessageStat;
import com.truelife.chat.utils.JobSchedulerSingleton;
import com.truelife.chat.utils.RealmHelper;
import com.truelife.chat.utils.ServiceHelper;

import io.realm.RealmResults;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UnProcessedJobs {

    public static void process(Context context) {


        RealmResults<Message> unProcessedNetworkRequests = com.truelife.chat.utils.RealmHelper.getInstance().getUnProcessedNetworkRequests();
        for (Message unProcessedNetworkRequest : unProcessedNetworkRequests) {
            if (!doesJobExists(unProcessedNetworkRequest.getMessageId(), false)) {
                com.truelife.chat.utils.ServiceHelper.startNetworkRequest(context, unProcessedNetworkRequest.getMessageId(), unProcessedNetworkRequest.getChatId());

            }
        }

        for (UnUpdatedVoiceMessageStat unUpdatedVoiceMessageStat : com.truelife.chat.utils.RealmHelper.getInstance().getUnUpdatedVoiceMessageStat()) {
            if (!doesJobExists(unUpdatedVoiceMessageStat.getMessageId(), true)) {
                com.truelife.chat.utils.ServiceHelper.startUpdateVoiceMessageStatRequest(context, unUpdatedVoiceMessageStat.getMessageId(), null, unUpdatedVoiceMessageStat.getMyUid());
            }
        }

        for (UnUpdatedStat unUpdatedStat : com.truelife.chat.utils.RealmHelper.getInstance().getUnUpdateMessageStat()) {

            if (!doesJobExists(unUpdatedStat.getMessageId(), false)) {
                com.truelife.chat.utils.ServiceHelper.startUpdateMessageStatRequest(context, unUpdatedStat.getMessageId(), unUpdatedStat.getMyUid(), null, unUpdatedStat.getStatToBeUpdated());
            }

        }

        for (PendingGroupJob pendingGroupJob : com.truelife.chat.utils.RealmHelper.getInstance().getPendingGroupCreationJobs()) {
            String groupId = pendingGroupJob.getGroupId();
            if (!doesJobExists(groupId, false)) {
                if (pendingGroupJob.getType() == PendingGroupTypes.CHANGE_EVENT) {
                    com.truelife.chat.utils.ServiceHelper.updateGroupInfo(context, pendingGroupJob.getGroupId(), pendingGroupJob.getGroupEvent());
                } else {
                    ServiceHelper.fetchAndCreateGroup(context, groupId);
                }
            }
        }

    }

    private static boolean doesJobExists(String id, boolean isVoiceMessage) {
        int jobId = RealmHelper.getInstance().getJobId(id, isVoiceMessage);
        if (jobId == -1)
            return false;
        for (JobInfo jobInfo : JobSchedulerSingleton.getInstance().getAllPendingJobs()) {
            if (jobInfo.getId() == jobId)
                return true;
        }
        return false;
    }

}
