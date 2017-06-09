package com.lac.pucrio.luizpitta.iotrade;

/**
 * Created by luizg on 08/06/2017.
 */

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.Menu;

import com.lac.pucrio.luizpitta.iotrade.Activities.MenuActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ChangeTextBehaviorTest {

    private String mStringToBetyped;

    @Rule
    public ActivityTestRule<MenuActivity> mActivityRule = new ActivityTestRule<>(
            MenuActivity.class);

    @Before
    public void initValidString() {
        // Specify a valid string.
        mStringToBetyped = "2.0";
    }

    @Test
    public void changeText_sameActivity() {
        // Type text and then press the button.
        onView(withId(R.id.value))
                .perform(typeText(mStringToBetyped), closeSoftKeyboard());
        onView(withId(R.id.value)).perform(click());
        // Check that the text was changed.
        onView(withId(R.id.value))
                .check(matches(withText(mStringToBetyped)));
    }
}