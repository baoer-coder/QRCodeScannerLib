package xujianjie.qrcodescannerlib.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionApplyUtil
{
    private static final int CALL_BACK_CODE = 0x1;
    private Activity context;
    private List<PermissionData> permissionList;
    private OnPermissionRequestResultListener onPermissionRequestResultListener;

    public PermissionApplyUtil(Activity context, List<PermissionData> permissionList)
    {
        this.context = context;
        this.permissionList = permissionList;
    }

    public void apply()
    {
        List<PermissionData> list = new ArrayList<>();
        for (PermissionData permissionData : permissionList)
        {
            if (ContextCompat.checkSelfPermission(context, permissionData.permission) != PackageManager.PERMISSION_GRANTED)
            {
                list.add(permissionData);
            }
        }

        if (list.size() > 0)
        {
            String[] array = new String[list.size()];
            for (int i = 0; i < list.size(); i++)
            {
                array[i] = list.get(i).permission;
            }

            ActivityCompat.requestPermissions(context, array, CALL_BACK_CODE);
        }
        else
        {
            if (onPermissionRequestResultListener != null)
                onPermissionRequestResultListener.permissionRequestResult(true);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == CALL_BACK_CODE)
        {
            boolean allGranted = true;
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < permissions.length; i++)
            {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                {
                    permissionList.get(i).granted = true;
                }
                else
                {
                    permissionList.get(i).granted = false;
                    sb.append(permissionList.get(i).name + "、");
                    allGranted = false;
                }
            }

            if (!allGranted)
            {
                String result = sb.toString();
                result = result.substring(0, result.length() - 1);

                new AlertDialog.Builder(context)
                        .setTitle("警告")
                        .setMessage("您禁止了" + result + "权限，将影响正常使用，请前往应用管理器授予这些权限。")
                        .setNegativeButton("确定", null)
                        .create()
                        .show();
            }

            if (onPermissionRequestResultListener != null)
                onPermissionRequestResultListener.permissionRequestResult(allGranted);
        }
    }

    public interface OnPermissionRequestResultListener
    {
        void permissionRequestResult(boolean allGranted);
    }

    public void setOnPermissionRequestResultListener(OnPermissionRequestResultListener onPermissionRequestResultListener)
    {
        this.onPermissionRequestResultListener = onPermissionRequestResultListener;
    }

    public static class PermissionData
    {
        public String permission;
        public String name;
        public boolean granted;

        public PermissionData(String permission, String name)
        {
            this.permission = permission;
            this.name = name;
        }
    }
}
