package xujianjie.qrcodescannerlibrary.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Vector;

import xujianjie.qrcodescannerlibrary.R;
import xujianjie.qrcodescannerlibrary.qrcode.CameraManager;
import xujianjie.qrcodescannerlibrary.qrcode.InactivityTimer;
import xujianjie.qrcodescannerlibrary.qrcode.ScannerHandler;
import xujianjie.qrcodescannerlibrary.qrcode.ViewfinderView;

public class ScannerActivity extends AppCompatActivity implements Callback
{
    private LinearLayout linearLayout_content;

    private View view_statusBar;
    private ViewfinderView viewfinderView;

    private SurfaceHolder surfaceHolder;
    private ScannerHandler scannerHandler;

    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;

    private boolean hasSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        iniViews();

        view_statusBar.setLayoutParams(new LinearLayout.LayoutParams(-1, getStatusBarHeight()));
        view_statusBar.setBackgroundColor(Color.parseColor("#00000000"));

        CameraManager.init(getApplication());

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    protected void setBaseContentView(int layoutId)
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutId, null);

        if (view != null)
        {
            linearLayout_content.addView(view);
        }
    }

    protected void setScanLineColor(int color)
    {
        viewfinderView.setScanLineColor(color);
    }

    // 扫描结果
    public void handleDecode(final Result result, Bitmap bitmap)
    {
        inactivityTimer.onActivity();
        Vibrate();

        onGetResult(result.getText(), bitmap);
    }

    protected void onGetResult(String result, Bitmap bitmap)
    {

    }

    protected void restartScan()
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                initCamera(surfaceHolder);
                if (scannerHandler != null)
                {
                    scannerHandler.restartPreviewAndDecode();
                }
            }
        }, 500);
    }

    protected void turnOnLight()
    {
        CameraManager.get().turnOn();
    }

    protected void turnOffLight()
    {
        CameraManager.get().turnOff();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        if (surfaceView != null)
        {
            surfaceHolder = surfaceView.getHolder();
            if (hasSurface)
            {
                initCamera(surfaceHolder);
            }
            else
            {
                surfaceHolder.addCallback(this);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
            decodeFormats = null;
            characterSet = null;
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (scannerHandler != null)
        {
            scannerHandler.quitSynchronously();
            scannerHandler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy()
    {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private void initCamera(SurfaceHolder surfaceHolder)
    {
        try
        {
            CameraManager.get().openDriver(surfaceHolder);
        }
        catch (IOException | RuntimeException ioe)
        {
            return;
        }
        if (scannerHandler == null)
        {
            scannerHandler = new ScannerHandler(ScannerActivity.this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        if (!hasSurface)
        {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView()
    {
        return viewfinderView;
    }

    public Handler getHandler()
    {
        return scannerHandler;
    }

    public void drawViewfinder()
    {
        viewfinderView.drawViewfinder();
    }

    private void Vibrate()
    {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    //获取状态栏高度
    private int getStatusBarHeight()
    {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
        {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void iniViews()
    {
        linearLayout_content = (LinearLayout) findViewById(R.id.linearLayout_content);

        view_statusBar = findViewById(R.id.view_statusBar);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinderView);
    }
}