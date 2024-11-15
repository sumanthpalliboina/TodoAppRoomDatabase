package com.sumanthacademy.myapplication.interfaces

import androidx.recyclerview.widget.RecyclerView

interface CallBackItemTouch {
    fun itemTouchOnMove(oldPosition:Int,newPosition:Int)
    fun itemTouchOnSwipe(viewHolder:RecyclerView.ViewHolder,position:Int)
}