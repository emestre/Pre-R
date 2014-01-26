package com.prer.tests.PrerViews;

import android.widget.EditText;
import android.widget.ImageView;
import com.google.android.apps.common.testing.ui.espresso.matcher.BoundedMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static com.google.android.apps.common.testing.testrunner.util.Checks.checkNotNull;

public class CustomViewMatchers {

    public static Matcher<Object> withDrawableRes(int drawableId) {
        checkNotNull(drawableId);
        return withDrawable(drawableId);
    }

    private static Matcher<Object> withDrawable(final int drawableId) {
        return new BoundedMatcher<Object, ImageView>(ImageView.class) {
            @Override
            public boolean matchesSafely(ImageView image) {
                return image.getResources().getDrawable(drawableId).getConstantState().equals(image.getDrawable().getConstantState());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with drawable ");
                description.appendText(drawableId + "");
            }
        };
    }

    public static Matcher<Object> withHintText(int stringId) {
        checkNotNull(stringId);
        return withHint(stringId);
    }

    private static Matcher<Object> withHint(final int stringId) {
        return new BoundedMatcher<Object, EditText>(EditText.class) {
            @Override
            public boolean matchesSafely(EditText hint) {
                String expectedText = hint.getResources().getString(stringId);
                return expectedText.equals(hint.getHint().toString());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with hint ");
                description.appendText(stringId + "");
            }
        };
    }
}
