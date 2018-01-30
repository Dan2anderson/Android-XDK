package com.layer.ui.message.receipt;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.R;
import com.layer.ui.message.location.LocationMessageModel;
import com.layer.ui.message.model.MessageModel;
import com.layer.ui.message.product.ProductMessageModel;
import com.layer.ui.message.view.MessageView;
import com.layer.ui.util.json.AndroidFieldNamingStrategy;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReceiptMessageModel extends MessageModel {
    public static final String MIME_TYPE = "application/vnd.layer.receipt+json";
    private static final String ROLE_PRODUCT_ITEM = "product";
    private static final String ROLE_SHIPPING_ADDRESS = "shipping";
    private static final String ROLE_BILLING_ADDRESS = "billing";

    private Gson mGson;
    private ReceiptMessageMetadata mMetadata;

    public ReceiptMessageModel(Context context, LayerClient layerClient) {
        super(context, layerClient);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingStrategy(new AndroidFieldNamingStrategy());
        mGson = gsonBuilder.create();
    }

    @Override
    public Class<? extends MessageView> getRendererType() {
        return ReceiptMessageView.class;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        if (messagePart.equals(getRootMessagePart())) {
            JsonReader reader = new JsonReader(new InputStreamReader(messagePart.getDataStream()));
            mMetadata = mGson.fromJson(reader, ReceiptMessageMetadata.class);
        }
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @Nullable
    @Override
    public String getTitle() {
        return mMetadata != null && mMetadata.getTitle() != null ? mMetadata.getTitle() : "Order Confirmation";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public String getFooter() {
        return null;
    }

    @Override
    public boolean getHasContent() {
        return mMetadata != null;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return getContext().getString(R.string.ui_receipt_message_preview_text);
    }

    @NonNull
    public List<ProductMessageModel> getProductItemModels() {
        List<ProductMessageModel> models = new ArrayList<>();
        for (MessageModel model : getChildMessageModelsWithRole(ROLE_PRODUCT_ITEM)) {
            models.add((ProductMessageModel) model);
        }

        return models;
    }

    @Nullable
    public LocationMessageModel getShippingAddressLocationModel() {
        List<MessageModel> models = getChildMessageModelsWithRole(ROLE_SHIPPING_ADDRESS);
        if (!models.isEmpty()) {
            return (LocationMessageModel) models.get(0);
        }

        return null;
    }

    @Nullable
    public LocationMessageModel getBillingAddressLocationModel() {
        List<MessageModel> models = getChildMessageModelsWithRole(ROLE_BILLING_ADDRESS);
        if (!models.isEmpty()) {
            return (LocationMessageModel) models.get(0);
        }

        return null;
    }

    public ReceiptMessageMetadata getMetadata() {
        return mMetadata;
    }

    public void setMetadata(ReceiptMessageMetadata metadata) {
        mMetadata = metadata;
    }

    @BindingAdapter("app:receiptCostToDisplay")
    public static void displayFormattedCost(TextView textView, ReceiptMessageMetadata metadata) {
        if (metadata == null) return;
        String cost = metadata.getTotalCostToDisplay(textView.getContext());
        if (cost != null) {
            textView.setText(cost);
        }
    }
}
