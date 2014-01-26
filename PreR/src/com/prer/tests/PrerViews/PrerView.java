package com.prer.tests.PrerViews;

import android.view.View;
import com.google.android.apps.common.testing.ui.espresso.action.ViewActions;
import com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions;
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers;
import org.hamcrest.Matcher;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.closeSoftKeyboard;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

abstract class PrerView<T> {

    protected int id;
    protected int stringId;
    protected String text;
    protected Class<T> type;
    protected Matcher<View> selector;

    public T click() {
        if (selector == null)
            onView(withId(id)).perform(ViewActions.click());
        else
            onView(selector).perform(ViewActions.click());
        return returnGeneric();
    }
    
    public T pressBack() {
    	onView(withId(id)).perform(ViewActions.pressBack());
    	return returnGeneric();
    }

    public T isDisplayed() {
        onView(withId(id)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        return returnGeneric();
    }

    public T withText(int stringId) {
        if (selector == null)
            onView(withId(id)).check(ViewAssertions.matches(ViewMatchers.withText(stringId)));
        else
            onView(selector).check(ViewAssertions.matches(ViewMatchers.withText(stringId)));
        return returnGeneric();
    }

    public T withText(String string) {
        onView(withId(id)).check(ViewAssertions.matches(ViewMatchers.withText(string)));
        return returnGeneric();
    }

    public T closeKeyboard() {
        onView(withId(id)).perform(closeSoftKeyboard());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return returnGeneric();
    }

    protected T returnGeneric() {
        try {
            return type.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public T pause() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return returnGeneric();
    }

    public T pause(int timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return returnGeneric();
    }
}
