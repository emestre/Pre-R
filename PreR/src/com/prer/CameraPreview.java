package com.prer;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class. */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	
    private static final String TAG = "CameraPreview";
	private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Parameters mParams;

    /** A standard default constructor, provided to suppress warning. */
    public CameraPreview(Context context) {
    	super(context);
    }
    
    @SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera) {
        super(context);
        
        mCamera = camera;
        mParams = mCamera.getParameters();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } 
        catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } 
        catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or reformatting changes here

        // rotate camera 90 degrees to portrait
        mCamera.setDisplayOrientation(90);
        // rotate the image file so it's oriented correctly (portrait) when viewed in photo viewer
        mParams.setRotation(90);
        
        // set camera preview to auto focus if available
        List<String> focusModes = mParams.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
          // auto focus mode is supported
        	mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        
        
        
//        List<Camera.Size> previewSizes = mParams.getSupportedPreviewSizes();
//        for (Camera.Size s : previewSizes) {
//        	Log.d(TAG, "height: " + s.height + " width: " + s.width);
//        }
//        Camera.Size realSize = mParams.getPreviewSize();
//        Log.d(TAG, "using preview size - height: " + realSize.height + " width: " + realSize.width);
//        realSize = mParams.getPictureSize();
//        Log.d(TAG, "using picture size - height: " + realSize.height + " width: " + realSize.width);
        
        
        
        // update the camera object parameters
        mCamera.setParameters(mParams);

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } 
        catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}