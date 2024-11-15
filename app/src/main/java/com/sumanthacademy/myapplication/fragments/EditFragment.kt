package com.sumanthacademy.myapplication.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.sumanthacademy.myapplication.ui.MainActivity
import com.sumanthacademy.myapplication.model.Todo
import com.sumanthacademy.myapplication.databinding.FragmentEditBinding
import com.sumanthacademy.myapplication.model.entity.TodoItem
import com.sumanthacademy.myapplication.util.AppConstants

class EditFragment : DialogFragment(), View.OnClickListener {

    lateinit var b:FragmentEditBinding

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentEditBinding.inflate(inflater,container,false)
        return b.root
    }

    companion object {
        const val TAG = "EditFragment"

        @JvmStatic
        fun newInstance(position:Int,currentTodo:TodoItem) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putInt(AppConstants.POSITION,position)
                    putInt(AppConstants.IMAGE_INT,currentTodo.image)
                    putString(AppConstants.TITLE, currentTodo.title)
                    putString(AppConstants.STATUS, currentTodo.status)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListers()

        b.title?.apply {
            setText(arguments?.getString(AppConstants.TITLE,"Default"))
        }
        b.status?.apply {
            setText(arguments?.getString(AppConstants.STATUS,"Default"))
        }
    }

    fun setListers(){
        b.cancelBtn.setOnClickListener(this)
        b.saveBtn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view){
            b.cancelBtn -> {
                println("cancelBtn")
                dismiss()
            }
            b.saveBtn -> {
                var updatedTitle = b.title.text.toString()
                var updatedStatus = b.status.text.toString()
                var position = requireArguments().getInt(AppConstants.POSITION)
                var image = requireArguments().getInt(AppConstants.IMAGE_INT,0)
                (activity as MainActivity).editAndSaveTodo(position,
                    TodoItem(image,updatedTitle,updatedStatus)
                )
                dismiss()
            }
        }
    }
}