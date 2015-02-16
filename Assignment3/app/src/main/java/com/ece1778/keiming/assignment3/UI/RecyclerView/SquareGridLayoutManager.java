package com.ece1778.keiming.assignment3.UI.RecyclerView;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ece1778.keiming.assignment3.Utils.OrientationUtils;

/**
 * Created by Kei-Ming on 2015-02-09.
 */
public class SquareGridLayoutManager extends GridLayoutManager{
    private Context mContext = null;

    public SquareGridLayoutManager (Context context, int span) {
        super(context, span);
        mContext = context;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);
        if (OrientationUtils.isLandscape(mContext)) {
            int spanCount = 3;
            this.setSpanCount(spanCount);
        } else {
            int spanCount = 2;
            this.setSpanCount(spanCount);
        }
    }
}
