package com.yu.nested.recyclerview.base;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.tabs.TabLayout;

public class RelatedTabLayout extends TabLayout {
    private TabLayout mScrollView;

    public RelatedTabLayout(Context context) {
        super(context);
    }

    public RelatedTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldL, int oldT) {
        super.onScrollChanged(l, t, oldL, oldT);
        if(mScrollView != null) {
            mScrollView.scrollTo(l, t);
        }
    }

    public void setRelateScrollView(TabLayout scrollView) {
        mScrollView = scrollView;
    }
}
