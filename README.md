RecyclerView嵌套ViewPager嵌套RecyclerView

====

NestedRecyclerView

## 功能描述

1. 支持嵌套ViewPager嵌套RecyclerView，理论上也支持直接嵌套RecyclerView，不过感觉没什么意义，就不再提供demo

2. 一个NestedRecyclerView类处理所有滚动逻辑，不需要对ViewPager内的RecyclerView做特殊处理

3. 支持第三方的下拉刷新库，例如SmartRefreshLayout

4. 提供RecyclerView内的tab栏吸顶【适合简单的吸顶，以及不可操作动画吸顶，如京东app】 以及
 外部传入吸顶tab栏【适合需要动态改变吸顶tab高度的场景，例如吸顶广告，可操作的吸顶栏动画，或者
 吸顶栏位置有特殊要求等】
 两种方式实现吸顶，具体见demo，暂不提供动画相关demo，以后看情况添加
 
5. 提供底部视图缓存demo，避免滑出ViewPager时卡顿的问题

## 示例



## 使用方法

### 新增依赖

1. 在项目的根目录gradle新增仓库如下：

```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

2. 使用module依赖，新增依赖：

```
compile 'com.github.liyuzero:MaeBundlesVoice:1.0.0'
```

### 具体调用（详情见demo）

1. 布局，当做普通RecyclerView进行布局即可

```
        <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent">
        
                <com.yu.nested.library.NestedRecyclerView
                    android:id="@+id/recyclerView"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"/>
        
            </FrameLayout>

```

1. 初始化

```
        final NestedRecyclerView mNestedRecyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView正常初始化
        mNestedRecyclerView.setLayoutManager(new LinearLayoutManager(mNestedRecyclerView.getContext()));
        
        //底部tab栏 和 ViewPager
        BottomTabView tabView = new BottomTabView(getContext());
        //设置底部Tab View大小，必须！！！！！！！！！！！, 该高度一般等于NestedRecyclerView大小
        mNestedRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mBottomTabView.setViewHeight(mNestedRecyclerView.getMeasuredHeight());
            }
        });

        mNestedRecyclerView.setAdapter(new RecyclerView.Adapter() {
            if(viewType == 1) {
                //一般是最后一个view是底部tab栏以及ViewPager
                return new RecyclerView.ViewHolder(mBottomTabView) {
                
                };
            } else {
                TextView titleView = new TextView(parent.getContext());
                ....
            }
            
            
        });

        //初始化，返回值为当前 ViewPager内部 展示的 RecyclerView
        mNestedRecyclerView.setChildRecyclerViewHelper(new NestedRecyclerView.ChildRecyclerViewHelper() {
            @Override
            public RecyclerView getCurRecyclerView() {
                return mBottomTabView.getCurRecyclerView();
            }

            //提供外部自定义吸顶栏时需要实现的接口，方便实现各种自定义效果，例如广告展示，触摸动画等等
            public View getInnerTabView() {
                return null;
            }
    
            public View getOutTabView() {
                return null;
            }

        });
        //监听器
        mNestedRecyclerView.addOnActionListener(new NestedRecyclerView.OnActionListener() {
            @Override
            public void onTabMounting(boolean isMounting) {
                //吸顶状态监听
                if(isMounting) {
                    ToastUtil.INSTANCE.showToast(mNestedRecyclerView.getContext(), "吸顶了");
                }
            }

            @Override
            public void onTabViewFirstShow() {
                //tab栏首次展示出来，一般用作打点
            }
        });

```