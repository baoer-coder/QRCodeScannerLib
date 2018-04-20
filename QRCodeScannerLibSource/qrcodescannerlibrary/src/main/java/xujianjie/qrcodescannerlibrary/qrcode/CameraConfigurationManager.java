package xujianjie.qrcodescannerlibrary.qrcode;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

final class CameraConfigurationManager
{
    private static final int TEN_DESIRED_ZOOM = 27;
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    private final Context context;
    private Point screenResolution;
    private Point cameraResolution;
    private int previewFormat;
    private String previewFormatString;

    CameraConfigurationManager(Context context)
    {
        this.context = context;
    }

    @SuppressWarnings("deprecation")
    void initFromCameraParameters(Camera camera)
    {
        Camera.Parameters parameters = camera.getParameters();
        previewFormat = parameters.getPreviewFormat();
        previewFormatString = parameters.get("preview-format");

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        screenResolution = new Point(display.getWidth(), display.getHeight());

        Point screenResolutionForCamera = new Point();
        screenResolutionForCamera.x = screenResolution.x;
        screenResolutionForCamera.y = screenResolution.y;

        if (screenResolution.x < screenResolution.y)
        {
            screenResolutionForCamera.x = screenResolution.y;
            screenResolutionForCamera.y = screenResolution.x;
        }
        cameraResolution = getCameraResolution(parameters, screenResolutionForCamera);
    }

    @SuppressWarnings("deprecation")
    void setDesiredCameraParameters(Camera camera)
    {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        setFlash(parameters);
        setZoom(parameters);

        camera.setParameters(parameters);
        if (Integer.parseInt(Build.VERSION.SDK) >= 8)
        {
            setDisplayOrientation(camera, 90);
        }
    }

    protected void setDisplayOrientation(Camera camera, int angle)
    {
        Method downPolymorphic;
        try
        {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]
                    {int.class});
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[]
                        {angle});
        }
        catch (Exception e1)
        {
        }
    }

    Point getCameraResolution()
    {
        return cameraResolution;
    }

    Point getScreenResolution()
    {
        return screenResolution;
    }

    int getPreviewFormat()
    {
        return previewFormat;
    }

    String getPreviewFormatString()
    {
        return previewFormatString;
    }

    private static Point getCameraResolution(Camera.Parameters parameters, Point screenResolution)
    {
        String previewSizeValueString = parameters.get("preview-size-values");
        if (previewSizeValueString == null)
        {
            previewSizeValueString = parameters.get("preview-size-value");
        }

        Point cameraResolution = null;

        if (previewSizeValueString != null)
        {
            cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
        }

        if (cameraResolution == null)
        {
            cameraResolution = new Point((screenResolution.x >> 3) << 3, (screenResolution.y >> 3) << 3);
        }

        return cameraResolution;
    }

    private static Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution)
    {
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (String previewSize : COMMA_PATTERN.split(previewSizeValueString))
        {

            previewSize = previewSize.trim();
            int dimPosition = previewSize.indexOf('x');
            if (dimPosition < 0)
            {
                continue;
            }

            int newX;
            int newY;
            try
            {
                newX = Integer.parseInt(previewSize.substring(0, dimPosition));
                newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
            }
            catch (NumberFormatException nfe)
            {
                continue;
            }

            int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
            if (newDiff == 0)
            {
                bestX = newX;
                bestY = newY;
                break;
            }
            else if (newDiff < diff)
            {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }

        }

        if (bestX > 0 && bestY > 0)
        {
            return new Point(bestX, bestY);
        }
        return null;
    }

    private static int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom)
    {
        int tenBestValue = 0;
        for (String stringValue : COMMA_PATTERN.split(stringValues))
        {
            stringValue = stringValue.trim();
            double value;
            try
            {
                value = Double.parseDouble(stringValue);
            }
            catch (NumberFormatException nfe)
            {
                return tenDesiredZoom;
            }
            int tenValue = (int) (10.0 * value);
            if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue))
            {
                tenBestValue = tenValue;
            }
        }
        return tenBestValue;
    }

    private void setFlash(Camera.Parameters parameters)
    {
        if (Build.MODEL.contains("Behold II") && CameraManager.SDK_INT == 3)
        {
            parameters.set("flash-value", 1);
        }
        else
        {
            parameters.set("flash-value", 2);
        }

        parameters.set("flash-mode", "off");
    }

    private void setZoom(Camera.Parameters parameters)
    {
        String zoomSupportedString = parameters.get("zoom-supported");
        if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString))
        {
            return;
        }

        int tenDesiredZoom = TEN_DESIRED_ZOOM;

        String maxZoomString = parameters.get("max-zoom");
        if (maxZoomString != null)
        {
            try
            {
                int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
                if (tenDesiredZoom > tenMaxZoom)
                {
                    tenDesiredZoom = tenMaxZoom;
                }
            }
            catch (NumberFormatException nfe)
            {
            }
        }

        String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
        if (takingPictureZoomMaxString != null)
        {
            try
            {
                int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
                if (tenDesiredZoom > tenMaxZoom)
                {
                    tenDesiredZoom = tenMaxZoom;
                }
            }
            catch (NumberFormatException nfe)
            {
            }
        }

        String motZoomValuesString = parameters.get("mot-zoom-values");
        if (motZoomValuesString != null)
        {
            tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
        }

        String motZoomStepString = parameters.get("mot-zoom-step");
        if (motZoomStepString != null)
        {
            try
            {
                double motZoomStep = Double.parseDouble(motZoomStepString.trim());
                int tenZoomStep = (int) (10.0 * motZoomStep);
                if (tenZoomStep > 1)
                {
                    tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
                }
            }
            catch (NumberFormatException nfe)
            {
            }
        }

        if (maxZoomString != null || motZoomValuesString != null)
        {
            parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
        }

        if (takingPictureZoomMaxString != null)
        {
            parameters.set("taking-picture-zoom", tenDesiredZoom);
        }
    }
}
