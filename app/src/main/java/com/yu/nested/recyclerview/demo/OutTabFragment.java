package com.yu.nested.recyclerview.demo;

import android.graphics.Color;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.yu.nested.library.NestedRecyclerView;
import com.yu.nested.recyclerview.R;
import com.yu.nested.recyclerview.base.RelatedTabLayout;

import org.jetbrains.annotations.NotNull;

//该种模式适合tab栏有动画的设计形式，因为对背部tab做动画会和recyclerView的滑动互相影响
public class OutTabFragment extends PullRefreshFragment {
    private RelatedTabLayout mOutTabLayout;

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_out_tab;
    }

    @Override
    public void loadView(@NotNull View view) {
        super.loadView(view);
        mOutTabLayout = find(R.id.outTab);
        //需要同步内外两个tabLayout的滑动状态
        mBottomTabView.getTabLayout().setBackgroundColor(Color.GRAY);
        mOutTabLayout.setRelateScrollView(mBottomTabView.getTabLayout());
        mBottomTabView.getTabLayout().setRelateScrollView(mOutTabLayout);
        mOutTabLayout.setupWithViewPager(mBottomTabView.getViewPager());

        mNestedRecyclerView.setChildRecyclerViewHelper(new NestedRecyclerView.ChildRecyclerViewHelper() {
            @Override
            public RecyclerView getCurRecyclerView() {
                return mBottomTabView.getCurRecyclerView();
            }

            @Override
            public View getInnerTabView() {
                return mBottomTabView.getTabLayout();
            }

            @Override
            public View getOutTabView() {
                return mOutTabLayout;
            }
        });
    }
}
