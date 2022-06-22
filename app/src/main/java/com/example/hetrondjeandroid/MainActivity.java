package com.example.hetrondjeandroid;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.hetrondjeandroid.databinding.ActivityMainBinding;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback {

    private final String TAG = this.getClass().getSimpleName();
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Camera mCamera;
    private CameraPreview mPreview;
    private Button mSwitchCamera;
    private int currentCamId;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private Button captureButton ;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Button button;
    private Button bRecord;
    private EditText etUrl;
    private RtmpCamera1 rtmpCamera1;
    private String currentDateAndTime = "";
    private File folder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        checkPermission();
//        Intent intent = new Intent(this, SimpleRtmpActivity.class);
//        startActivity(intent);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        folder = PathUtils.getRecordPath();
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        folder = new File(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        button = findViewById(R.id.b_start_stop);
        button.setOnClickListener(this);
        bRecord = findViewById(R.id.b_record);
        bRecord.setOnClickListener(this);
        Button switchCamera = findViewById(R.id.switch_camera);
        switchCamera.setOnClickListener(this);
        etUrl = findViewById(R.id.et_rtp_url);
        etUrl.setText(R.string.hint_rtmp);
        rtmpCamera1 = new RtmpCamera1(surfaceView, this);
        rtmpCamera1.setReTries(10);
        surfaceView.getHolder().addCallback(this);


        // Create an instance of Camera
//        mCamera = getCameraInstance();
//        currentCamId =  0;
//        mSwitchCamera = findViewById(R.id.button_switch);
//        mSwitchCamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                switchCamera();
//            }
//        });
//        //        // Add a listener to the Capture button
//        captureButton = (Button) findViewById(R.id.button_capture);
//        captureButton.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        recordVideo();
//                    }
//                }
//        );
//
//        // Create our Preview view and set it as the content of our activity.
//        mPreview = new CameraPreview(this, mCamera);
//        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
//        preview.addView(mPreview);
    }



    @Override
    public void onConnectionStartedRtmp(String rtmpUrl) {
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionFailedRtmp(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rtmpCamera1.reTry(5000, reason, null)) {
                    Toast.makeText(MainActivity.this, "Retry", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(MainActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
                            .show();
                    rtmpCamera1.stopStream();
                    button.setText(R.string.start_button);
                }
            }
        });
    }

    @Override
    public void onNewBitrateRtmp(final long bitrate) {

    }

    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthErrorRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
                rtmpCamera1.stopStream();
                button.setText(R.string.start_button);
            }
        });
    }

    @Override
    public void onAuthSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b_start_stop:
                if (!rtmpCamera1.isStreaming()) {
                    if (rtmpCamera1.isRecording()
                            || rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                        button.setText(R.string.stop_button);
                        rtmpCamera1.startStream(etUrl.getText().toString());
                    } else {
                        Toast.makeText(this, "Error preparing stream, This device cant do it",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    button.setText(R.string.start_button);
                    rtmpCamera1.stopStream();
                }
                break;
            case R.id.switch_camera:
                try {
                    rtmpCamera1.switchCamera();
                } catch (CameraOpenException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.b_record:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    if (!rtmpCamera1.isRecording()) {
                        try {
                            if (!folder.exists()) {
                                folder.mkdir();
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                            currentDateAndTime = sdf.format(new Date());
                            if (!rtmpCamera1.isStreaming()) {
                                if (rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                                    rtmpCamera1.startRecord(
                                            folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                                    bRecord.setText(R.string.stop_record);
                                    Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Error preparing stream, This device cant do it",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                rtmpCamera1.startRecord(
                                        folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                                bRecord.setText(R.string.stop_record);
                                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            rtmpCamera1.stopRecord();
//                            PathUtils.updateGallery(this, folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                            bRecord.setText(R.string.start_record);
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        rtmpCamera1.stopRecord();
//                        PathUtils.updateGallery(this, folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                        bRecord.setText(R.string.start_record);
                        Toast.makeText(this,
                                "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        rtmpCamera1.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
            rtmpCamera1.stopRecord();
//            PathUtils.updateGallery(this, folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
            bRecord.setText(R.string.start_record);
            Toast.makeText(this,
                    "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
            currentDateAndTime = "";
        }
        if (rtmpCamera1.isStreaming()) {
            rtmpCamera1.stopStream();
            button.setText(getResources().getString(R.string.start_button));
        }
        rtmpCamera1.stopPreview();
    }


















    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void switchCamera() {
        mCamera.release();
        if (currentCamId == 0) {
            currentCamId = 1;
        } else {
            currentCamId = 0;
        }
        mCamera = Camera.open(currentCamId);
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {




            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,}, 1);
            }
        }
    }




    private boolean prepareVideoRecorder(){
        String state = Environment.getExternalStorageState();
        Log.d(TAG, state);
        mediaRecorder= new MediaRecorder();
        mCamera = getCameraInstance();

        //Output settings:
        Context ctx = this.getApplicationContext();
        File audioDir = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AudioMemos");
        audioDir.mkdirs();
        String audioDirPath = audioDir.getAbsolutePath();
        Log.d(TAG, "Recording file location: " + audioDirPath);

        Date currentTime = Calendar.getInstance().getTime(); // current time
        String curTimeStr = currentTime.toString().replace(" ", "_");

        File recordingFile = new File(audioDirPath + "/" + curTimeStr + ".m4a");
        Log.d(TAG, "Created file: " + recordingFile.getName());


        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        // Step 3: Set output format and encoding (for versions prior to API Level 8)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

//        // Step 4: Set output file
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
//        mediaRecorder.setOutputFile(recordingFile.getAbsolutePath());

        mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("TAG", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("TAG", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public void recordVideo(){
        if (isRecording) {
            // stop recording and release camera
            mediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            captureButton.setText("Start");
            isRecording = false;
        } else {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mediaRecorder.start();

                // inform the user that recording has started
                captureButton.setText("Stop");
                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }







    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

}