package com.yu.nested.library.manager;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class NestedStaggeredGridLayoutManager extends StaggeredGridLayoutManager implements NestedBaseManager {
    private boolean canScroll = true;

    public NestedStaggeredGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public NestedStaggeredGridLayoutManager(int spanCount, int orientation) {
        super(spanCount, orientation);
    }

    @Override
    public boolean canScrollHorizontally() {
        return canScroll && super.canScrollHorizontally();
    }

    public boolean canScrollVertically() {
        return canScroll && super.canScrollVertically();
    }

    @Override
    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }
}
