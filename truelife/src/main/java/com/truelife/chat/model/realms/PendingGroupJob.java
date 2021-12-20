package com.truelife.chat.model.realms;

import com.truelife.chat.model.realms.GroupEvent;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class PendingGroupJob extends RealmObject {

    public PendingGroupJob() {
    }

    @PrimaryKey
    private String groupId;
    private int type;
    private com.truelife.chat.model.realms.GroupEvent groupEvent;

    public PendingGroupJob(String groupId, int type , com.truelife.chat.model.realms.GroupEvent groupEvent) {
        this.groupId = groupId;
        this.type = type;
        this.groupEvent = groupEvent;
    }

    public GroupEvent getGroupEvent() {
        return groupEvent;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getType() {
        return type;
    }
}

