package com.wency.petmanager.diary

import android.content.Context
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TagLayoutManager(val context: Context, val isFullyLayout: Boolean): RecyclerView.LayoutManager() {


    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        for (i in 0 .. itemCount.minus(1)){

        }
    }

    override fun supportsPredictiveItemAnimations(): Boolean {
        return  true
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return dx
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        val shouldOffset = 0

        return super.scrollVerticallyBy(dy, recycler, state)
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        removeAllViews()
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        val widthMode = View.MeasureSpec.getMode(widthSpec)
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        val widthSize = View.MeasureSpec.getSize(widthSpec)
        val heightSize = View.MeasureSpec.getSize(heightSpec)
        when (widthMode){
            View.MeasureSpec.UNSPECIFIED -> Log.d("LayoutManager", "WidthMode is unspecified")
        }

        removeAndRecycleAllViews(recycler)
        recycler.clear()



    }



}