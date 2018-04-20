package xujianjie.qrcodescannerlib.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import xujianjie.qrcodescannerlib.utils.PermissionApplyUtil;
import xujianjie.qrcodescannerlib.R;

public class MainActivity extends BaseActivity
{
    private Button button_scanQRCode;
    private PermissionApplyUtil permissionApplyUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setImmersiveStatusBar(R.id.view_statusBar, Color.parseColor("#66a5ff"));

        initViews();

        button_scanQRCode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                List<PermissionApplyUtil.PermissionData> permissionList = new ArrayList<>();
                permissionList.add(new PermissionApplyUtil.PermissionData(Manifest.permission.CAMERA, "相机"));
                permissionList.add(new PermissionApplyUtil.PermissionData(Manifest.permission.FLASHLIGHT, "闪光灯"));

                permissionApplyUtil = new PermissionApplyUtil(MainActivity.this, permissionList);
                permissionApplyUtil.setOnPermissionRequestResultListener(new PermissionApplyUtil.OnPermissionRequestResultListener()
                {
                    @Override
                    public void permissionRequestResult(boolean allGranted)
                    {
                        if (allGranted)
                        {
                            Intent intent = new Intent(MainActivity.this, QRCodeScanActivity.class);
                            startActivity(intent);
                        }
                    }
                });
                permissionApplyUtil.apply();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissionApplyUtil != null)
        {
            permissionApplyUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initViews()
    {
        button_scanQRCode = (Button) findViewById(R.id.button_scanQRCode);
    }
}