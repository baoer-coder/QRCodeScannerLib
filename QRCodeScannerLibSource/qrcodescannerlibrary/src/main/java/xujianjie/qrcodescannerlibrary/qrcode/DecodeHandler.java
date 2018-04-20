package xujianjie.qrcodescannerlibrary.qrcode;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;

import xujianjie.qrcodescannerlibrary.R;
import xujianjie.qrcodescannerlibrary.activity.ScannerActivity;

final class DecodeHandler extends Handler
{
    private final ScannerActivity activity;
    private final MultiFormatReader multiFormatReader;

    DecodeHandler(ScannerActivity activity, Hashtable<DecodeHintType, Object> hints)
    {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message message)
    {
        if (message.what == R.id.decode)
        {
            decode((byte[]) message.obj, message.arg1, message.arg2);

        }
        else if (message.what == R.id.quit)
        {
            Looper.myLooper().quit();
        }
    }

    private void decode(byte[] data, int width, int height)
    {
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width;
        width = height;
        height = tmp;
        Result rawResult = null;

        PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try
        {
            rawResult = multiFormatReader.decodeWithState(bitmap);
        }
        catch (ReaderException re)
        {
        }
        finally
        {
            multiFormatReader.reset();
        }

        if (rawResult != null)
        {
            Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, rawResult);
            Bundle bundle = new Bundle();
            bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
            message.setData(bundle);
            message.sendToTarget();
        }
        else
        {
            Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
    }
}
