package com.layer.xdk.ui.message.adapter.decoration;


import android.content.Context;

import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.adapter.MessageGrouping;

import java.util.EnumSet;

public class SubGroupStartItemDecoration extends MessageGroupingItemDecoration {

    public SubGroupStartItemDecoration(Context context) {
        super(context.getResources().getDimensionPixelSize(
                R.dimen.xdk_ui_sub_group_start_item_decoration_height));
    }

    @Override
    boolean shouldDraw(EnumSet<MessageGrouping> groupings) {
        return groupings.contains(MessageGrouping.SUB_GROUP_START)
                && !groupings.contains(MessageGrouping.GROUP_START);
    }
}
