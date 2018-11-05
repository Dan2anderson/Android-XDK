package com.layer.xdk.ui.composebar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.content.res.AppCompatResources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.layer.sdk.LayerClient;
import com.layer.sdk.listeners.LayerTypingIndicatorListener;
import com.layer.sdk.messaging.Conversation;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.databinding.XdkUiComposeBarAttachmentMenuItemBinding;
import com.layer.xdk.ui.databinding.XdkUiComposeBarBinding;
import com.layer.xdk.ui.message.sender.AttachmentSender;
import com.layer.xdk.ui.message.sender.MessageSender;
import com.layer.xdk.ui.message.text.RichTextSender;
import com.layer.xdk.ui.message.text.TextSender;
import com.layer.xdk.ui.util.EditTextUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A widget that contains buttons and text entry for use when composing a message. This widget
 * contains the following configurable widgets in a horizontal layout:
 * <br/>
 * L1 L2 L3 L4 EditText R1 R2 R3 R4 Button
 * <br/>
 * where L1-L4 are {@link ImageButton}s to the left of the {@link EditText} and R1-R4 are
 * {@link ImageButton}s to the right of the {@link EditText}. The rightmost {@link Button} is used
 * as the Send button (see {@link #getSendButton()}).
 * </p>
 * <p>
 * By default, the {@link ImageButton} in position L4 is used to add attachments (see
 * {@link #getDefaultAttachButton()}. The {@link EditText} in the middle is used for text entry for
 * the message contents. All of the widgets can be customized by calling the appropriate accessors.
 * </p>
 */
public class ComposeBar extends FrameLayout implements TextWatcher {

    private EditText mEditText;
    private Button mSendButton;

    protected OnFocusChangeListener mExternalEditTextOnFocusChangeListener;

    private ImageButton mLeftButton1;
    private ImageButton mLeftButton2;
    private ImageButton mLeftButton3;
    private ImageButton mDefaultAttachButton;

    private ImageButton mRightButton1;
    private ImageButton mRightButton2;
    private ImageButton mRightButton3;
    private ImageButton mRightButton4;

    private Conversation mConversation;

    private List<AttachmentSender> mAttachmentSenders = new ArrayList<AttachmentSender>();
    private TextSender mTextSender;
    private MessageSender.Callback mMessageSenderCallback;

    private PopupWindow mAttachmentMenu;

    public ComposeBar(Context context) {
        this(context, null);
    }

    public ComposeBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComposeBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        XdkUiComposeBarBinding binding = XdkUiComposeBarBinding.inflate(LayoutInflater.from(getContext()), this, true);

        mEditText = binding.xdkUiComposeBarEditText;
        mSendButton = binding.xdkUiComposeBarSendButton;

        mLeftButton1 = binding.xdkUiComposeBarButtonLeft1;
        mLeftButton2 = binding.xdkUiComposeBarButtonLeft2;
        mLeftButton3 = binding.xdkUiComposeBarButtonLeft3;
        mDefaultAttachButton = binding.xdkUiComposeBarButtonLeft4;

        mRightButton1 = binding.xdkUiComposeBarButtonRight1;
        mRightButton2 = binding.xdkUiComposeBarButtonRight2;
        mRightButton3 = binding.xdkUiComposeBarButtonRight3;
        mRightButton4 = binding.xdkUiComposeBarButtonRight4;

        mEditText.addTextChangedListener(this);

        mSendButton.setEnabled(mEditText.getEditableText().length() > 0);
        mSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mTextSender.requestSend(mEditText.getText().toString())) return;
                mEditText.setText("");
            }
        });

        mDefaultAttachButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!mAttachmentSenders.isEmpty()) {
                    LinearLayout menu = (LinearLayout) mAttachmentMenu.getContentView();
                    menu.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

                    mAttachmentMenu.showAsDropDown(v, 0, -menu.getMeasuredHeight() - v.getHeight());
                }
            }
        });

        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                setEditTextEnabledState(hasFocus);

                if (mExternalEditTextOnFocusChangeListener != null && isEnabled()) {
                    mExternalEditTextOnFocusChangeListener.onFocusChange(view, hasFocus);
                }
            }
        });

        // Attachment Menu

        if (mAttachmentMenu != null) throw new IllegalStateException("Already initialized menu");

        if (attrs == null) {
            mAttachmentMenu = new PopupWindow(context);
        } else {
            mAttachmentMenu = new PopupWindow(context, attrs, defStyleAttr);
        }
        mAttachmentMenu.setContentView(LayoutInflater.from(context).inflate(R.layout.xdk_ui_compose_bar_attachment_menu, null));
        mAttachmentMenu.getContentView().setId(R.id.xdk_ui_attachment_menu);
        mAttachmentMenu.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mAttachmentMenu.setOutsideTouchable(true);

        mAttachmentMenu.setFocusable(true);

        parseAndApplyStyle(context, attrs, defStyleAttr);
    }

    protected void parseAndApplyStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ComposeBar, R.attr.ComposeBar, defStyle);
        setEnabled(ta.getBoolean(R.styleable.ComposeBar_android_enabled, true));

        mEditText.setTextColor(ta.getColor(R.styleable.ComposeBar_android_textColor, context.getResources().getColor(R.color.xdk_ui_compose_bar_text)));

        int textSize = ta.getDimensionPixelSize(R.styleable.ComposeBar_android_textSize, context.getResources().getDimensionPixelSize(R.dimen.xdk_ui_compose_bar_text_size));
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        String typeFaceName = ta.getString(R.styleable.ComposeBar_inputTextTypeface);
        Typeface typeface = typeFaceName != null ? Typeface.create(typeFaceName, ta.getInt(R.styleable.ComposeBar_android_textStyle, Typeface.NORMAL)) : null;
        mEditText.setTypeface(typeface);

        int underlineColor = ta.getColor(R.styleable.ComposeBar_inputUnderlineColor, context.getResources().getColor(R.color.xdk_ui_compose_bar_underline));
        int cursorColor = ta.getColor(R.styleable.ComposeBar_inputCursorColor, context.getResources().getColor(R.color.xdk_ui_compose_bar_cursor));

        EditTextUtil.setCursorDrawableColor(mEditText, cursorColor);
        EditTextUtil.setUnderlineColor(mEditText, underlineColor);

        Drawable attachmentSendersBackground = ta.getDrawable(
                R.styleable.ComposeBar_attachmentSendersBackground);
        if (attachmentSendersBackground == null) {
            attachmentSendersBackground = ContextCompat.getDrawable(context, R.drawable.xdk_ui_popup_background);
        }

        mAttachmentMenu.setBackgroundDrawable(attachmentSendersBackground);

        mEditText.setHint(ta.getString(R.styleable.ComposeBar_android_hint));
        mEditText.setMaxLines(ta.getInt(R.styleable.ComposeBar_android_maxLines, 5));

        ta.recycle();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (mAttachmentSenders.isEmpty()) return superState;
        SavedState savedState = new SavedState(superState);
        for (AttachmentSender sender : mAttachmentSenders) {
            Parcelable parcelable = sender.onSaveInstanceState();
            if (parcelable == null) continue;
            savedState.put(sender.getClass(), parcelable);
        }
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        for (AttachmentSender sender : mAttachmentSenders) {
            Parcelable parcelable = savedState.get(sender.getClass());
            if (parcelable == null) continue;
            sender.onRestoreInstanceState(parcelable);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        setEditTextEnabledState(enabled);

        mLeftButton1.setEnabled(enabled);
        mLeftButton2.setEnabled(enabled);
        mLeftButton3.setEnabled(enabled);
        mDefaultAttachButton.setEnabled(enabled);

        mRightButton1.setEnabled(enabled);
        mRightButton2.setEnabled(enabled);
        mRightButton3.setEnabled(enabled);
        mRightButton4.setEnabled(enabled);

        int color = enabled ? ContextCompat.getColor(this.getContext(), android.R.color.transparent) : ContextCompat.getColor(this.getContext(), R.color.xdk_ui_color_primary_gray_1);
        this.setBackgroundColor(color);
    }

    protected void setEditTextEnabledState(boolean hasFocus) {
        if (isEnabled()) {
            boolean isSendEnabled = mEditText.getText().length() > 0;

            mEditText.setEnabled(hasFocus);
            mEditText.setFocusable(hasFocus);

            mSendButton.setEnabled(isSendEnabled);
        } else {
            mEditText.setEnabled(false);
            mEditText.setFocusable(false);
            mSendButton.setEnabled(false);
        }
    }

    public void setConversation(LayerClient layerClient, Conversation conversation) {
        mConversation = conversation;
        mTextSender = new RichTextSender(getContext(), layerClient);
        mTextSender.setConversation(conversation);
        for (AttachmentSender attachmentSender : mAttachmentSenders) {
            attachmentSender.setConversation(conversation);
        }
    }

    /**
     * Sets the TextSender used for sending composed text messages.
     *
     * @param textSender TextSender used for sending composed text messages.
     */

    public void setTextSender(TextSender textSender) {
        mTextSender = textSender;
        mTextSender.setConversation(mConversation);
    }

    /**
     * Adds AttachmentSenders to this ComposeBar's attachment menu.
     *
     * @param senders AttachmentSenders to add to this ComposeBar's attachment menu.
     */
    public void addAttachmentSendersToDefaultAttachmentButton(AttachmentSender... senders) {
        for (AttachmentSender sender : senders) {
            if (sender.getTitle() == null && sender.getIcon() == null) {
                throw new NullPointerException("Attachment handlers must have at least a mTitle or icon specified.");
            }
            if (mMessageSenderCallback != null) sender.setCallback(mMessageSenderCallback);
            mAttachmentSenders.add(sender);
            addAttachmentMenuItem(sender);
        }
        if (!mAttachmentSenders.isEmpty()) mDefaultAttachButton.setVisibility(View.VISIBLE);
    }

    protected void addAttachmentMenuItem(final AttachmentSender sender) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout menuLayout = (LinearLayout) mAttachmentMenu.getContentView();

        XdkUiComposeBarAttachmentMenuItemBinding binding = XdkUiComposeBarAttachmentMenuItemBinding.inflate(inflater, menuLayout, false);
        binding.setSender(sender);
        binding.getRoot().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAttachmentMenu.dismiss();
                sender.requestSend();
            }
        });
        binding.executePendingBindings();

        if (sender.getIcon() != null) {
            Drawable iconDrawable = AppCompatResources.getDrawable(getContext(), sender.getIcon());
            DrawableCompat.setTint(iconDrawable, getResources().getColor(R.color.xdk_ui_icon_tint));
            binding.title.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null);
        }

        menuLayout.addView(binding.getRoot());
    }

    public void setOnMessageEditTextFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        mExternalEditTextOnFocusChangeListener = onFocusChangeListener;
    }

    public void removeOnMessageEditTextFocusChangeListener() {
        mExternalEditTextOnFocusChangeListener = null;
    }

    /**
     * Must be called from Activity's onActivityResult to allow attachment senders to manage results
     * from e.g. selecting a gallery photo or taking a camera image.
     *
     * @param activity    Activity receiving the result.
     * @param requestCode Request code from the Activity's onActivityResult.
     * @param resultCode  Result code from the Activity's onActivityResult.
     * @param data        Intent data from the Activity's onActivityResult.
     */
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        for (AttachmentSender sender : mAttachmentSenders) {
            sender.onActivityResult(activity, requestCode, resultCode, data);
        }
    }

    /**
     * Must be called from Activity's onRequestPermissionsResult to allow attachment senders to
     * manage dynamic permissions.
     *
     * @param requestCode  The request code passed in requestPermissions(android.app.Activity, String[], int)
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (AttachmentSender sender : mAttachmentSenders) {
            sender.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Sets an optional callback for receiving MessageSender events.  If non-null, overrides any
     * callbacks already set on MessageSenders.
     *
     * @param callback Callback to receive MessageSender events.
     */
    @SuppressWarnings("unused")
    public void setMessageSenderCallback(MessageSender.Callback callback) {
        mMessageSenderCallback = callback;
        if (mMessageSenderCallback == null) return;
        if (mTextSender != null) mTextSender.setCallback(callback);
        for (AttachmentSender sender : mAttachmentSenders) {
            sender.setCallback(callback);
        }
    }

    // TextWatcher
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (mConversation == null || mConversation.isDeleted()) return;
        if (editable.length() > 0) {
            if (!mSendButton.isEnabled()) {
                mSendButton.setEnabled(true);
            }
            mConversation.send(LayerTypingIndicatorListener.TypingIndicator.STARTED);
        } else {
            mSendButton.setEnabled(false);
            mConversation.send(LayerTypingIndicatorListener.TypingIndicator.FINISHED);
        }
    }

    /**
     * @return the {@link EditText} used for text entry for the message content
     */
    public EditText getEditText() {
        return mEditText;
    }

    /**
     * @return the {@link Button} used for sending the message
     */
    public Button getSendButton() {
        return mSendButton;
    }

    /**
     * Given positions incrementing from left to right (1 2 3 4 EditText), returns the button in
     * position 1.
     *
     * @return the {@link ImageButton} in the 1st and leftmost position to the left of the
     * {@link EditText}
     */
    public ImageButton getLeftButton1() {
        return mLeftButton1;
    }

    /**
     * Given positions incrementing from left to right (1 2 3 4 EditText), returns the button in
     * position 2.
     *
     * @return the {@link ImageButton} in the 2nd position to the left of the {@link EditText}
     */
    public ImageButton getLeftButton2() {
        return mLeftButton2;
    }

    /**
     * Given positions incrementing from left to right (1 2 3 4 EditText), returns the button in
     * position 3.
     *
     * @return the {@link ImageButton} in the 3rd position to the left of the {@link EditText}
     */
    public ImageButton getLeftButton3() {
        return mLeftButton3;
    }

    /**
     * Given positions incrementing from left to right (1 2 3 4 EditText), returns the button in
     * position 4. This is also the location of the default attachment button.
     *
     * @return the {@link ImageButton} in the 4th and rightmost position to the left of the
     * {@link EditText}
     */
    public ImageButton getDefaultAttachButton() {
        return mDefaultAttachButton;
    }

    /**
     * Given positions incrementing from left to right (EditText 1 2 3 4), returns the button in
     * position 1.
     *
     * @return the {@link ImageButton} in the 1st and leftmost position to the right of the
     * {@link EditText}
     */
    public ImageButton getRightButton1() {
        return mRightButton1;
    }

    /**
     * Given positions incrementing from left to right (EditText 1 2 3 4), returns the button in
     * position 2.
     *
     * @return the {@link ImageButton} in the 2nd position to the right of the {@link EditText}
     */
    public ImageButton getRightButton2() {
        return mRightButton2;
    }

    /**
     * Given positions incrementing from left to right (EditText 1 2 3 4), returns the button in
     * position 3.
     *
     * @return the {@link ImageButton} in the 3rd position to the right of the {@link EditText}
     */
    public ImageButton getRightButton3() {
        return mRightButton3;
    }

    /**
     * Given positions incrementing from left to right (EditText 1 2 3 4), returns the button in
     * position 4.
     *
     * @return the {@link ImageButton} in the 4th and rightmost position to the right of the
     * {@link EditText}
     */
    public ImageButton getRightButton4() {
        return mRightButton4;
    }

    /**
     * @return the Conversation this ComposeBar is associated with
     */
    public Conversation getConversation() {
        return mConversation;
    }

    /**
     * Saves a map from AttachmentSender class to AttachmentSender saved instance.
     */
    protected static class SavedState extends BaseSavedState {
        Bundle mBundle = new Bundle();

        public SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState put(Class<? extends AttachmentSender> cls, Parcelable parcelable) {
            mBundle.putParcelable(cls.getName(), parcelable);
            return this;
        }

        Parcelable get(Class<? extends AttachmentSender> cls) {
            return mBundle.getParcelable(cls.getName());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(mBundle);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SavedState(Parcel in) {
            super(in);
            mBundle = in.readBundle();
        }
    }

}
