package com.prer.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import com.prer.HomeScreenActivity;

import static com.prer.tests.SearchActivityModel.*;
import static com.prer.tests.HomeScreenActivityModel.*;
import static com.prer.tests.CameraActivityModel.*;

@LargeTest
public class HomeScreenActivityTests extends
		ActivityInstrumentationTestCase2<HomeScreenActivity> {

	@SuppressWarnings("deprecation")
	public HomeScreenActivityTests() {
		// This constructor was deprecated - but we want to support lower API
		// levels.
		super("com.prer.HomeScreenActivity", HomeScreenActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		getActivity();
	}

	public void testButtonsDisplayed() {
		HomeSearchButton.isDisplayed();
		CameraAPIButton.isDisplayed();
		CameraIntentButton.isDisplayed();
	}

	public void testSearchButton() {
		HomeSearchButton.click();
		ZipCodeEditText.isDisplayed();
	}
	
	public void testCameraAPIButton() {
		CameraAPIButton.click();
		CaptureButton.isDisplayed();
	}
}
