package com.yu.nested.recyclerview.demo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yu.lib.common.utils.ToastUtil;
import com.yu.nested.library.NestedRecyclerView;
import com.yu.nested.recyclerview.R;
import com.yu.nested.recyclerview.base.BaseNestedFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NormalFragment extends BaseNestedFragment {
    private BottomTabView mBottomTabView;

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_normal;
    }

    @Override
    public void loadView(@NotNull final View view) {
        super.loadView(view);
        final NestedRecyclerView mNestedRecyclerView = view.findViewById(R.id.recyclerView);
        mNestedRecyclerView.setLayoutManager(new LinearLayoutManager(mNestedRecyclerView.getContext()));
        final List<Object> list = new ArrayList<>();
        for (int i=0; i<40; i++) {
            list.add(" == " + i);
        }

        //如果有数据加载的话，例如信息流，建议提前加载数据，避免卡顿，
        // 至于打点，请自行处理这种预加载数据情况下的 曝光逻辑

        //构建底部view，如果在onCreateViewHolder内生成并加载数据，滑出viewpager的时候会有明显卡顿，毕竟渲染需要时间，
        //对于复杂底部View，例如常见的信息流卡片，这个卡顿会十分明显，解决卡顿的一个方案就是提前预加载底部ViewPager
        //提前渲染View，只是new 出View类，并不会引发绘制，需要将其加到某个容器内提前渲染
        if(getArguments() != null && getArguments().getBoolean("cache", true)) {
            mBottomTabView = new BaseNestedFragment.BottomTabView(view.getContext(), getChildFragmentManager(), mNestedRecyclerView);
            FrameLayout cacheContainerView = find(R.id.cacheContainer);
            cacheContainerView.addView(mBottomTabView);
            //bottomTabView.updateData();
        }

        mNestedRecyclerView.setAdapter(new RecyclerView.Adapter() {

            @Override
            public int getItemViewType(int position) {
                return position == getItemCount() - 1? 1: 0;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if(viewType == 1) {
                    if(mBottomTabView != null && mBottomTabView.getParent() != null) {
                        ((ViewGroup)mBottomTabView.getParent()).removeView(mBottomTabView);
                    }
                    if(mBottomTabView == null) {
                        mBottomTabView = new BottomTabView(parent.getContext(), getChildFragmentManager(), mNestedRecyclerView);
                    }
                    //设置底部Tab View大小，必须！！！！！！！！！！！
                    if(mNestedRecyclerView.getMeasuredHeight() == 0) {
                        mNestedRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                mBottomTabView.setViewHeight(mNestedRecyclerView.getMeasuredHeight());
                            }
                        });
                    } else {
                        mBottomTabView.setViewHeight(mNestedRecyclerView.getMeasuredHeight());
                    }
                    mBottomTabView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBottomTabView.getViewHeight()));

                    return new RecyclerView.ViewHolder(mBottomTabView) {

                    };
                } else {
                    TextView titleView = new TextView(parent.getContext());
                    titleView.setTextSize(15);
                    titleView.setPadding(0, 20, 0, 20);
                    return new RecyclerView.ViewHolder(titleView) {

                    };
                }
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                if(getItemViewType(position) == 0) {
                    TextView titleView = (TextView) holder.itemView;
                    titleView.setText(String.valueOf(list.get(position)));
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ToastUtil.INSTANCE.showToast(v.getContext(), "点击 out：" + position);
                        }
                    });
                } else {

                }
            }

            @Override
            public int getItemCount() {
                return list.size() + 1;
            }
        });
        mNestedRecyclerView.setChildRecyclerViewHelper(new NestedRecyclerView.ChildRecyclerViewHelper() {
            @Override
            public RecyclerView getCurRecyclerView() {
                return mBottomTabView.getCurRecyclerView();
            }

        });
        mNestedRecyclerView.addOnActionListener(new NestedRecyclerView.OnActionListener() {
            @Override
            public void onTabMounting(boolean isMounting) {
                if(isMounting) {
                    ToastUtil.INSTANCE.showToast(mNestedRecyclerView.getContext(), "吸顶了");
                }
            }

            @Override
            public void onTabViewFirstShow() {

            }
        });
    }


}
