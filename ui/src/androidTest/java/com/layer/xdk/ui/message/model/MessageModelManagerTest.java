package com.layer.xdk.ui.message.model;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.message.text.TextMessageModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


import static org.junit.Assert.assertNotNull;

public class MessageModelManagerTest {

    private Context mContext;
    private MessageModelManager mMessageModelManager;

    @Mock
    private LayerClient mLayerClient;

    @Before
    public void setup() {
        mContext = InstrumentationRegistry.getTargetContext();
        mMessageModelManager = new MessageModelManager(mContext, mLayerClient);
    }

    @Test
    public void testTextMessageModelRegistration() {
        mMessageModelManager.registerModel("TextMessageModel", TextMessageModel.class);
        assertNotNull(mMessageModelManager.getNewModel("TextMessageModel"));
    }
}