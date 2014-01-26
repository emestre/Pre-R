package com.prer.tests;

import com.prer.R;
import com.prer.tests.PrerViews.Clickable;

public class CameraActivityModel {
	public static Clickable<CameraActivityModel> CaptureButton = new Clickable<CameraActivityModel>(CameraActivityModel.class, R.id.camera_capture_button);
    
}
