package com.panku.draglayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2017/7/8.
 * 1. 侧拉面板打开 拦截事件
 * 2.手指抬起 关闭侧拉面板
 */

public class MyLinearLayout extends LinearLayout {
    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //只有在策划面板打开的时候拦截事件
        if (dragLayout != null && dragLayout.getStatus() == DragLayoutView.Status.OPEN) {
            return true;
        }
        //其他情况交给系统处理
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //侧拉面板处于打开状态
        if (dragLayout != null && dragLayout.getStatus() == DragLayoutView.Status.OPEN) {
            //手指抬起 关闭侧拉面板
            if (event.getAction() == MotionEvent.ACTION_UP) {
                dragLayout.close();
            }
            return true;//消费  move up事件
        }
        return super.onTouchEvent(event);
    }

    private DragLayoutView dragLayout;

    public void setDragLayout(DragLayoutView dragLayout) {
        this.dragLayout = dragLayout;
    }
}
