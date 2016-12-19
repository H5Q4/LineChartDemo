package com.github.geoffreyhuang.linechart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

class LineGraphicView_bak extends View {
    /**
     * 公共部分
     */
    private static final int CIRCLE_SIZE = 10;

    private enum LineStyle {
        Line, Curve
    }

    private Context mContext;
    private Paint mPaint;

    /**
     * data
     */
    private LineStyle mStyle = LineStyle.Curve;

    private int mCanvasHeight;
    private int mCanvasWidth;
    private int mBottomY = 0;
    private int mLeftX;
    private boolean mIsMeasure = true;
    /**
     * Y轴最大值
     */
    private int mYMaxValue;
    /**
     * Y轴间距值
     */
    private int mYAvgValue;
    private int mTopMargin = 20;
    private int mRightMargin = 20;
    private int mBottomMargin = 40;
    private int mLeftMargin = 40;

    /**
     * 曲线上总点数
     */
    private Point[] mPoints;
    /**
     * 纵坐标值
     */
    private ArrayList<Double> mYData;
    /**
     * 横坐标值
     */
    private ArrayList<String> mXData;
    private ArrayList<Integer> mXList = new ArrayList<Integer>();// 记录每个x的值
    private int mSpacingHeight;

    public LineGraphicView_bak(Context context) {
        this(context, null);
    }

    public LineGraphicView_bak(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    private void initView() {
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTopMargin = dip2px(mTopMargin);
        mBottomMargin = dip2px(mBottomMargin);
        mLeftMargin = dip2px(mLeftMargin);
        mRightMargin = dip2px(mRightMargin);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        if (mIsMeasure) {
            this.mCanvasHeight = getHeight();
            this.mCanvasWidth = getWidth();
            if (mBottomY == 0) {
                mBottomY = mCanvasHeight - mBottomMargin;
            }
            mLeftX = mLeftMargin;
            mIsMeasure = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(Color.RED);

        drawAllXLine(canvas);
        // 画直线（纵向）
        drawAllYLine(canvas);
        // 点的操作设置
        mPoints = getPoints();

        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(dip2px(2.5f));
        mPaint.setStyle(Style.STROKE);
        if (mStyle == LineStyle.Curve) {
            drawScrollLine(canvas);
        } else {
            drawLine(canvas);
        }

        mPaint.setStyle(Style.FILL);
        for (Point mPoint : mPoints) {
            canvas.drawCircle(mPoint.x, mPoint.y, CIRCLE_SIZE / 2, mPaint);
        }
    }

    /**
     * 画所有横向表格，包括X轴
     */
    private void drawAllXLine(Canvas canvas) {
        canvas.drawLine(mLeftX,
                mBottomY + mTopMargin,
                (mCanvasWidth - mLeftX),
                mBottomY + mTopMargin,
                mPaint);
        for (int i = 0; i < mSpacingHeight + 1; i++) {
            // Y坐标
            drawText(String.valueOf(mYAvgValue * i),
                    mLeftX / 2,
                    mBottomY - (mBottomY / mSpacingHeight) * i + mTopMargin,
                    canvas);
        }
    }

    /**
     * 画所有纵向表格，包括Y轴
     */
    private void drawAllYLine(Canvas canvas) {
        canvas.drawLine(mLeftX,
                mTopMargin,
                mLeftX,
                mBottomY + mTopMargin, mPaint);
        for (int i = 0; i < mYData.size(); i++) {
            mXList.add(mLeftX + (mCanvasWidth - mLeftX) / mYData.size() * i);
            // X坐标
            drawText(mXData.get(i),
                    mLeftX + (mCanvasWidth - mLeftX) / mYData.size() * i,
                    mBottomY + dip2px(32),
                    canvas);
        }
    }

    private void drawScrollLine(Canvas canvas) {
        Point startP;
        Point endP;
        for (int i = 0; i < mPoints.length - 1; i++) {
            startP = mPoints[i];
            endP = mPoints[i + 1];
            int wt = (startP.x + endP.x) / 2;
            Point p3 = new Point();
            Point p4 = new Point();
            p3.y = startP.y;
            p3.x = wt;
            p4.y = endP.y;
            p4.x = wt;

            Path path = new Path();
            path.moveTo(startP.x, startP.y);
            path.cubicTo(p3.x, p3.y, p4.x, p4.y, endP.x, endP.y);
            canvas.drawPath(path, mPaint);
        }
    }

    private void drawLine(Canvas canvas) {
        Point startP;
        Point endP;
        for (int i = 0; i < mPoints.length - 1; i++) {
            startP = mPoints[i];
            endP = mPoints[i + 1];
            canvas.drawLine(startP.x, startP.y, endP.x, endP.y, mPaint);
        }
    }

    private void drawText(String text, int x, int y, Canvas canvas) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(dip2px(12));
        p.setColor(Color.BLUE);
        p.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(text, x, y, p);
    }

    private Point[] getPoints() {
        Point[] points = new Point[mYData.size()];
        for (int i = 0; i < mYData.size(); i++) {
            int ph = mBottomY - (int) (mBottomY * (mYData.get(i) / mYMaxValue));

            points[i] = new Point(mXList.get(i), ph + mTopMargin);
        }
        return points;
    }

    public void setData(ArrayList<Double> yData, ArrayList<String> xData, int maxValue, int avgValue) {
        this.mYMaxValue = maxValue;
        this.mYAvgValue = avgValue;
        this.mPoints = new Point[yData.size()];
        this.mXData = xData;
        this.mYData = yData;
        this.mSpacingHeight = maxValue / avgValue;
    }

    public void setYMaxValue(int maxValue) {
        this.mYMaxValue = maxValue;
    }

    public void setYAvgValue(int YAvgValue) {
        this.mYAvgValue = YAvgValue;
    }

    public void setTopMargin(int topMargin) {
        this.mTopMargin = topMargin;
    }

    public void setBottomMargin(int bottomMargin) {
        this.mBottomMargin = bottomMargin;
    }

    public void setStyle(LineStyle mStyle) {
        this.mStyle = mStyle;
    }

    public void setBottomY(int bottomY) {
        this.mBottomY = bottomY;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(float dpValue) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return (int) (dpValue * dm.density + 0.5f);
    }

}