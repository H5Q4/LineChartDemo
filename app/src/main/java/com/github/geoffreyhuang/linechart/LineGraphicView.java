package com.github.geoffreyhuang.linechart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

class LineGraphicView extends View {
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
    private int mYSpacingCount;

    private DashPathEffect mDashPathEffect;
    private Path mPath;

    public LineGraphicView(Context context) {
        this(context, null);
    }

    public LineGraphicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    private void initView() {
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mDashPathEffect = new DashPathEffect(new float[]{5, 5}, 0);
        this.mPath = new Path();
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

        canvas.drawColor(Color.TRANSPARENT);

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

        drawRightDashLine(canvas);
    }

    private void drawRightDashLine(Canvas canvas) {
        Integer lastX = mXList.get(mXList.size() - 1);
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(dip2px(1.0f));
        mPaint.setPathEffect(mDashPathEffect);
        mPath.moveTo(lastX, mTopMargin);
        mPath.lineTo(lastX, mBottomY);
        canvas.drawPath(mPath, mPaint);
        mPaint.reset();
    }

    /**
     * 画X轴，Y轴坐标
     */
    private void drawAllXLine(Canvas canvas) {
        canvas.drawLine(mLeftX,
                mBottomY,
                (mCanvasWidth - mLeftX / 2),
                mBottomY,
                mPaint);
        for (int i = 0; i < mYSpacingCount + 1; i++) {
            if (i != 0 && i != mYSpacingCount) {
                canvas.drawLine(mLeftX,
                        mBottomY - ((mBottomY - mTopMargin) / mYSpacingCount) * i,
                        mLeftX + dip2px(5),
                        mBottomY - ((mBottomY - mTopMargin) / mYSpacingCount) * i,
                        mPaint);
            }
            if (i != mYSpacingCount) {
                // Y坐标
                drawText(String.valueOf(mYAvgValue * i),
                        mLeftX / 2,
                        mBottomY - ((mBottomY - mTopMargin) / mYSpacingCount) * i + dip2px(5),
                        canvas);
            }
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Style.STROKE);
            canvas.drawLine(mLeftX, mTopMargin, mLeftX / 5 * 4, mTopMargin * 2, mPaint);
            canvas.drawLine(mLeftX, mTopMargin, mLeftX / 5 * 6, mTopMargin * 2, mPaint);
        }
    }

    /**
     * 画Y轴，X轴坐标，最右端边界
     */
    private void drawAllYLine(Canvas canvas) {
        canvas.drawLine(mLeftX,
                mTopMargin,
                mLeftX,
                mBottomY, mPaint);
        for (int i = 0; i < mYData.size(); i++) {
            mXList.add(mLeftX + (mCanvasWidth - mLeftX - mRightMargin) / mYData.size() * i);
            // X坐标
            if (i == 0) {
                drawText(mXData.get(i),
                        (mCanvasWidth - mLeftX) / mYData.size() * i,
                        mBottomY + dip2px(32),
                        canvas);
            }
            if (i == mYData.size() - 1) {
                drawText(mXData.get(i),
                        mCanvasWidth - dip2px(120),
                        mBottomY + dip2px(32),
                        canvas);
            }
        }
        canvas.drawLine(mCanvasWidth - mLeftX / 2, mBottomY,
                mCanvasWidth - mLeftX / 2 - mTopMargin / 2, mBottomY + mLeftX / 5, mPaint);
        canvas.drawLine(mCanvasWidth - mLeftX / 2, mBottomY,
                mCanvasWidth - mLeftX / 2 - mTopMargin / 2,
                mBottomY - mLeftX / 5,
                mPaint);
    }

    private void drawScrollLine(Canvas canvas) {
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(dip2px(2.5f));
        mPaint.setStyle(Style.STROKE);
        mPaint.setAntiAlias(true);
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
            int ph = mBottomY - (int) ((mBottomY - mTopMargin) * (mYData.get(i) / (mYMaxValue +
                    mYAvgValue)));
            points[i] = new Point(mXList.get(i), ph);
        }
        return points;
    }

    public void setData(ArrayList<Double> yData, ArrayList<String> xData, int maxValue, int
            yCount) {
        this.mYMaxValue = maxValue;
        this.mYAvgValue = maxValue / yCount;
        this.mPoints = new Point[yData.size()];
        this.mXData = xData;
        this.mYData = yData;
        this.mYSpacingCount = yCount + 1;
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