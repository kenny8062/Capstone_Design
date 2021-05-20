package com.example.bgcamera;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class CamService extends Service {

    //UI
    private WindowManager wm = null;
    private View rootview = null;
    private TextureView textureView = null;

    //Camera2-related stuff
    private CameraManager cameraManager = null;
    private Size previewSize = null;
    private CameraDevice cameraDevice = null;
    private CaptureRequest captureRequest = null;
    private CameraCaptureSession captureSession = null;
    private ImageReader imageReader = null;

    private boolean shouldShowPreview = true;

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(CameraCaptureSession session,
                                        CaptureRequest request, CaptureResult result){};
        @Override
        public void onCaptureCompleted(CameraCaptureSession session,
                                       CaptureRequest request, TotalCaptureResult result) {
        }
    };

    final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            initCam(width,height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image=reader.acquireLatestImage();
            Log.d(TAG,"Got image: " + " x ");
            if(image!=null)
                image.close();
        }
    };
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCaptureSession();
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            cameraDevice=null;
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onError( CameraDevice camera, int error) {
            camera.close();
            cameraDevice=null;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        switch (intent.getAction()){
            case ACTION_START:
                System.out.println("액션스타트로 넘어옴");
                start();
            case ACTION_START_WITH_PREVIEW:
                System.out.println("액션스타트 프리뷰로 넘어옴");
                startWithPreview();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG, "oncreate");
        startForeground();
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public void onDestroy(){
        super.onDestroy();
        System.out.println(1122);
        stopCamera();
        if(rootview != null) wm.removeView(rootview);
        sendBroadcast(new Intent(ACTION_STOPPED));
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void start() {

        shouldShowPreview = false;

        initCam(320, 200);
        System.out.println("initCam까지 실행");
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void startWithPreview(){
        shouldShowPreview = true;

        initOverlay();

        if(textureView.isAvailable()) initCam(textureView.getWidth(),textureView.getHeight());
        else textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    private void initOverlay(){
        System.out.println(22);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootview=li.inflate(R.layout.overlay,null);
        textureView = rootview.findViewById(R.id.texPreview);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
          LAYOUT_FLAG,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        wm= (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        System.out.println("요기");
        wm.addView(rootview,params);
        System.out.println("요기2");
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void initCam(int width,int height) {
        System.out.println(2);
        cameraManager= (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String camId=null;

        try {
            for(String id: cameraManager.getCameraIdList()){
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(facing ==CameraCharacteristics.LENS_FACING_FRONT){
                    camId=id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        try {
            previewSize=chooseSupportedSize(camId,width,height);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                         int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            cameraManager.openCamera(camId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private Size chooseSupportedSize(String camId,int textureViewWidth,int textureViewHeight) throws CameraAccessException {
        System.out.println(11111);
        CameraManager manager= (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(camId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        System.out.println(11);
        //이 부분 오류날수도있음
        Size[] supportedSizes = map.getOutputSizes(SurfaceTexture.class);

        int texVuewArea = textureViewWidth* textureViewHeight;
        float textVuewAspect=(float)textureViewWidth/(float)textureViewHeight;

        //int nearestTofurthestSz = supportedSizes
        Size size=new Size(320,200);
        return size;
    }
    private void startForeground(){

        System.out.println("인탠트받아옴1");
        Intent intent =new Intent(this,MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.app_name))
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.app_name))
                .build();
        startForeground(ONGOING_NOTIFICATION_ID,notification);

        System.out.println("인탠트받아옴 끝남");
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void createCaptureSession() {
        try{
            System.out.println(111);
            ArrayList<Surface> targetSurfaces= new ArrayList();
            CaptureRequest.Builder requestBuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            if(shouldShowPreview){
                SurfaceTexture texture=textureView.getSurfaceTexture();
                texture.setDefaultBufferSize(previewSize.getWidth(),previewSize.getHeight());
                Surface previewSurface = new Surface(texture);
                targetSurfaces.add(previewSurface);
                requestBuilder.addTarget(previewSurface);
            }
            imageReader= ImageReader.newInstance(previewSize.getWidth(),previewSize.getHeight(), ImageFormat.YUV_420_888,2);
            imageReader.setOnImageAvailableListener(imageAvailableListener,null);

            targetSurfaces.add(imageReader.getSurface());
            requestBuilder.addTarget(imageReader.getSurface());
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            cameraDevice.createCaptureSession(targetSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if(cameraDevice.equals(null)) return;
                    captureSession = session;
                    try {
                        captureRequest= requestBuilder.build();
                        captureSession.setRepeatingRequest(captureRequest,captureCallback,null);

                    }catch (CameraAccessException e){
                        Log.e(TAG,"createCaptureSession",e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG,"createCaptureSession()");
                }
            },null);
        }catch (CameraAccessException e){
            Log.e(TAG,"createCaptureSesstion",e);
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private void stopCamera(){
        try{
            System.out.println(1111);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                captureSession.close();
            }
            captureSession = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cameraDevice.close();
            }
            cameraDevice = null;

            imageReader.close();
            imageReader = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static final  String TAG = "CamService";

    public static final String ACTION_START = "eu.sisik.backgroundcam.action.START";
    public static final  String ACTION_START_WITH_PREVIEW = "eu.sisik.backgroundcam.action.START_WITH_PREVIEW";
    public static final  String ACTION_STOPPED = "eu.sisik.backgroundcam.action.STOPPED";

    public static final  int ONGOING_NOTIFICATION_ID = 6660;
    public static final  String CHANNEL_ID = "cam_service_channel_id";
    public static final String CHANNEL_NAME = "cam_service_channel_name";


}

