package com.panku.draglayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Administrator on 2017/6/26.
 * 侧拉面板自定义view
 */

public class DragLayoutView extends FrameLayout {
    private ViewDragHelper viewDragHelper;
    private int mWidth;
    private int mHeight;
    private int mRange;
    private ViewGroup mLeftPanel;
    private ViewGroup mMainPanel;

    public enum Status {//枚举
        CLOSE, OPEN, DRAGING;
    }

    /**
     * 返回当前侧拉面板状态(打开or关闭)
     *
     * @return
     */
    public Status getStatus() {
        return status;
    }

    private Status status = Status.CLOSE;//默认是关闭状态

    public DragLayoutView(Context context) {
        this(context, null);
    }

    public DragLayoutView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewDragHelper = ViewDragHelper.create(this, callback);//可以拖动的类  2013年谷歌大会发布
    }

    // 2.事件的拦截交给viewdraghelper
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;// 消费事件 move +up
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mRange = (int) (mWidth * 0.6f);//横向拖动范围   屏幕宽度60%
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 2) {
            throw new IllegalStateException("You must have 2 children at least!");
        }

        if (!(getChildAt(0) instanceof ViewGroup) || !(getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalArgumentException("your children must be instanceof ViewGroup!");
        }
        mLeftPanel = (ViewGroup) findViewById(R.id.fl_left);
        mMainPanel = (ViewGroup) findViewById(R.id.fl_main);
    }

    private final ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        /*返回值决定字view是否可以拖动*/

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        /*决定拖动的范围,还没有产生真正的移动 限制拖动范围*/
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mMainPanel) {  //限制主面板拖动范围
                left = fixLeft(left);
            }
            return left;
        }

        private int fixLeft(int left) {
            if (left < 0) {
                left = 0;
            } else if (left > mRange) {
                left = mRange;
            }
            return left;
        }

        /*获取横向拖动的范围,抗干扰  >0   限制拖动范围*/
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mRange;//横向拖动范围   屏幕宽度60%
        }

        /*已经发生了真正的移动    1.添加伴随动画2.添加状态更新 3.添加回调*/
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            // 1. 将左面版放在原来的位置
            //2. 将左面版的瞬间变化量转交给主面板
            //layout();该方法是View的放置方法，在View类实现。调用该方法需要传入放置View的矩形空间左上角left、top值和右下角right、bottom值(以坐标来理解)。这四个值是相对于父控件而言的
            if (changedView == mLeftPanel) {
                //当拖动左面板的时候,左面版保持原来位置
                mLeftPanel.layout(0, 0, mWidth, mHeight);
                //将瞬间变化量交给主面板
                int newLeft = mMainPanel.getLeft() + dx;
                //重新修订范围
                newLeft = fixLeft(newLeft);
                mMainPanel.layout(newLeft, 0, newLeft + mWidth, mHeight);
            }
            dispatchEvent();//做面板移动的动画效果
        }

        /*手指释放时调用*/
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            // xvel:水平方向速度    向左滑动时值为负数    向右为正数    0
            //小于横向拖动范围 的一半   关闭
            if (xvel == 0 && mMainPanel.getLeft() < mRange * 0.5f) {
                close();
            } else if (xvel < 0) {//小于0说明向左滑动 关闭
                close();
            } else {//打开侧拉面板
                openLayout();
            }
        }
    };

    private void dispatchEvent() {
        float fraction = mMainPanel.getLeft() * 1.0f / mRange;
        anims(fraction);//动画效果 面板缩放 平移
        //侧拉状态更新
        Status lastStatus = status;//记录上一个状态
        //获取当前状态
        status = updateStatus(fraction);
        if (onDragChangeListener != null) {
            onDragChangeListener.onDraging(fraction);
            if (status != lastStatus) {
                if (status == Status.CLOSE) {
                    onDragChangeListener.onClose();
                } else if (status == Status.OPEN) {
                    onDragChangeListener.onopen();
                }
            }
        }
    }

    private Status updateStatus(float fraction) {
        if (fraction == 0) {//百分比是0  当前处于关闭状态
            return Status.CLOSE;
        } else if (fraction == 1) {//当前处于打开状态
            return Status.OPEN;
        }
        return Status.DRAGING;
    }

    private void anims(float fraction) {
        //        mMainPanel.setScaleX(evaluate(fraction, 1.0f, 0.8f));
//        mMainPanel.setScaleY(evaluate(fraction, 1.0f, 0.8f));    3.0以下不兼容
//        1. 主面板:缩放 1.0f ---- 0.8f   0.5 + (1-percent)*(1-0.5)
        ViewHelper.setScaleX(mMainPanel, evaluate(fraction, 1.0f, 0.8f));
        ViewHelper.setScaleY(mMainPanel, evaluate(fraction, 1.0f, 0.8f));   //可以兼容任何版本

        // 2. 左面版:缩放 0.7f----1.0f 平移
        ViewHelper.setScaleX(mLeftPanel, evaluate(fraction, 0.7f, 1.0f));
        ViewHelper.setScaleY(mLeftPanel, evaluate(fraction, 0.7f, 1.0f));
        ViewHelper.setTranslationX(mLeftPanel, evaluate(fraction, -mWidth * 1.0f / 2, 0));
        // 3. 背景:颜色转换
        this.getBackground().setColorFilter(
                (int) evaluateColor(fraction, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }


    /**
     * 计算过程值 FloatEvaluator 估值器
     *
     * @param fraction   百分比
     * @param startValue 开始值
     * @param endValue   结束值
     * @return
     */
    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    /**
     * 计算颜色过程值 ArgbEvaluator 估值器
     *
     * @param fraction   百分比
     * @param startValue 开始颜色
     * @param endValue   结束颜色
     * @return
     */
    public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int) ((startA + (int) (fraction * (endA - startA))) << 24) |
                (int) ((startR + (int) (fraction * (endR - startR))) << 16) |
                (int) ((startG + (int) (fraction * (endG - startG))) << 8) |
                (int) ((startB + (int) (fraction * (endB - startB))));
    }

    /**
     * 平滑打开侧拉菜单
     */
    public void openLayout() {
        open(true);//默认平滑打开
    }

    /**
     * 平滑关闭侧拉菜单
     */
    public void close() {
        close(true);//默认平滑关闭
    }

    public void open(boolean isSmooth) {
        int finalLeft = mRange;
        if (isSmooth) {
            boolean b = viewDragHelper.smoothSlideViewTo(mMainPanel, finalLeft, 0);
            //是否需要执行动画
            if (b) {
                ViewCompat.postInvalidateOnAnimation(this);//次方法被调用的时候,computeScroll()会调用多次
            }
        } else {
            mMainPanel.layout(finalLeft, 0, mRange + mWidth, mHeight);
        }
    }

    public void close(boolean isSmooth) {
        int finalLeft = 0;
        if (isSmooth) {//如果是平滑关闭侧拉菜单
            boolean b = viewDragHelper.smoothSlideViewTo(mMainPanel, finalLeft, 0);
            //是否需要执行动画
            if (b) {
                ViewCompat.postInvalidateOnAnimation(this);//次方法被调用的时候,computeScroll()会调用多次
            }
        } else {
            mMainPanel.layout(finalLeft, 0, mWidth, mHeight);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //会调用多次
        boolean b = viewDragHelper.continueSettling(true);
        if (b) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    private OnDragChangeListener onDragChangeListener;

    public OnDragChangeListener getOnDragChangeListener() {
        return onDragChangeListener;
    }

    public void setOnDragChangeListener(OnDragChangeListener onDragChangeListener) {
        this.onDragChangeListener = onDragChangeListener;
    }

    public interface OnDragChangeListener {
        void onClose();

        void onopen();

        void onDraging(float fraction);
    }

}
