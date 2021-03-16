package com.yu.nested.library;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yu.nested.recycler.library.R;

import java.util.HashSet;

public class NestedRecyclerView extends RecyclerView {
    private static final int INVALID_POINTER = -1;

    private ChildRecyclerViewHelper mChildRecyclerViewHelper;
    private final HashSet<OnActionListener> mOnActionListeners = new HashSet<>();
    private final HashSet<OnScrollListener> mOnScrollListeners = new HashSet<>();
    private ScrollerManager mScrollerManager;
    private MultiFingerHelper mFingerHelper;
    private boolean mIsMounting; //是否吸顶状态
    private int mMountingDistance; //正常状态吸顶：外部RecyclerView无法滑动的时候，设置该距离后，会提前滑动距离显示吸顶栏

    private boolean mIsInterceptTouchEvent;
    private boolean mIsScrollUp;
    //手指是否按下
    private boolean isTouching;

    public NestedRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public NestedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        //关闭嵌套滚动机制，避免与下拉刷新等Nested嵌套框架出现冲突
        setNestedScrollingEnabled(false);
        mScrollerManager = new ScrollerManager(context);
        mFingerHelper = new MultiFingerHelper();
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).findViewById(android.R.id.content);
        }
        super.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (mOnScrollListener != null) {
                    mOnScrollListener.onScrolled(recyclerView, dx, dy);
                }
                handleMounting();
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                handleMounting();
                if (mOnScrollListener != null) {
                    mOnScrollListener.onScrollStateChanged(recyclerView, newState);
                }
            }
        });
    }

    @Override
    public void addOnScrollListener(@NonNull OnScrollListener listener) {
        mOnScrollListeners.add(listener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mScrollerManager.addMovement(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouching = true;
                mFingerHelper.actionDown(ev);
                mScrollerManager.actionDown();
                mIsInterceptTouchEvent = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = mFingerHelper.checkMoveAvailable(ev);
                if (pointerIndex == INVALID_POINTER) {
                    mIsInterceptTouchEvent = false;
                    return super.dispatchTouchEvent(ev);
                }

                float curX = ev.getX(pointerIndex);
                float curY = ev.getY(pointerIndex);

                float offsetY = curY - mFingerHelper.mPreY;
                mFingerHelper.mPreY = curY;
                mIsInterceptTouchEvent = mScrollerManager.actionMove(ev, offsetY, curX, curY, mFingerHelper.mDownX, mFingerHelper.mDownY);
                return mIsInterceptTouchEvent || super.dispatchTouchEvent(ev);
            case MotionEvent.ACTION_POINTER_DOWN:
                mIsInterceptTouchEvent = false;
                mFingerHelper.actionPointerDown(ev);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mIsInterceptTouchEvent = false;
                if (mFingerHelper.actionPointerUp(ev)) {
                    return super.dispatchTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
                isTouching = false;
                mFingerHelper.activePointerId = INVALID_POINTER;
                mIsInterceptTouchEvent = mScrollerManager.actionUp() != null;
                return mIsInterceptTouchEvent || super.dispatchTouchEvent(ev);
            case MotionEvent.ACTION_CANCEL:
                isTouching = false;
                mIsInterceptTouchEvent = false;
                mFingerHelper.activePointerId = INVALID_POINTER;
                mScrollerManager.abortAnimation();
                return super.dispatchTouchEvent(ev);
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return mIsInterceptTouchEvent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return true;
    }

    private void scrollVer(MotionEvent ev, float offsetY) {
        if (mIsScrollUp) {
            return;
        }
        if (!(offsetY > 0 && !canScrollVertically(-1))) {
            if (ev != null) {
                //避免特殊情况下子View触摸生效或不连续
                MotionEvent event = MotionEvent.obtain(ev);
                event.setAction(MotionEvent.ACTION_CANCEL);
                try {
                    super.dispatchTouchEvent(event);
                } catch (Exception e) {
                    //
                }
            }
            //滑动内容
            scrollContent(offsetY);
        }
    }

    private void scrollContent(float offsetY) {
        int scrollY = (int) offsetY;
        if (!canScrollVertically(1)) {
            if (mChildRecyclerViewHelper != null) {
                RecyclerView recyclerView = mChildRecyclerViewHelper.getCurRecyclerView();
                if (recyclerView != null) {
                    //关闭嵌套滚动机制，避免与下拉刷新等Nested嵌套框架出现冲突
                    recyclerView.setNestedScrollingEnabled(false);
                }
                if (recyclerView != null && recyclerView.getTag(R.id.nested_recycler_view_inner_recycler_listener) == null) {
                    recyclerView.addOnScrollListener(new OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (mOnScrollListener != null) {
                                mOnScrollListener.onScrollStateChanged(recyclerView, newState);
                            }
                        }

                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            if (mOnScrollListener != null) {
                                mOnScrollListener.onScrolled(recyclerView, dx, dy);
                            }
                        }
                    });
                    recyclerView.setTag(R.id.nested_recycler_view_inner_recycler_listener, new Object());
                }
                if (recyclerView != null && recyclerView.getMeasuredHeight() != 0) {
                    //tab内的RecyclerView在顶部
                    try {
                        if (!recyclerView.canScrollVertically(-1)) {
                            if (offsetY > 0) {
                                scrollContentView(scrollY);
                            } else {
                                recyclerView.scrollBy(0, -scrollY);
                            }
                        } else if (!recyclerView.canScrollVertically(1)) {
                            mScrollerManager.abortAnimation();
                            recyclerView.scrollBy(0, -scrollY);
                        } else {
                            //滑动到底部，此时需要滑动tab内的recyclerView
                            recyclerView.scrollBy(0, -scrollY);
                        }
                    } catch (Exception e) {
                        //规避以下错误【底部信息流的view在被detached之后引起，偶先，理论上去掉信息流部分的回收机制也行】：java.lang.NullPointerException: Attempt to read from field 'java.util.ArrayList
                        // android.support.v7.widget.StaggeredGridLayoutManager$Span.mViews' on a null object reference
                    }
                } else {
                    scrollContentView(scrollY);
                }
            } else {
                scrollContentView(scrollY);
            }
        } else {
            scrollContentView(scrollY);
        }
    }

    private void scrollContentView(int scrollY) {
        scrollBy(0, -scrollY);
        if (mOnScrollListener != null) {
            if (isTouching && mOnScrollListener.getScrollState() != SCROLL_STATE_DRAGGING) {
                mOnScrollListener.onScrollStateChanged(this, SCROLL_STATE_DRAGGING);
            } else if (!isTouching && mOnScrollListener.getScrollState() != SCROLL_STATE_SETTLING) {
                mOnScrollListener.onScrollStateChanged(this, SCROLL_STATE_SETTLING);
            }
        }
    }

    private static class MultiFingerHelper {
        int activePointerId;

        private float mDownX;
        private float mDownY;
        private float mPreY;

        void actionDown(MotionEvent ev) {
            activePointerId = ev.getPointerId(0);
            resetTouchParam(ev, ev.findPointerIndex(activePointerId));
        }

        private int checkMoveAvailable(MotionEvent ev) {
            if (activePointerId == INVALID_POINTER) {
                return INVALID_POINTER;
            }
            return ev.findPointerIndex(activePointerId);
        }

        private void actionPointerDown(MotionEvent ev) {
            int pointerIndex = ev.getActionIndex();
            if (pointerIndex < 0) {
                return;
            }
            if (activePointerId == pointerIndex) {
                return;
            }
            resetTouchParam(ev, pointerIndex);
            activePointerId = ev.getPointerId(pointerIndex);
        }

        private void resetTouchParam(MotionEvent ev, int pointerIndex) {
            mPreY = ev.getY(pointerIndex);
            mDownY = ev.getY(pointerIndex);
            mDownX = ev.getX(pointerIndex);
        }

        private boolean actionPointerUp(MotionEvent ev) {
            int pointerIndex = ev.getActionIndex();
            final int pointerId = ev.getPointerId(pointerIndex);
            if (pointerId == activePointerId) {
                // This was our active pointer going up. Choose a new
                // active pointer and adjust accordingly.
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                resetTouchParam(ev, newPointerIndex);
                activePointerId = ev.getPointerId(newPointerIndex);
            }
            pointerIndex = ev.findPointerIndex(activePointerId);
            if (pointerIndex == INVALID_POINTER) {
                return true;
            }
            resetTouchParam(ev, pointerIndex);
            return false;
        }
    }

    private class ScrollerManager implements Runnable {
        private ViewConfiguration mViewConfiguration;
        private Scroller mScroller;
        private VelocityTracker mTracker;
        private int mPreScrollY;
        private int mMinYV; //最小速度

        private static final int SCROLL_HOR = 0;
        private static final int SCROLL_VER = 1;
        private static final int SCROLL_NONE = 2;

        //当前整体处于的滑动状态，水平/ 竖直/ 无滑动
        private int mCurScrollState;

        ScrollerManager(Context context) {
            mViewConfiguration = ViewConfiguration.get(context);
            mScroller = new Scroller(context);
            mTracker = VelocityTracker.obtain();
            removeCallbacks(this);
        }

        void actionDown() {
            mCurScrollState = SCROLL_NONE;
            abortAnimation();
        }

        boolean actionMove(MotionEvent ev, float offsetY, float curX, float curY, float mDownX, float mDownY) {
            if (mCurScrollState == SCROLL_VER) {
                scrollVer(ev, offsetY);
                return true;
            } else if (mCurScrollState == SCROLL_HOR) {
                return false;
            } else if (mCurScrollState == SCROLL_NONE) {
                float dx = Math.abs(curX - mDownX);
                float dy = Math.abs(curY - mDownY);
                if (dx <= 0.01f && dy < 0.01f) {
                    return false;
                } else {
                    if (Math.abs(dx) >= mViewConfiguration.getScaledTouchSlop()) {
                        mCurScrollState = SCROLL_HOR;
                    }

                    if (Math.abs(dy) > mViewConfiguration.getScaledTouchSlop()) {
                        mCurScrollState = SCROLL_VER;
                        scrollVer(ev, offsetY);
                        return true;
                    }

                    if (mCurScrollState == SCROLL_HOR) {
                        return false;
                    }
                }
            }
            return false;
        }

        Boolean actionUp() {
            if (mCurScrollState == SCROLL_HOR) {
                mScrollerManager.abortAnimation();
                return null;
            } else {
                if (mCurScrollState == SCROLL_VER) {
                    mScrollerManager.handleFlingEvent();
                    return true;
                } else {
                    mScrollerManager.abortAnimation();
                    return null;
                }
            }
        }

        void addMovement(MotionEvent event) {
            if (mTracker == null) {
                mTracker = VelocityTracker.obtain();
            }
            mTracker.addMovement(event);
        }

        void abortAnimation() {
            mScroller.abortAnimation();
        }

        void handleFlingEvent() {
            if (mMinYV == 0) {
                mMinYV = mViewConfiguration.getScaledMinimumFlingVelocity();
            }
            if (mTracker == null) {
                mTracker = VelocityTracker.obtain();
            }
            mTracker.computeCurrentVelocity(1000);
            int initialVelocity = (int) mTracker.getYVelocity();
            if (Math.abs(initialVelocity) > mMinYV) {
                // 由于坐标轴正方向问题，要加负号。
                doFling(-initialVelocity);
            } else {
                mOnScrollListener.onScrollStateChanged(NestedRecyclerView.this, SCROLL_STATE_IDLE);
            }
        }

        void doFling(int speed) {
            mPreScrollY = 0;
            mScroller.fling(0, mPreScrollY, 0, speed, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            post(this);
        }

        @Override
        public void run() {
            boolean finished = !mScroller.computeScrollOffset() || mScroller.isFinished();
            if (!finished) {
                int offsetY = mScroller.getCurrY() - mPreScrollY;
                if (offsetY < 0 && !NestedRecyclerView.this.canScrollVertically(-1)) {
                    //向下滚动，且不能下滑了
                    mOnScrollListener.onScrollStateChanged(NestedRecyclerView.this, SCROLL_STATE_IDLE);
                    removeCallbacks(this);
                }
                mPreScrollY = mScroller.getCurrY();
                scrollContent(-offsetY);
                post(this);
            } else {
                mOnScrollListener.onScrollStateChanged(NestedRecyclerView.this, SCROLL_STATE_IDLE);
                removeCallbacks(this);
            }
        }

        public boolean isFlingFinished() {
            return !mScroller.computeScrollOffset() || mScroller.isFinished();
        }

        void release() {
            if (mTracker != null) {
                mTracker.recycle();
                mTracker = null;
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mScrollerManager != null) {
            mScrollerManager.release();
        }
    }

    public void setChildRecyclerViewHelper(ChildRecyclerViewHelper childRecyclerViewHelper) {
        mChildRecyclerViewHelper = childRecyclerViewHelper;
    }

    public void addOnActionListener(OnActionListener onActionListener) {
        mOnActionListeners.add(onActionListener);
    }

    public static abstract class ChildRecyclerViewHelper {
        public abstract RecyclerView getCurRecyclerView();

        //提供外部自定义吸顶栏，方便实现各种自定义效果，例如广告展示，触摸动画等等
        public View getInnerTabView() {
            return null;
        }

        public View getOutTabView() {
            return null;
        }
    }

    public interface OnActionListener {
        void onTabMounting(boolean isMounting);

        void onTabViewFirstShow(); //第一次把底部view展示出来，一般用来曝光打点
    }

    @Override
    public void stopNestedScroll() {
        super.stopNestedScroll();
        if (mScrollerManager != null) {
            mScrollerManager.abortAnimation();
        }
    }

    private final OnNestedScrollListener mOnScrollListener = new OnNestedScrollListener();

    private class OnNestedScrollListener extends OnScrollListener {
        private int mScrollState = -1;

        public int getScrollState() {
            return mScrollState;
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            mScrollerManager.mCurScrollState = ScrollerManager.SCROLL_VER;
            for (OnScrollListener listener : mOnScrollListeners) {
                listener.onScrolled(recyclerView, 0, dy);
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            mScrollState = newState;
            mScrollerManager.mCurScrollState = ScrollerManager.SCROLL_VER;
            for (OnScrollListener listener : mOnScrollListeners) {
                listener.onScrollStateChanged(recyclerView, newState);
            }
        }
    }

    public interface OnRecyclerScrollListener {
        void onRecyclerScroll(RecyclerView recyclerView, int dy, boolean isTabPage);
    }

    /*---------------------------- 处理吸顶逻辑 -------------------------*/

    private boolean mIsTabViewFirstShow;

    //因为
    private void handleMounting() {
        if (getAdapter() == null || mChildRecyclerViewHelper == null) {
            return;
        }
        int bottomViewPos = getTabPos();
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null) {
            return;
        }

        int firstVisibleItem = -1;
        int lastVisibleItem = -1;
        if (layoutManager instanceof LinearLayoutManager) {
            firstVisibleItem = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            lastVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else {
            return;
        }

        if (bottomViewPos >= firstVisibleItem && bottomViewPos <= lastVisibleItem) {
            if (!mIsTabViewFirstShow) {
                for (OnActionListener listener : mOnActionListeners) {
                    listener.onTabViewFirstShow();
                }
                mIsTabViewFirstShow = true;
            }
            View itemView = getChildAt(bottomViewPos - firstVisibleItem);
            if (itemView != null) {
                if (itemView.getTop() <= mMountingDistance) {
                    //吸顶
                    setTabVisible(true);
                } else {
                    setTabVisible(false);
                }
            }
        } else if (bottomViewPos < firstVisibleItem) {
            //吸顶
            setTabVisible(true);
        } else {
            setTabVisible(false);
        }

        //滑到底部并且可以下拉
        if (!canScrollVertically(1) && canScrollVertically(-1)) {
            setTabVisible(true);
        }
    }

    private void setTabVisible(boolean isCurMounting) {
        if (mChildRecyclerViewHelper == null) {
            return;
        }
        View innerTabView = mChildRecyclerViewHelper.getInnerTabView();
        View outTabView = mChildRecyclerViewHelper.getOutTabView();
        if (isCurMounting != mIsMounting) {
            if (isCurMounting) {
                if (innerTabView != null) {
                    //innerTabView.setVisibility(View.INVISIBLE);
                }
                if (outTabView != null) {
                    outTabView.setVisibility(View.VISIBLE);
                }
                for (OnActionListener listener : mOnActionListeners) {
                    listener.onTabMounting(true);
                }
                notifyLastItemAttacheInfo(true);
                //缓慢滑动的情况下，会出现内部inner tab栏无法与顶部完全贴合的情况，留下一条缝隙，此处需要手动校准下
                if (mChildRecyclerViewHelper.getOutTabView() != null && mChildRecyclerViewHelper.getInnerTabView() != null) {
                    //当吸顶栏为 外部写入 的时候没必要校准
                    scrollBy(0, 5);
                }
            } else {
                if (innerTabView != null) {
                    //innerTabView.setVisibility(View.VISIBLE);
                }
                if (outTabView != null) {
                    outTabView.setVisibility(View.INVISIBLE);
                }
                for (OnActionListener listener : mOnActionListeners) {
                    listener.onTabMounting(false);
                }
                notifyLastItemAttacheInfo(false);
            }
            mIsMounting = isCurMounting;
        }
    }

    /*
     *     当外部吸顶tab的高度 大于 内部tab的高度，瀑布流上最后一个或几个组件的attach方法调用会出现问题，
     *     因为吸顶之后，整体RecyclerView不再滑动，不会触发detach方法。
     *     实际应用中，一般只有tab广告才会需要处理这种情况
     * */
    @SuppressWarnings("rawtypes")
    private void notifyLastItemAttacheInfo(boolean isOutTabVisible) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || getAdapter() == null || mChildRecyclerViewHelper == null ||
                !(getLayoutManager() instanceof LinearLayoutManager)) {
            return;
        }
        LinearLayoutManager manager = (LinearLayoutManager) getLayoutManager();
        int firstVisibleItem = manager.findFirstVisibleItemPosition();
        int bottomTabViewPos = getTabPos();
        for (int i = bottomTabViewPos - 1; i >= firstVisibleItem; i--) {
            RecyclerView.ViewHolder holder = findViewHolderForAdapterPosition(i);
            if (holder == null || getAdapter() == null) {
                continue;
            }
            RecyclerView.Adapter adapter = getAdapter();
            if (isOutTabVisible && holder.itemView.isAttachedToWindow()) {
                //吸顶
                adapter.onViewDetachedFromWindow(holder);
            } else if (!isOutTabVisible && holder.itemView.isAttachedToWindow()) {
                //吸顶消失
                adapter.onViewAttachedToWindow(holder);
            }
        }
    }

    private int getTabPos() {
        return getAdapter() == null ? -1 : getAdapter().getItemCount() - 1;
    }

    public void setMountingDistance(int mountingDistance) {
        mMountingDistance = mountingDistance;
    }

    //向上滑动
    public void scrollUp(final int distance) {
        mIsScrollUp = true;
        post(new Runnable() {
            @Override
            public void run() {
                scrollBy(0, distance);
                mIsScrollUp = false;
            }
        });
    }
}
