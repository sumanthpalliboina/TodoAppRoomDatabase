package com.sumanthacademy.myapplication.helper

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sumanthacademy.myapplication.adapter.TodoAdapter
import com.sumanthacademy.myapplication.interfaces.CallBackItemTouch

class TodoItemTouchHelper(val callBackItemTouch: CallBackItemTouch) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags,swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        callBackItemTouch.itemTouchOnMove(viewHolder.absoluteAdapterPosition,target.absoluteAdapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        callBackItemTouch.itemTouchOnSwipe(viewHolder,viewHolder.absoluteAdapterPosition)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG){
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        } else {
            /*val foriGroundView = (viewHolder as TodoAdapter.TodoViewHolder)?.viewB
            getDefaultUIUtil().onDrawOver(c,recyclerView,foriGroundView,dX,dY,actionState,isCurrentlyActive)*/
        }

        //to show delete btn whn we swipe
    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if(actionState != ItemTouchHelper.ACTION_STATE_DRAG) {
            /*val foriGroundView = (viewHolder as TodoAdapter.TodoViewHolder)?.todoDetails
            getDefaultUIUtil().onDrawOver(c,recyclerView,foriGroundView,dX,dY,actionState,isCurrentlyActive)*/
        }

        //when swipe o smooth animation
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        //super.clearView(recyclerView, viewHolder)
        /*val foriGroundView = (viewHolder as TodoAdapter.TodoViewHolder)?.todoDetails
        getDefaultUIUtil().clearView(foriGroundView)*/
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
    }
}