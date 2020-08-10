package com.yu.nested.recyclerview.base;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;

public abstract class SingleRecyclerAdapter<D> extends RecyclerView.Adapter<SingleRecyclerAdapter.BaseSingleRecyclerVH> {
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemChildClickListener mOnItemChildClickListener;

    private List<D> mData;
    private int mLayoutRes;
    private HashSet<Integer> mChildIdSet;

    public SingleRecyclerAdapter(List<D> data, int layoutRes) {
        mData = data;
        mLayoutRes = layoutRes;
        mChildIdSet = new HashSet<>();
    }

    @NonNull
    @Override
    public BaseSingleRecyclerVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final BaseSingleRecyclerVH holder = new BaseSingleRecyclerVH(
                LayoutInflater.from(viewGroup.getContext()).inflate(
                        mLayoutRes,
                        viewGroup,
                        false
                )
        );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, holder.getLayoutPosition(), mData.get(holder.getLayoutPosition()));
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mOnItemLongClickListener != null) {
                    return mOnItemLongClickListener.onItemLongClick(v, holder.getLayoutPosition());
                }
                return false;
            }
        });

        for (int id: mChildIdSet) {
            holder.itemView.findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnItemChildClickListener != null) {
                        mOnItemChildClickListener.onChildItemClick(v, holder.getLayoutPosition());
                    }
                }
            });
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseSingleRecyclerVH baseSingleRecyclerVH, int i) {
        onBindData(baseSingleRecyclerVH, mData.get(i), i);
    }

    public abstract void onBindData(BaseSingleRecyclerVH holder, D data, int pos);

    @Override
    public int getItemCount() {
        return mData == null? 0: mData.size();
    }

    public static class BaseSingleRecyclerVH extends RecyclerView.ViewHolder {
        //view 缓存map
        private SparseArray<View> mViewMap = new SparseArray();

        public BaseSingleRecyclerVH(@NonNull View itemView) {
            super(itemView);
        }

        public <V extends View>V getView(int id) {
            View view = null;
            if(mViewMap.get(id) != null) {
                view = mViewMap.get(id);
            }
            if (view == null) {
                view = itemView.findViewById(id);
                mViewMap.put(id, view);
            }
            return (V) view;
        }
    }

    public void setOnItemClickListener(OnItemClickListener<D> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemChildClickListener(int childId, OnItemChildClickListener onItemChildClickListener) {
        mOnItemChildClickListener = onItemChildClickListener;
    }

    public interface OnItemClickListener<D> {
        void onItemClick(View view, int position, D data);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    public interface OnItemChildClickListener {
        void onChildItemClick(View view, int position);
    }
}
