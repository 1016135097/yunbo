package com.yunbo.frame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent; 

import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration; 
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

public class DoubleScaleImageView extends ImageView implements OnTouchListener, OnGlobalLayoutListener {
  private boolean isFirst = false;
  private float doubleScale;// ˫���Ŵ��ֵ
  private float fourScale;//  4�Ŵ��ֵ
  private Matrix mScaleMatrix;
  private float defaultScale;// Ĭ�ϵ�����ֵ
  private int mLastPinterCount;// ��¼��һ�ζ�㴥�ص�����
  private float mLastX;
  private float mLastY;
  private int mTouchSlop;
  private boolean isCanDrag;
  private boolean isCheckLeft;
  private boolean isCheckTop;
  private GestureDetector mGestureDetector;
  private int doubleclickcount=0;
  public DoubleScaleImageView(Context context) {
    this(context, null);
  }
  public DoubleScaleImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }
  @SuppressLint("ClickableViewAccessibility")
  public DoubleScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mScaleMatrix = new Matrix();
    setScaleType(ScaleType.MATRIX);
    setOnTouchListener(  this);
    // getScaledTouchSlop��һ�����룬��ʾ������ʱ���ֵ��ƶ�Ҫ�����������ſ�ʼ�ƶ��ؼ������С���������Ͳ������ƶ��ؼ�
    mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
      @Override
      public boolean onDoubleTap(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
//        if (getScale() < doubleScale) {
//          mScaleMatrix.postScale(doubleScale / getScale(), doubleScale / getScale(), x, y);// �Ŵ�
//        }
//        else {
//          mScaleMatrix.postScale(defaultScale / getScale(), defaultScale / getScale(), x, y);// ��С
//        }
        doubleclickcount++;
        doubleclickcount=doubleclickcount%3;
        if (doubleclickcount==1) {
        	mScaleMatrix.postScale(doubleScale / getScale(), doubleScale / getScale(), x, y);
		}
        if (doubleclickcount==2) {
        	mScaleMatrix.postScale(fourScale / getScale(), fourScale / getScale(), x, y);
		}
        if (doubleclickcount==0) {
        	//mScaleMatrix.postScale(doubleScale / getScale(), doubleScale / getScale(), x, y);
        	initimg();
		}
        setImageMatrix(mScaleMatrix);
        return super.onDoubleTap(e);
      }
    });
  }
  @Override
  protected void onAttachedToWindow() {// view���ӵ�������ʱ���ø÷���
    super.onAttachedToWindow();
    getViewTreeObserver().addOnGlobalLayoutListener(this);
  }
  @SuppressWarnings("deprecation")
  @Override
  protected void onDetachedFromWindow() {// ����ͼ�Ӵ����Ϸ����ʱ����ø÷�����
    super.onDetachedFromWindow();
    getViewTreeObserver().removeGlobalOnLayoutListener(this);
  }
  @Override
  public void onGlobalLayout() {// ����������л�ȡImageView������ɺ��ͼƬ
    if (!isFirst) {
      // ��ȡ�ؼ��Ŀ�Ⱥ͸߶�
      int width = getWidth();
      int height = getHeight();
      // �õ����ǵ�ͼƬ�Լ�ͼƬ�Ŀ�ȼ��߶�
      Drawable drawable = getDrawable();
      if (drawable == null) { return; }
      int imageWidth = drawable.getIntrinsicWidth();// ͼƬ�Ŀ��
      int imageHeight = drawable.getIntrinsicHeight();// ͼƬ�ĸ߶�
      float scale = 1.0f;
      // ���ͼƬ��ȴ��ڿؼ���ȣ�����ͼƬ�߶�С�ڿؼ� �߶ȣ�����Ҫ��СͼƬ
      if (imageWidth > width && imageHeight < height) {
        scale = width * 1.0f / imageWidth;
      }
      // ���ͼƬ���С�ڿؼ���ȣ�����ͼƬ�߶ȴ��ڿؼ� �߶ȣ�����Ҫ��СͼƬ
      if (imageWidth < width && imageHeight > height) {
        scale = height * 1.0f / imageHeight;
      }
      // ���ͼƬ�Ŀ�ȶ� ���ڻ�С�ڿؼ���ȣ�������Ҫ��ͼƬ���ж�Ӧ���ţ���֤ͼƬռ���ؼ�
      if ((imageWidth > width && imageHeight > height) || (imageWidth < width && imageHeight < height)) {
        scale = Math.min(width * 1.0f / imageWidth, height * 1.0f / imageHeight);
      }
      // ��ʼ����Ӧ������ֵ
      defaultScale = scale;
      doubleScale = defaultScale * 2;
      fourScale = doubleScale * 2;
      
      // ͼƬ���ź󣬽�ͼƬҪ�ƶ����ؼ�����
      int dx = width / 2 - imageWidth / 2;
      int dy = height / 2 - imageHeight / 2;
      mScaleMatrix.postTranslate(dx, dy);
      mScaleMatrix.postScale(defaultScale, defaultScale, width / 2, height / 2);
      setImageMatrix(mScaleMatrix);
      isFirst = true;
    }
  }
  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouch(View v, MotionEvent event) {
    if (mGestureDetector.onTouchEvent(event)) { return true; }
    float x = 0;
    float y = 0;
    int pointerCount = event.getPointerCount();// ��ȡ������Ļ�ϵ���ָ����
    for (int i = 0; i < pointerCount; i++) {
      x += event.getX(i);
      y += event.getY(i);
    }
    x /= pointerCount;
    y /= pointerCount;
    if (mLastPinterCount != pointerCount) {
      isCanDrag = false;
      mLastX = x;
      mLastY = y;
 
    }
    mLastPinterCount = pointerCount;
    switch (event.getAction()) {
      case MotionEvent.ACTION_MOVE:
        float dx = x - mLastX;
        float dy = y - mLastY;
        isCanDrag = isMove(dx, dy);
        if (isCanDrag) {
          RectF rectf = getMatrixRectf();
          if (null != getDrawable()) {
            isCheckLeft = isCheckTop = true;
            if (rectf.width() < getWidth()) {// ���ͼƬ���С�ڿؼ���ȣ���Ļ��ȣ�����������ƶ�
              dx = 0;
              isCheckLeft = false;
            }
            if (rectf.height() < getHeight()) {// ���ͼƬ�߶�С�ڿؼ��߶ȣ���Ļ�߶ȣ������������ƶ�
              dy = 0;
              isCheckTop = false;
            }
            mScaleMatrix.postTranslate(dx, dy);
            checkTranslateWithBorder();
            setImageMatrix(mScaleMatrix);
          }
        }
        mLastX = x;
        mLastY = y;
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        mLastPinterCount = 0;
        break;
    }
    return true;
  }
  /**
   * �ƶ�ͼƬʱ���б߽���
   * @description��
   * @date 2016-1-8 ����4:02:24
   */
  private void checkTranslateWithBorder() {
    RectF rectf = getMatrixRectf();
    float delX = 0;
    float delY = 0;
    int width = getWidth();
    int height = getHeight();
    if (rectf.top > 0 && isCheckTop) {
      delY = -rectf.top;
    }
    if (rectf.bottom < height && isCheckTop) {
      delY = height - rectf.bottom;
    }
    if (rectf.left > 0 && isCheckLeft) {
      delX = -rectf.left;
    }
    if (rectf.right < width && isCheckLeft) {
      delX = width - rectf.right;
    }
    mScaleMatrix.postTranslate(delX, delY);
  }
  // �ж��Ƿ����ƶ�
  private boolean isMove(float x, float y) {
    return Math.sqrt(x * x + y * y) > mTouchSlop;
  }
  
  @Override
public void setVisibility(int visibility) {
	// TODO Auto-generated method stub
	super.setVisibility(visibility);
	try {
		if (visibility==VISIBLE) {
			initimg();
		}
		
	} catch (Exception e) {
		// TODO: handle exception
	}
}
private void initimg() {
	doubleclickcount=0;
	isFirst=false;
	mScaleMatrix = new Matrix();
	onGlobalLayout();
}
/**
   * ��ȡͼƬ��λ��
   * @description��
   * @date 2016-1-8 ����9:02:10
   */
  private RectF getMatrixRectf() {
    Matrix matrix = mScaleMatrix;
    RectF recft = new RectF();
    if (getDrawable() != null) {
      recft.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
      matrix.mapRect(recft);
    }
    return recft;
  }
 
  // ��ȡ��ǰͼƬ������ֵ
  private float getScale() {
    float values[] = new float[9];
    mScaleMatrix.getValues(values);
    return values[Matrix.MSCALE_X];
  }
  public void doFirst() {
	isFirst=false;
}
}
