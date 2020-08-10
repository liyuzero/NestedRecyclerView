package com.yu.nested.recyclerview.base;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.jetbrains.annotations.NotNull;

public class LoadMoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int BASE_ITEM_TYPE_FOOTER = 200000;

    private SparseArray<FooterFactory> mFootViews = new SparseArray<>();
    private RecyclerView.Adapter mTargetAdapter;

    public LoadMoreAdapter(RecyclerView.Adapter targetAdapter) {
        if(targetAdapter == null){
            throw new NullPointerException("target adapter is null");
        }
        mTargetAdapter = targetAdapter;

        targetAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                LoadMoreAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                LoadMoreAdapter.this.notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                LoadMoreAdapter.this.notifyItemRangeChanged(positionStart, itemCount, payload);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                LoadMoreAdapter.this.notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                LoadMoreAdapter.this.notifyItemRangeRemoved(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                LoadMoreAdapter.this.notifyItemMoved(fromPosition, toPosition);
            }
        });
    }

    @Override
    public final int getItemViewType(int position) {
        if (isFooterView(position)) {
            return mFootViews.keyAt(position - getTargetItemCount());
        }
        return mTargetAdapter.getItemViewType(position);
    }


    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mFootViews.get(viewType) != null) {
            return new HeaderFooterViewHolder(mFootViews.get(viewType).createFooter(parent));
        }
        return mTargetAdapter.onCreateViewHolder(parent,viewType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
            if (isFooterView(position)) {
                ((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(true);
                return;
            } else {
                ((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(false);
            }
            holder.itemView.setLayoutParams(layoutParams);
        } else if(isFooterView(position)){
            return;
        }
        mTargetAdapter.onBindViewHolder(holder,position);
    }

    private boolean isFooterView(int position) {
        return position >= getTargetItemCount();
    }

    public LoadMoreAdapter addFootView(FooterFactory view) {
        mFootViews.put(BASE_ITEM_TYPE_FOOTER, view);
        this.notifyDataSetChanged();
        return this;
    }

    public void removeFooterView(FooterFactory view){
        int index = mFootViews.indexOfValue(view);
        mFootViews.removeAt(index);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return getFooterCount() + getTargetItemCount();
    }

    private int getTargetItemCount() {
        return mTargetAdapter.getItemCount();
    }

    private int getFooterCount() {
        return mFootViews.size();
    }

    /**
     * 处理GridLayoutManager
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int itemType = getItemViewType(position);
                    if (mFootViews.get(itemType) != null) {
                        return gridLayoutManager.getSpanCount();
                    }
                    if (spanSizeLookup != null)
                        return spanSizeLookup.getSpanSize(position);
                    return 1;
                }
            });
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                if(layoutManager == null) {
                    return;
                }
                int lastVisibleItemPosition = -1;
                int itemCount = 0;

                if(layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
                    lastVisibleItemPosition = manager.findLastVisibleItemPosition();
                    itemCount = manager.getItemCount();
                } else if(layoutManager instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) layoutManager;
                    int[] arr = new int[2];
                    manager.findLastVisibleItemPositions(arr);
                    lastVisibleItemPosition = Math.max(arr[0], arr[1]);
                }

                itemCount = layoutManager.getItemCount();
                if(!recyclerView.canScrollVertically(1) && recyclerView.canScrollVertically(-1)
                        && lastVisibleItemPosition == itemCount - 1 && mOnLoadMoreListener != null && !mIsLoadMore){
                    mIsLoadMore = true;
                    mOnLoadMoreListener.onLoadMore();
                }
            }
        });
    }

    /**
     * 处理StaggeredGridLayoutManager
     */
    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if (isFooterView(position)) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p =
                        (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    private static class HeaderFooterViewHolder extends RecyclerView.ViewHolder {
        HeaderFooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void stopLoadMore() {
        mIsLoadMore = false;
    }

    private boolean mIsLoadMore;
    private OnLoadMoreListener mOnLoadMoreListener;

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public interface FooterFactory {
        View createFooter(ViewGroup parent);
    }
}
