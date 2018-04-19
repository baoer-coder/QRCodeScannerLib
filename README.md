# 扫描二维码和生成二维码

快速使用：
1、在项目的build.gradle中配置maven仓库：
allprojects {
    repositories {
        ....
        maven { url "https://raw.githubusercontent.com/xujianjie12138/QRCodeScannerLib/master" }
        ....
    }
}

2、在app的build.gradle中引用：
dependencies {
    ...
    compile 'xujianjie:qrcodescannerlibrary:1.0.0'
    ...
}

一、二维码扫描：
使用时需要继承ScannerActivity，覆盖onGetResult(final String result, Bitmap bitmap)即可获得扫码结果，可通过setScanLineColor(int coler)设置扫描线的颜色，通过setBaseContentView(int layoutId)添加扫描框以外的布局，通过restartScan()可在获取一次扫描结果后重启扫描。默认支持沉浸式状态栏，使用前需动态申请相机和闪光灯权限。

使用：
public class QRCodeScanActivity extends ScannerActivity
{
    private ImageView imageView_flashLight;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setBaseContentView(R.layout.activity_qr_code_scan);

        initViews();

        setScanLineColor(Color.parseColor("#66a5ff"));

        imageView_flashLight.setOnClickListener(new View.OnClickListener()
        {
            boolean isLight = false;

            @Override
            public void onClick(View v)
            {
                if (isLight)
                {
                    turnOffLight();
                    imageView_flashLight.setImageResource(R.mipmap.icon_light_off);
                }
                else
                {
                    turnOnLight();
                    imageView_flashLight.setImageResource(R.mipmap.icon_light_on);
                }

                isLight = !isLight;
            }
        });
    }

    @Override
    protected void onGetResult(final String result, Bitmap bitmap)
    {
        super.onGetResult(result, bitmap);

        AlertDialog dialog = new AlertDialog.Builder(QRCodeScanActivity.this)
                .setTitle("扫描结果")
                .setMessage(result).setNegativeButton("取消", null)
                .setPositiveButton("使用浏览器打开", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Uri uri = Uri.parse(result);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                }).setNeutralButton("复制", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        clipboardManager.setText(result);

                        AppUtil.showToast(QRCodeScanActivity.this, "已成功复制到剪贴板");
                    }
                }).create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                restartScan();
            }
        });
        dialog.show();
    }

    private void initViews()
    {
        imageView_flashLight = (ImageView) findViewById(R.id.imageView_flashLight);
        findViewById(R.id.textView_back).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }
}

扫描框外布局：
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <TextView
        android:id="@+id/textView_back"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="10dp"
        android:padding="10dp"
        android:text="返回"
        android:textColor="#ffffff"
        android:textSize="16sp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="top|center"
        android:orientation="vertical">

        <ImageView
            android:id="@+id/imageView_flashLight"
            android:layout_width="50dp"
            android:layout_height="50dp"
            android:layout_marginTop="40dp"
            android:background="@mipmap/icon_light_off"/>

    </LinearLayout>

</LinearLayout>

二、生成二维码：
调用Bitmap createQrCode(String content, final int width, final int height)方法即可生成指定大小的二维码。
