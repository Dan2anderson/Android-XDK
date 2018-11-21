/*
 * Copyright (c) 2018 SalesRabbit, Inc. All rights reserved.
 */

package com.layer.xdk.ui.conversation.adapter.viewholder;

import android.graphics.Color;
import android.view.View;

public class SelectorOnClickListener implements View.OnClickListener {
    private static SelectorOnClickListener INSTANCE;
    private View mCurrentSelectedView;

    private SelectorOnClickListener(){}

    @Override
    public void onClick(View view) {
        int transparent = Color.argb(0,0,0,0);
        if(mCurrentSelectedView != null) mCurrentSelectedView.setBackgroundColor(transparent);
        mCurrentSelectedView = view;
        int color = Color.argb(255,220,220,220);
        view.setBackgroundColor(color);
    }

    public static SelectorOnClickListener getInstance() {
        if(INSTANCE == null) INSTANCE = new SelectorOnClickListener();
        return INSTANCE;
    }
}
