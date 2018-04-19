# 扫描二维码和生成二维码

快速使用：
1、在项目的build.gradle中配置maven仓库：

allprojects {
    repositories {
        maven { url "https://raw.githubusercontent.com/xujianjie12138/QRCodeScannerLib/master" }
    }
}

2、在app的build.gradle中引用：

dependencies {
    compile 'xujianjie:qrcodescannerlibrary:1.0.0'
}

# 二维码扫描：
使用时需要继承ScannerActivity，覆盖onGetResult(final String result, Bitmap bitmap)即可获得扫码结果，可通过setScanLineColor(int coler)设置扫描线的颜色，通过setBaseContentView(int layoutId)添加扫描框以外的布局，通过restartScan()可在获取一次扫描结果后重启扫描。默认支持沉浸式状态栏，使用前需动态申请相机和闪光灯权限。

# 生成二维码：
调用Bitmap createQrCode(String content, final int width, final int height)方法即可生成指定大小的二维码。


