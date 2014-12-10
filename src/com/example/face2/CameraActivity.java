package com.example.face2;

import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraActivity extends Activity
implements SurfaceHolder.Callback {

public static final String TAG = CameraActivity.class.getSimpleName();

private Camera mCamera;

// We need the phone orientation to correctly draw the overlay:
private int mOrientation;
private int mOrientationCompensation;
private OrientationEventListener mOrientationEventListener;

// Let's keep track of the display rotation and orientation also:
private int mDisplayRotation;
private int mDisplayOrientation;

// Holds the Face Detection result:
private Camera.Face[] mFaces;

// The surface view for the camera data
private SurfaceView mView;

// Draw rectangles and other fancy stuff:
private FaceOverlayView mFaceView;

/**
* Sets the faces for the overlay view, so it can be updated
* and the face overlays will be drawn again.
*/
private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
@Override
public void onFaceDetection(Face[] faces, Camera camera) {
    Log.d("onFaceDetection", "Number of Faces:" + faces.length);
    // Update the view now!
    mFaceView.setFaces(faces);
}
};

@Override
public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
mView = new SurfaceView(this);

setContentView(mView);
// Now create the OverlayView:
mFaceView = new FaceOverlayView(this);
addContentView(mFaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
// Create and Start the OrientationListener:
mOrientationEventListener = new SimpleOrientationEventListener(this);
mOrientationEventListener.enable();
}

@Override
protected void onPostCreate(Bundle savedInstanceState) {
super.onPostCreate(savedInstanceState);
SurfaceHolder holder = mView.getHolder();
holder.addCallback(this);
}

@Override
protected void onPause() {
mOrientationEventListener.disable();
super.onPause();
}

@Override
protected void onResume() {
mOrientationEventListener.enable();
super.onResume();
}

@Override
public void surfaceCreated(SurfaceHolder surfaceHolder) {
mCamera = Camera.open();
mCamera.setFaceDetectionListener(faceDetectionListener);
mCamera.startFaceDetection();
try {
    mCamera.setPreviewDisplay(surfaceHolder);
} catch (Exception e) {
    Log.e(TAG, "Could not preview the image.", e);
}
}


@Override
public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
// We have no surface, return immediately:
if (surfaceHolder.getSurface() == null) {
    return;
}
// Try to stop the current preview:
try {
    mCamera.stopPreview();
} catch (Exception e) {
    // Ignore...
}
// Get the supported preview sizes:
Camera.Parameters parameters = mCamera.getParameters();
List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
Camera.Size previewSize = previewSizes.get(0);
// And set them:
parameters.setPreviewSize(previewSize.width, previewSize.height);
mCamera.setParameters(parameters);
// Now set the display orientation for the camera. Can we do this differently?
mDisplayRotation = Util.getDisplayRotation(CameraActivity.this);
mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, 0);
mCamera.setDisplayOrientation(mDisplayOrientation);

if (mFaceView != null) {
    mFaceView.setDisplayOrientation(mDisplayOrientation);
}

// Finally start the camera preview again:
mCamera.startPreview();
}

@Override
public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
mCamera.setPreviewCallback(null);
mCamera.setFaceDetectionListener(null);
mCamera.setErrorCallback(null);
mCamera.release();
mCamera = null;
}

/**
* We need to react on OrientationEvents to rotate the screen and
* update the views.
*/
private class SimpleOrientationEventListener extends OrientationEventListener {

public SimpleOrientationEventListener(Context context) {
    super(context, SensorManager.SENSOR_DELAY_NORMAL);
}

@Override
public void onOrientationChanged(int orientation) {
    // We keep the last known orientation. So if the user first orient
    // the camera then point the camera to floor or sky, we still have
    // the correct orientation.
    if (orientation == ORIENTATION_UNKNOWN) return;
    mOrientation = Util.roundOrientation(orientation, mOrientation);
    // When the screen is unlocked, display rotation may change. Always
    // calculate the up-to-date orientationCompensation.
    int orientationCompensation = mOrientation
            + Util.getDisplayRotation(CameraActivity.this);
    if (mOrientationCompensation != orientationCompensation) {
        mOrientationCompensation = orientationCompensation;
        mFaceView.setOrientation(mOrientationCompensation);
    }
}
}
}
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//
//public class CameraActivity extends Activity {
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_camera);
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.camera, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//}
