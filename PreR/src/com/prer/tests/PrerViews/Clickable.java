package com.prer.tests.PrerViews;


import android.view.View;

import com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions;
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

public class Clickable<T> extends PrerView<T> {

    public Clickable(Class<T> type, int resourceId) {
        this.type = type;
        id = resourceId;
    }

    public Clickable(Class<T> type, Matcher<View> selector) {
        this.type = type;
        this.selector = selector;
    }

    public Clickable(Class<T> type, int resourceId, int stringResourceId) {
        this.type = type;
        id = resourceId;
        stringId = stringResourceId;
    }

    public Clickable(Class<T> type, int resourceId, int stringResourceId, String displayText) {
        this.type = type;
        id = resourceId;
        stringId = stringResourceId;
        text = displayText;
    }

    public boolean isChecked() {
        try {
            if (selector == null)
                onView(withId(id)).check(ViewAssertions.matches(ViewMatchers.isChecked()));
            else
                onView(selector).check(ViewAssertions.matches(ViewMatchers.isChecked()));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
