package xujianjie.qrcodescannerlib.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

public class BaseActivity extends AppCompatActivity
{
    private int statusBarHeight;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        statusBarHeight = getStatusBarHeight(BaseActivity.this);
    }

    protected void setImmersiveStatusBar(int id, int color)
    {
        View statusBar = findViewById(id);
        if (statusBar != null)
        {
            statusBar.setLayoutParams(new LinearLayout.LayoutParams(-1, statusBarHeight));
            statusBar.setBackgroundColor(color);
        }
    }

    public static int getStatusBarHeight(Context context)
    {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
        {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}