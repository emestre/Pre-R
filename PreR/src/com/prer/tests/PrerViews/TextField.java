package com.prer.tests.PrerViews;

import com.google.android.apps.common.testing.ui.espresso.action.ViewActions;
import com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions;


import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

public class TextField<T> extends PrerView<T> {

    public TextField(Class<T> type, int resourceId) {
        this.type = type;
        id = resourceId;
    }

    public TextField(Class<T> type, int resourceId, int stringResourceId) {
        this.type = type;
        id = resourceId;
        stringId = stringResourceId;
    }

    public TextField(Class<T> type, int resourceId, int stringResourceId, String displayText) {
        this.type = type;
        id = resourceId;
        stringId = stringResourceId;
        text = displayText;
    }

    public T typeText(String toType) {
        onView(withId(id)).perform(ViewActions.typeText(toType));
        return returnGeneric();
    }

    public T clearText(){
        onView(withId(id)).perform(ViewActions.clearText());
        return returnGeneric();
    }

    public T checkHintText(int stringId){
        onView(withId(id)).check(ViewAssertions.matches(CustomViewMatchers.withHintText(stringId)));
        return returnGeneric();
    }
}
