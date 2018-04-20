package xujianjie.qrcodescannerlibrary.qrcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;

import java.util.Collection;
import java.util.HashSet;

import xujianjie.qrcodescannerlibrary.R;

public final class ViewfinderView extends View
{
    // 刷新界面的时间
    private static final long ANIMATION_DELAY = 1;
    private static final int OPAQUE = 0xFF;

    // 四个绿色边角对应的长度
    private int ScreenRate;

    // 四个绿色边角对应的宽度
    private static final int CORNER_WIDTH = 5;

    // 扫描框中的中间线的宽度
    private static final int MIDDLE_LINE_WIDTH = 2;

    // 扫描框中的中间线的与扫描框左右的间隙
    private static final int MIDDLE_LINE_PADDING = 0;

    // 中间那条线每次刷新移动的距离
    private static final int SPEEN_DISTANCE = 5;

    // 手机的屏幕密度
    private static float density;

    // 画笔对象的引用
    private Paint paint;

    // 中间滑动线的最顶端位置
    private int slideTop;

    private int scanLineColor;

    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;

    private final int resultPointColor;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;

    boolean isFirst;

    public ViewfinderView(Context context)
    {
        this(context, null);
    }

    public ViewfinderView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ViewfinderView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs);

        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.QRCode, defStyleAttr, 0);
        scanLineColor = attributes.getColor(R.styleable.QRCode_scanLineColor, Color.parseColor("#ff55a6ff"));
        attributes.recycle();

        density = context.getResources().getDisplayMetrics().density;
        ScreenRate = (int) (30 * density);//边角线的长度=30

        paint = new Paint();
        maskColor = Color.parseColor("#60000000");
        resultColor = Color.parseColor("#b0000000");

        resultPointColor = Color.parseColor("#c0ffff00");
        possibleResultPoints = new HashSet<>(5);
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas)
    {
        // 中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null)
        {
            return;
        }

        // 初始化中间线滑动的最上边和最下边
        if (!isFirst)
        {
            isFirst = true;
            slideTop = frame.top;
        }

        // 获取屏幕的宽和高
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        paint.setColor(resultBitmap != null ? resultColor : maskColor);

        // 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
        // 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
        canvas.drawRect(0, frame.bottom, width, height, paint);

        if (resultBitmap != null)
        {
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        }
        else
        {
            // 画扫描框边上的角，总共8个部分
            paint.setColor(scanLineColor);
            // left, top, right, bottom
            canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate, frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top + ScreenRate, paint);
            canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right, frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top + ScreenRate, paint);
            canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left + ScreenRate, frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - ScreenRate, frame.left + CORNER_WIDTH, frame.bottom, paint);
            canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH, frame.right, frame.bottom, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate, frame.right, frame.bottom, paint);

            // 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
            slideTop += SPEEN_DISTANCE;
            if (slideTop >= frame.bottom)
            {
                slideTop = frame.top;
            }
            canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH / 2, frame.right
                    - MIDDLE_LINE_PADDING, slideTop + MIDDLE_LINE_WIDTH / 2, paint);

            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            if (currentPossible.isEmpty())
            {
                lastPossibleResultPoints = null;
            }
            else
            {
                possibleResultPoints = new HashSet<ResultPoint>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(OPAQUE);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentPossible)
                {
                    canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
                }
            }
            if (currentLast != null)
            {
                paint.setAlpha(OPAQUE / 2);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentLast)
                {
                    canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
                }
            }

            // 只刷新扫描框的内容，其他地方不刷新
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    public void setScanLineColor(int scanLineColor)
    {
        this.scanLineColor = scanLineColor;
    }

    public void drawViewfinder()
    {
        resultBitmap = null;
        invalidate();
    }

    public void drawResultBitmap(Bitmap barcode)
    {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point)
    {
        possibleResultPoints.add(point);
    }
}
