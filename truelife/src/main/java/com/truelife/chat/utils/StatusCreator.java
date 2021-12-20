package com.truelife.chat.utils;

import com.truelife.TLApplication;
import com.truelife.chat.model.realms.TextStatus;
import com.truelife.chat.model.constants.StatusType;
import com.truelife.chat.model.realms.Status;
import com.truelife.chat.utils.network.FireManager;

import java.util.Date;

public class StatusCreator {
    public static Status createImageStatus(String imagePath) {
        String statusId = com.truelife.chat.utils.FireConstants.getMyStatusRef(StatusType.IMAGE).push().getKey();
        String thumbImg = com.truelife.chat.utils.BitmapUtils.decodeImage(imagePath, false);
        Status status = new Status(statusId, FireManager.getUid(), new Date().getTime(), thumbImg, null, imagePath, StatusType.IMAGE);
        com.truelife.chat.utils.RealmHelper.getInstance().saveObjectToRealm(status);
        return status;
    }

    public static Status createVideoStatus(String videoPath) {
        String statusId = com.truelife.chat.utils.FireConstants.getMyStatusRef(StatusType.VIDEO).push().getKey();
        String thumbImg = BitmapUtils.generateVideoThumbAsBase64(videoPath);
        long mediaLengthInMillis = Util.getMediaLengthInMillis(TLApplication.Companion.context(), videoPath);
        Status status = new Status(statusId, FireManager.getUid(), new Date().getTime(), thumbImg, null, videoPath, StatusType.VIDEO, mediaLengthInMillis);
        com.truelife.chat.utils.RealmHelper.getInstance().saveObjectToRealm(status);
        return status;
    }

    public static Status createTextStatus(TextStatus textStatus) {
        String statusId = FireConstants.getMyStatusRef(StatusType.TEXT).push().getKey();
        Status status = new Status(statusId, FireManager.getUid(), new Date().getTime(), textStatus,StatusType.TEXT);
        textStatus.setStatusId(statusId);
        RealmHelper.getInstance().saveObjectToRealm(status);
        return status;
    }
}
