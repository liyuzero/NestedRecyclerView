package com.yu.nested.recyclerview.base;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.yu.lib.common.ui.BaseFragment;
import com.yu.lib.common.utils.ToastUtil;
import com.yu.nested.library.NestedRecyclerView;
import com.yu.nested.library.manager.NestedLinearLayoutManager;
import com.yu.nested.recyclerview.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseNestedFragment extends BaseFragment {

    @Override
    public void loadView(@NotNull View view) {
        ((TextView) find(R.id.title)).setText(getClass().getSimpleName());
    }

    public static class BottomTabView extends FrameLayout {
        public MyFragment mCurFragment;
        private NestedRecyclerView mNestedRecyclerView;
        private View mRootView;
        private RelatedTabLayout mTabLayout;
        private ViewPager mViewPager;
        private int mViewHeight;

        public BottomTabView(@NonNull Context context, FragmentManager manager, NestedRecyclerView nestedRecyclerView) {
            super(context);
            mNestedRecyclerView = nestedRecyclerView;
            init(manager);
        }

        private void init(FragmentManager fragmentManager) {
            final View itemView = LayoutInflater.from(getContext()).inflate(R.layout.view_tab, this);
            mTabLayout = itemView.findViewById(R.id.tab);
            mRootView = itemView.findViewById(R.id.root);
            mViewPager = itemView.findViewById(R.id.pager);
            mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
                @NonNull
                @Override
                public Fragment getItem(int position) {
                    MyFragment fragment = new MyFragment();
                    Bundle bundle = new Bundle();
                    fragment.setArguments(bundle);
                    if (position == 0 && mCurFragment == null) {
                        mCurFragment = fragment;
                    }
                    if (mNestedRecyclerView != null) {
                        mNestedRecyclerView.addOnActionListener(fragment);
                    }
                    return fragment;
                }

                @Override
                public int getCount() {
                    return 3;
                }

                @Override
                public CharSequence getPageTitle(int position) {
                    return " + " + (position + 1) + " + ";
                }

                @Override
                public void setPrimaryItem(ViewGroup container, int position, Object object) {
                    super.setPrimaryItem(container, position, object);
                    mCurFragment = (MyFragment) object;
                }

            });
            mTabLayout.setupWithViewPager(mViewPager);
        }

        public void setViewHeight(int height) {
            mViewHeight = height;
            if (getLayoutParams() != null) {
                getLayoutParams().height = height;
                setLayoutParams(getLayoutParams());
            }
        }

        public int getViewHeight() {
            return mViewHeight;
        }

        public RecyclerView getCurRecyclerView() {
            return mCurFragment == null ? null : mCurFragment.mRecyclerView;
        }

        public RelatedTabLayout getTabLayout() {
            return mTabLayout;
        }

        public ViewPager getViewPager() {
            return mViewPager;
        }
    }

    public static class MyFragment extends Fragment implements NestedRecyclerView.OnActionListener {
        private RecyclerView mRecyclerView;
        private View mRootView;
        private int pageCount = 50;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            if (mRootView == null) {
                View view = mRootView = inflater.inflate(R.layout.fragment_test, null);

                RecyclerView recyclerView = mRecyclerView = view.findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new NestedLinearLayoutManager(recyclerView.getContext()));
                final List<Object> list = new ArrayList<>();
                for (int i = 0; i < pageCount; i++) {
                    list.add(" == " + i);
                }

                //NestedRecyclerView会拦截内部RecyclerView的触摸事件，自行处理所有的触摸和滚动逻辑，所以此处不适合用类似smartRefreshLayout这类实现机制的footer框架，
                //而需要使用基于ViewHolder实现的footer
                final LoadMoreAdapter adapter = new LoadMoreAdapter(new SingleRecyclerAdapter<Object>(list, R.layout.item_test) {
                    @Override
                    public void onBindData(BaseSingleRecyclerVH holder, Object data, final int pos) {
                        TextView titleView = holder.getView(R.id.title);
                        titleView.setText((String) data);
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ToastUtil.INSTANCE.showToast(v.getContext(), "点击了inner：" + pos);
                            }
                        });
                    }
                }).addFootView(new LoadMoreAdapter.FooterFactory() {
                    @Override
                    public View createFooter(ViewGroup parent) {
                        return LayoutInflater.from(parent.getContext()).inflate(R.layout.view_footer, parent, false);
                    }
                });
                adapter.setOnLoadMoreListener(new LoadMoreAdapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        mRootView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int curSize = list.size();
                                for (int i = curSize; i < pageCount + curSize; i++) {
                                    list.add(" == " + i);
                                }
                                adapter.notifyDataSetChanged();
                                adapter.stopLoadMore();
                            }
                        }, 500);
                    }
                });
                recyclerView.setAdapter(adapter);

            } else {
                if (mRootView.getParent() != null) {
                    ((ViewGroup) mRootView.getParent()).removeView(mRootView);
                }
            }

            return mRootView;
        }

        @Override
        public void onTabMounting(boolean isMounting) {
            //吸顶置顶
            if (mRecyclerView != null && !isMounting) {
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.scrollToPosition(0);
                    }
                });
            }
        }

        @Override
        public void onTabViewFirstShow() {

        }


    }
}
