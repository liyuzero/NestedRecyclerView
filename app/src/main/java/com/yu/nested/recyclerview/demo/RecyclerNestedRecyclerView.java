package com.yu.nested.recyclerview.demo;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.yu.lib.common.utils.DisplayUtilsKt;
import com.yu.nested.library.NestedRecyclerView;
import com.yu.nested.recyclerview.R;

import org.jetbrains.annotations.NotNull;

public class RecyclerNestedRecyclerView extends OutTabFragment {

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_ad_tab;
    }

    @Override
    public void loadView(@NotNull View view) {
        super.loadView(view);

        final View mountingTabView = find(R.id.mountingTab);
        //设置吸顶偏移量，增加广告后，需要提前进入吸顶状态
        final int adHeight = (int) DisplayUtilsKt.dp2px(view.getContext(), 30);
        mNestedRecyclerView.setMountingDistance(adHeight);
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
                return mountingTabView;
            }
        });

        final View adView = view.findViewById(R.id.ad);
        adView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adView.setVisibility(View.GONE);
                mBottomTabView.setViewHeight(mNestedRecyclerView.getMeasuredHeight());
                mNestedRecyclerView.setMountingDistance(0);
                mNestedRecyclerView.scrollUp(adHeight);
            }
        });

        mNestedRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                //为了关闭广告后，吸顶栏
                mBottomTabView.setViewHeight(mNestedRecyclerView.getMeasuredHeight() - adHeight);
            }
        });
    }
}
