package com.yu.nested.library.manager;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

public class NestedLinearLayoutManager extends LinearLayoutManager implements NestedBaseManager {
    private boolean canScroll = true;

    public NestedLinearLayoutManager(Context context) {
        super(context);
    }

    public NestedLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public NestedLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
