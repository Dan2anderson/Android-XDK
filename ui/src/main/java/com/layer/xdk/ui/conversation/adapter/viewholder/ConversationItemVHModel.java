package com.layer.xdk.ui.conversation.adapter.viewholder;

import android.graphics.Color;
import android.view.View;
import androidx.databinding.Bindable;

import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.conversation.ConversationItemFormatter;
import com.layer.xdk.ui.conversation.adapter.ConversationItemModel;
import com.layer.xdk.ui.fourpartitem.adapter.viewholder.FourPartItemVHModel;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class ConversationItemVHModel extends FourPartItemVHModel<ConversationItemModel> {
    //View Logic
    protected ConversationItemFormatter mConversationItemFormatter;

    // View Data
    protected Set<Identity> mParticipantsMinusAuthenticatedUser;

    @Inject
    public ConversationItemVHModel(IdentityFormatter identityFormatter,
            ImageCacheWrapper imageCacheWrapper,
            ConversationItemFormatter conversationItemFormatter) {

        super(identityFormatter, imageCacheWrapper);
        mConversationItemFormatter = conversationItemFormatter;
        mParticipantsMinusAuthenticatedUser = new HashSet<>();
    }

    @Bindable
    public String getTitle() {
        return mConversationItemFormatter.getConversationTitle(getItem().getConversation(), getItem().getParticipants());
    }

    @Bindable
    public String getSubtitle() {
        return mConversationItemFormatter.getLastMessagePreview(getItem().getConversation(), getItem().getLastMessageModel());
    }

    @Override
    public String getAccessoryText() {
        return mConversationItemFormatter.getTimeStamp(getItem().getConversation());
    }

    @Override
    public boolean isSecondaryState() {
        return getItem().getConversation().getTotalUnreadMessageCount() > 0;
    }

    @Override
    public Set<Identity> getIdentities() {
        return getItem().getParticipantsMinusAuthenticatedUser();
    }

//    public class DeselecterOnClickListener implements View.OnClickListener {
//        private View mCurrentSelectedView;
//        @Override
//        public void onClick(View view) {
//            int transparent = Color.argb(0,0,0,0);
//            if(mCurrentSelectedView != null) mCurrentSelectedView.setBackgroundColor(transparent);
//            mCurrentSelectedView = view;
//            int color = Color.argb(255,220,220,220);
//            view.setBackgroundColor(color);
//        }
//    }
}
