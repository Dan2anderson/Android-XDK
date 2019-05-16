package com.layer.xdk.ui.test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import com.layer.xdk.ui.R;
import com.layer.xdk.ui.testactivity.IdentityItemTestActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class IdentityItemTest {

    @Rule
    public ActivityTestRule<IdentityItemTestActivity> mActivityTestRule =
            new ActivityTestRule<>(IdentityItemTestActivity.class);

    @Before
    public void setup() {
        final IdentityItemTestActivity identityTestActivity = mActivityTestRule.getActivity();
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                identityTestActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        identityTestActivity.runOnUiThread(wakeUpDevice);
    }

    @Test
    public void testViewIsDisplayed() {
        onView(withId(R.id.title)).check(matches(isDisplayed()));
        onView(withId(R.id.right_accessory_text)).check(matches(isDisplayed()));
        onView(withId(R.id.avatar)).check(matches(isDisplayed()));
    }
}
