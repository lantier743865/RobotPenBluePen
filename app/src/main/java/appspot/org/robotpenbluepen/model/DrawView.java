package appspot.org.robotpenbluepen.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by wangweiwei on 2017/1/22.
 */

public class DrawView extends View {

    private float mX;
    private float mY;

    private final Paint mGesturePaint = new Paint();  //画线笔
    private final Paint mPointPaint = new Paint();  //画点笔


    private final Path mPath = new Path();


    public static boolean LINE_TYPE = true;


    public DrawView(Context context) {
        super(context);
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setStrokeWidth(5);
        mGesturePaint.setStyle(Paint.Style.STROKE);
        mGesturePaint.setColor(Color.BLACK);

        mPointPaint.setAntiAlias(true);
        mPointPaint.setStrokeWidth(2);
        mPointPaint.setStyle(Paint.Style.STROKE);
        mPointPaint.setColor(Color.RED);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        canvas.drawPath(mPath, mGesturePaint);
        canvas.drawCircle(mX, mY, 1, mPointPaint);

    }

    //手指点下屏幕时调用
    public void touchDown(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mPointPaint.setColor(Color.WHITE);
            mPointPaint.setStrokeWidth(1);
        }else {
            mPointPaint.setColor(Color.BLACK);
            mPointPaint.setStrokeWidth(10);
        }
        float x = event.getX();
        float y = event.getY();

        mX = x;
        mY = y;

        Log.e("touchDown", "result=" + mX + "    " + mY);
        //mPath绘制的绘制起点
        mPath.moveTo(x, y);
    }

    //手指在屏幕上滑动时调用
    public void touchMove(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);

        //两点之间的距离大于等于3时，生成贝塞尔绘制曲线
        if (dx >= 3 || dy >= 3) {
            //设置贝塞尔曲线的操作点为起点和终点的一半
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;

            //二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
            mPath.quadTo(previousX, previousY, cX, cY);

            //第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
            mX = x;
            mY = y;
        }
        Log.e("touchMove", "result=" + mX + "    " + mY);


    }

    /**
     * @param x     坐标x
     * @param y     坐标Y
     * @param paint 笔的按压值
     */
    public void setDrawLine(float x, float y, int paint) {

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);

        //两点之间的距离大于等于3时，生成贝塞尔绘制曲线
        if (dx >= 3 || dy >= 3) {
            //设置贝塞尔曲线的操作点为起点和终点的一半
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;

            //二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
            mPath.quadTo(previousX, previousY, cX, cY);

            //第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
            mX = x;
            mY = y;
        }
    }

    public WindowManager getWm() {
        return (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }


    public void setTouchDown(float x, float y) {
        mX = x;
        mY = y;
        //mPath绘制的绘制起点
        mPath.moveTo(x, y);
    }

    public void setClearCanvas() {
        mPath.reset();
    }
}
