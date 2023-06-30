package com.example.learnningproject.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class TagFlowLayout extends ViewGroup {
    private final String TAG = "TAG_FLOW_LAYOUT";

    private int onMeasureCount = 0; //测量次数
    private int onLayoutCount = 0; //摆放次数
    //保存每一行的每一个view
    private final List<List<View>> mRowViews = new ArrayList<>();
    //保存每一行的高度
    private final List<Integer> mRowHeight = new ArrayList<>();
    //当前行的views
    private List<View> currentViews = new ArrayList<>();

    public TagFlowLayout(Context context) {
        this(context,null,0);
    }

    public TagFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TagFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure: " + onMeasureCount++);
        mRowViews.clear();
        mRowHeight.clear();
        currentViews.clear();//会进行2次绘制，需清除
        //获取自身的模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //获取自身的宽高
        int width = MeasureSpec.getSize(widthMeasureSpec) - (getPaddingLeft()+getPaddingRight());
        int height = MeasureSpec.getSize(heightMeasureSpec);
        //tagflowlayout控件自身整体的宽高
        int mLayoutWidth = 0;
        int mLayoutHeight = 0;
        //每一行当前状态的宽高
        int lineWidth = 0;
        int lineHeight = 0;
        //获取子视图的个数
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            //1.获取子视图
            View childView = getChildAt(i);
            //2.对子视图进行测量
            measureChildWithMargins(childView,widthMeasureSpec,0,heightMeasureSpec,0);
            //3.获取子视图的宽高
            int childWidth = childView.getMeasuredWidth();
            int childHeight = childView.getMeasuredHeight();
            //4.子视图真正所占的大小
            MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
            int realWidth = childWidth + layoutParams.leftMargin + layoutParams.rightMargin;
            int realHeight = childHeight + layoutParams.topMargin + layoutParams.bottomMargin;
            //判断加上子视图是否会大于行宽
            if (realWidth + lineWidth > width) {
                //获取控件最大的宽度
                mLayoutWidth = Math.max(lineWidth,mLayoutWidth);
                mLayoutHeight += lineHeight; //保存控件高度
                //保存行的view数据
                mRowViews.add(currentViews);
                //保存行高
                mRowHeight.add(lineHeight);
                //重新开辟空间保存子view
                currentViews = new ArrayList<>();
                currentViews.add(childView);
                //重置行宽高
                lineWidth = realWidth;
                lineHeight = realHeight;
            } else {
                //保存行view
                currentViews.add(childView);
                //增加行宽,保存行高最大值
                lineWidth += realWidth;
                lineHeight = Math.max(lineHeight,realHeight);
            }
            //保存数据最后一行
            if (i == childCount - 1) {
                mLayoutWidth = Math.max(mLayoutWidth,lineWidth) + getPaddingRight() + getPaddingLeft();
                mLayoutHeight += lineHeight;
                mRowViews.add(currentViews);
                mRowHeight.add(lineHeight);
            }
        }
        if (widthMode == MeasureSpec.EXACTLY) {
            mLayoutWidth = width + getPaddingLeft() + getPaddingRight();
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            mLayoutHeight = height;
        } else {
            mLayoutHeight += getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(mLayoutWidth,mLayoutHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i(TAG, "onLayout: " + onLayoutCount++);
        int left,top,right,bottom;
        int currentTop = getPaddingTop();
        int currentLeft = getPaddingLeft();
        int size = mRowViews.size();
        for (int i = 0; i < size; i++) {
            List<View> lineView = mRowViews.get(i);
            int num = lineView.size();
            for (int j = 0; j < num; j++) {
                View view = lineView.get(j);
                MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
                left = currentLeft + params.leftMargin;
                top = currentTop + params.topMargin;
                right = left + view.getMeasuredWidth();
                bottom = top + view.getMeasuredHeight();
                view.layout(left,top,right,bottom);
                currentLeft += view.getMeasuredWidth() + params.leftMargin + params.rightMargin;
            }
            //每一行重置
            currentLeft = getPaddingLeft();
            currentTop += mRowHeight.get(i);
        }
        mRowViews.clear();
        mRowHeight.clear();
    }
}
