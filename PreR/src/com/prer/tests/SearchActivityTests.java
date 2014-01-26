package com.prer.tests;

import android.test.ActivityInstrumentationTestCase2;

import static com.prer.tests.SearchActivityModel.*;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.*;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.*;
import com.prer.SearchActivity;

public class SearchActivityTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	
	@SuppressWarnings("deprecation")
	public SearchActivityTests() {
		// This constructor was deprecated - but we want to support lower API
		// levels.
		super("com.prer.SearchActivity", SearchActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		getActivity();
	}
	
	public void testSearchForProcedure() {
		ZipCodeEditText.typeText("93405");
		SearchButton.click();
		ProcedureEditText.typeText("Ankle");
		ResultsList.isDisplayed();
	}
}
