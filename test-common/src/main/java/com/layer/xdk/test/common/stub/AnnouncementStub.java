package com.layer.xdk.test.common.stub;

import android.net.Uri;
import androidx.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Announcement;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.MessageOptions;
import com.layer.sdk.messaging.MessagePart;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnnouncementStub implements Announcement {
    public Set<MessagePart> mMessageParts = new HashSet<>();


    @Override
    public void delete(LayerClient.DeletionMode deletionMode) {

    }

    @Override
    public void markAsRead() {

    }

    @Override
    public Uri getId() {
        return null;
    }

    @Override
    public long getPosition() {
        return 0;
    }

    @Override
    public Conversation getConversation() {
        return null;
    }

    @Override
    public Set<MessagePart> getMessageParts() {
        return mMessageParts;
    }

    @Override
    public boolean isSent() {
        return false;
    }

    @Override
    public boolean isDeleted() {
        return false;
    }

    @Override
    public Date getSentAt() {
        return null;
    }

    @Override
    public Date getReceivedAt() {
        return null;
    }

    @Nullable
    @Override
    public Date getUpdatedAt() {
        return null;
    }

    @Nullable
    @Override
    public Identity getSender() {
        return null;
    }

    @Override
    public Map<Identity, RecipientStatus> getRecipientStatus() {
        return null;
    }

    @Override
    public RecipientStatus getRecipientStatus(Identity identity) {
        return null;
    }

    @Override
    public MessageOptions getOptions() {
        return null;
    }

    @Override
    public void putLocalData(@Nullable byte[] bytes) {

    }

    @Nullable
    @Override
    public byte[] getLocalData() {
        return new byte[0];
    }
}
