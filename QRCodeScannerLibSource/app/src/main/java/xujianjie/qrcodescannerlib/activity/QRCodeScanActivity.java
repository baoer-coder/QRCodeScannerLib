package xujianjie.qrcodescannerlib.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import xujianjie.qrcodescannerlib.R;
import xujianjie.qrcodescannerlibrary.activity.ScannerActivity;

public class QRCodeScanActivity extends ScannerActivity
{
    private TextView back;
    private TextView switchLight;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setBaseContentView(R.layout.activity_qr_code_scan);

        initViews();

        setScanLineColor(Color.parseColor("#00ff00"));

        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        switchLight.setOnClickListener(new View.OnClickListener()
        {
            boolean isLight = false;

            @Override
            public void onClick(View v)
            {
                if (isLight)
                {
                    turnOffLight();
                }
                else
                {
                    turnOnLight();
                }

                isLight = !isLight;
            }
        });
    }

    @Override
    protected void onGetResult(String result, Bitmap bitmap)
    {
        super.onGetResult(result, bitmap);

        Log.e("result=", result);

        restartScan();
    }

    private void initViews()
    {
        switchLight = (TextView) findViewById(R.id.switchLight);
        back = (TextView) findViewById(R.id.back);
    }
}