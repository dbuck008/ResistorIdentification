package io.xsor.resistor;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

import java.util.List;

public class CameraView extends JavaCameraView {

    private static final String TAG = "CameraView";

    private Context myreference;
    public CameraView (Context context, AttributeSet attrs) {
        super(context, attrs);
        this.myreference = context;
    }

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }

    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Camera.Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Camera.Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    // Setup the camera
    public void flashOn(boolean isFlashLightON) {
        Camera camera = mCamera;
        if (camera != null) {
            Camera.Parameters params = camera.getParameters();

            if (params != null) {
                if (isFlashLightON) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                } else {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                camera.setParameters(params);
                camera.startPreview();
            }
        }
    }
}