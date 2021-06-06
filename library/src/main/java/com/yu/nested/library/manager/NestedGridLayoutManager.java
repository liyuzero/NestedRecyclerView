package com.yu.nested.library.manager;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NestedGridLayoutManager extends GridLayoutManager implements NestedBaseManager {
    private boolean canScroll = true;

    public NestedGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public NestedGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public NestedGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
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

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        return 2000 * 100;
    }
}
