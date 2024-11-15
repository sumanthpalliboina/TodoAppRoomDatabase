package com.sumanthacademy.myapplication.ui

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData.Item
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.sumanthacademy.myapplication.App
import com.sumanthacademy.myapplication.R
import com.sumanthacademy.myapplication.ViewModel.TodoItemTableViewModel
import com.sumanthacademy.myapplication.ViewModel.TodoItemTableViewModelFactory
import com.sumanthacademy.myapplication.ViewModel.TodoLive
import com.sumanthacademy.myapplication.ViewModel.TodoRemainder
import com.sumanthacademy.myapplication.ViewModel.TodoViewModel
import com.sumanthacademy.myapplication.adapter.TodoAdapter
import com.sumanthacademy.myapplication.databinding.ActivityMainBinding
import com.sumanthacademy.myapplication.fragments.DeleteFragment
import com.sumanthacademy.myapplication.fragments.EditFragment
import com.sumanthacademy.myapplication.global.BaseActivity
import com.sumanthacademy.myapplication.helper.TodoItemTouchHelper
import com.sumanthacademy.myapplication.interfaces.CallBackItemTouch
import com.sumanthacademy.myapplication.interfaces.OnTodoClickListener
import com.sumanthacademy.myapplication.interfaces.OnTodoDeleteClickListener
import com.sumanthacademy.myapplication.interfaces.OnTodoRemainderClickListener
import com.sumanthacademy.myapplication.model.Todo
import com.sumanthacademy.myapplication.model.entity.TodoItem
import com.sumanthacademy.myapplication.receivers.NotificationReceiver
import com.sumanthacademy.myapplication.receivers.PositiveBtnInNotificationReceiver
import com.sumanthacademy.myapplication.services.PositiveBtnNotificationService
import com.sumanthacademy.myapplication.util.AppConstants
import com.sumanthacademy.myapplication.util.Helper
import com.sumanthacademy.myapplication.util.SPUtil
import com.sumanthacademy.myapplication.util.setExactHrAndMinute
import java.util.Calendar
import java.util.Collections

class MainActivity : BaseActivity(),View.OnClickListener, OnTodoClickListener,
    OnTodoDeleteClickListener, OnTodoRemainderClickListener, CallBackItemTouch {

    lateinit var activityMainBinding: ActivityMainBinding

    /*var todoItems:ArrayList<Todo> = ArrayList<Todo>()*/

    var todoItemsDesc:List<TodoItem> = ArrayList()
    var todoItemsAsc:List<TodoItem> = ArrayList()

    lateinit var todoAdapter: TodoAdapter
    private lateinit var todoViewModel: TodoViewModel

    private final val CHANNEL_ID = "1"
    lateinit var notificationMangerCompat:NotificationManagerCompat
    lateinit var builder:NotificationCompat.Builder
    lateinit var positiveBtnInNotificationReceiver: PositiveBtnInNotificationReceiver

    lateinit var todoItemTableViewModel: TodoItemTableViewModel

    var refreshed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate")
        positiveBtnInNotificationReceiver = PositiveBtnInNotificationReceiver()

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = activityMainBinding.root
        setContentView(view)

        setLayoutManger()
        setTodoItemTableViewModel()
        listenThroughObserver()

        /*persistTodos()*/

        activityMainBinding.todosRecyclerView.setHasFixedSize(false)
        activityMainBinding.todosRecyclerView.itemAnimator = null
        todoAdapter = TodoAdapter(this,this,this,this)
        activityMainBinding.todosRecyclerView.adapter = todoAdapter

        setListeners()

        Handler(Looper.getMainLooper()).postDelayed(
            {
                PopupIntro(this,packageName).showDialog(){ it ->
                    println("response from popup intro -> ${it}")
                }
            }, AppConstants.POP_INTRO_DELAY.toLong()
        )


        activityMainBinding.swipeRefreshLayout.setOnRefreshListener {
            if (refreshed){
                refreshed = false
            } else {
                refreshed = true
            }
            todoViewModel.isRefreshed.value = true
            /*activityMainBinding.swipeRefreshLayout.isRefreshing = false
            Collections.shuffle(this.todoItems, java.util.Random(System.currentTimeMillis()))
            *//*SPUtil.saveTodos(this.todoItems)*//*
            todoAdapter.notifyDataSetChanged()*/
        }

        startLocalNotification()
    }

    fun setTodoItemTableViewModel(){
        todoViewModel = ViewModelProvider(this).get(TodoViewModel::class.java)
        val todoItemTableViewModelFactory = TodoItemTableViewModelFactory((application as App).repository)
        todoItemTableViewModel = ViewModelProvider(this,todoItemTableViewModelFactory).get(TodoItemTableViewModel::class.java)
    }

    fun listenThroughObserver(){
        todoItemTableViewModel.myAllTodos.observe(this, Observer { todos ->
            this.todoItemsAsc = todos
            todoAdapter.setTodosToAdapter(todos)
        })
        todoItemTableViewModel.myAllDescTodos.observe(this, Observer { todos ->
            this.todoItemsDesc = todos
        })
        todoViewModel.isRefreshed.observe(this){it ->
            if (it && refreshed) {
                todoAdapter.setTodosToAdapter(todoItemsDesc)
                activityMainBinding.swipeRefreshLayout.isRefreshing = false
            } else {
                todoAdapter.setTodosToAdapter(todoItemsAsc)
                activityMainBinding.swipeRefreshLayout.isRefreshing = false
            }
        }
        todoViewModel.deletedData.observe(this){ it ->
            Toast.makeText(this,"${it.todo.title} is deleted",Toast.LENGTH_SHORT).show()
        }
        todoViewModel.remainderData.observe(this){ it ->
            if (it.isSet == true) {
                Toast.makeText(this,"${it.todo.title} todo remainder attached to alarm. you will be notified soon",Toast.LENGTH_LONG).show()
            }
        }

        /*var todoItemTouchHelper = TodoItemTouchHelper(this)
        var itemTouchHelper = ItemTouchHelper(todoItemTouchHelper)
        itemTouchHelper.attachToRecyclerView(activityMainBinding.todosRecyclerView)*/

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                todoItemTableViewModel.deleteTodo(todoAdapter.getItem(viewHolder.absoluteAdapterPosition))
            }

        }).attachToRecyclerView(activityMainBinding.todosRecyclerView)

    }

    fun setLayoutManger(){
        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            activityMainBinding.todosRecyclerView.layoutManager = GridLayoutManager(this,2)
        }else{
            activityMainBinding.todosRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.VERTICAL,false)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLayoutManger()
    }

    fun hideKeyboard(){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activityMainBinding.input.windowToken,0)
    }

    fun addTodo(){
        val text = activityMainBinding.input.text.toString()
        if (text.isNotEmpty()){
            val todoItem = TodoItem(R.drawable.sumanth_photo_qqqzsw,text,"Not Started Yet")
            todoItemTableViewModel.insertTodo(todoItem)
            activityMainBinding.input.setText("")
            hideKeyboard()
        }else{
            hideKeyboard()
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("Empty Input")
            dialogBuilder.setIcon(R.drawable.baseline_warning_24)
            dialogBuilder.setMessage("Please Provide Your input")
            dialogBuilder.setPositiveButton("Ok",DialogInterface.OnClickListener{dialog,_ ->
                dialog.dismiss()
            })
            dialogBuilder.setNegativeButton("Cancel",DialogInterface.OnClickListener{dialog,_ ->
                showSnackbar()
                dialog.dismiss()
            })
            val dialog = dialogBuilder.create()
            dialog.show()
        }
    }

    fun showSnackbar(){
        val snackbar = Snackbar.make(activityMainBinding.main,"Provide your valid input",Snackbar.LENGTH_SHORT)
        snackbar.setAction("close"){
            snackbar.dismiss()
        }
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.view.setBackgroundColor(Color.RED)
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }

    fun getPendingIntentUsingIntentAction(dynamicReceiverAction:String):PendingIntent{
        val intent = Intent(dynamicReceiverAction)
        intent.putExtra("positiveBtnClickStatus","Add Your Todos!")
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           return PendingIntent.getBroadcast(applicationContext,Helper.INTRO_NOTIFICATION_ID,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            return PendingIntent.getBroadcast(applicationContext,Helper.INTRO_NOTIFICATION_ID,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    fun getPendingIntentUsingJobIntentService(): PendingIntent{  //service will not work for pending intent of action for notification
        val intent = Intent(this@MainActivity,PositiveBtnNotificationService::class.java)
        PositiveBtnNotificationService.enqueueWork(applicationContext,intent)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.getService(this@MainActivity,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            return PendingIntent.getService(this@MainActivity,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    fun startLocalNotification(){
        builder = NotificationCompat.Builder(this@MainActivity,CHANNEL_ID)
        val pendingIntent = getPendingIntentUsingIntentAction("com.sumanthacademy.myapplication.SENDPOSTIVEEVENT")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID,"1",NotificationManager.IMPORTANCE_DEFAULT)
            val manager:NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            val mainActivityIntent = Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("FROM_NOTIFICATION",true)
            }
            builder.setSmallIcon(R.drawable.todos_animation)
                .setContentTitle("Do Your Todos")
                .setContentText("If you want to manage your todos efficiently, Do it in this famous Todos App?")
                .setLargeIcon(resources.getDrawable(R.drawable.mind).toBitmap())
                .setContentIntent(PendingIntent.getActivity(applicationContext,0,mainActivityIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(resources.getString(R.string.intro_notification_text)))
                .addAction(R.drawable.todos_animation,"Add Todos",pendingIntent)
        } else {
            val mainActivityIntent = Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("FROM_NOTIFICATION",true)
            }
            builder.setSmallIcon(R.drawable.todos_animation)
                .setContentTitle("Do Your Todos")
                .setContentText("Do you want to finish your todos?") //it will not displays if we maintain bigText for scroll
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(PendingIntent.getActivity(applicationContext,0,mainActivityIntent,PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(resources.getString(R.string.intro_notification_text)))
                .addAction(R.drawable.todos_animation,"Add Todos",pendingIntent)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf((Manifest.permission.POST_NOTIFICATIONS)),1)
            return
        }
        with(NotificationManagerCompat.from(this@MainActivity)){
            notify(Helper.INTRO_NOTIFICATION_ID,builder.build())
        }
    }

    /*handle the case where the user grants the permission*/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        } else {
            Toast.makeText(this@MainActivity,"post notification permissions are not accepted",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        println("onStart")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(positiveBtnInNotificationReceiver, IntentFilter("com.sumanthacademy.myapplication.SENDPOSTIVEEVENT"),Context.RECEIVER_EXPORTED)
        } else{
            registerReceiver(positiveBtnInNotificationReceiver,IntentFilter("com.sumanthacademy.myapplication.SENDPOSTIVEEVENT"))
        }
    }

    override fun onResume() {
        super.onResume()
        println("onResume")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(positiveBtnInNotificationReceiver, IntentFilter("com.sumanthacademy.myapplication.SENDPOSTIVEEVENT"),Context.RECEIVER_EXPORTED)
        } else{
            registerReceiver(positiveBtnInNotificationReceiver,IntentFilter("com.sumanthacademy.myapplication.SENDPOSTIVEEVENT"))
        }
    }

    override fun onPause() {
        super.onPause()
        println("onPause")
    }

    override fun onStop() {
        super.onStop()
        println("onStop")
        unregisterReceiver(positiveBtnInNotificationReceiver)
    }

    override fun onRestart() {
        super.onRestart()
        println("onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy")
        unregisterReceiver(positiveBtnInNotificationReceiver)
    }

    fun setListeners(){
        activityMainBinding.addBtn.setOnClickListener(this)
        /*activityMainBinding.input.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE){
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(activityMainBinding.input.windowToken,0)
                true
            }else{
                false
            }
        }*/
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when(view.id){
                R.id.addBtn -> addTodo()
            }
        }
    }

    override fun onTodoItemClickListener(position: Int, tod: TodoItem) {
        var item = todoAdapter.getItem(position)
        item.let{
            val fragmentManager = supportFragmentManager
            val editFragment = EditFragment.newInstance(position,TodoItem(item.image ?: 0,item.title.toString() ?: "default",item.status.toString() ?: "default"))
            if (fragmentManager != null){
                editFragment.show(fragmentManager, EditFragment.TAG)
                editFragment.isCancelable = false
            }
        }
    }

    fun editAndSaveTodo(position: Int,todo: TodoItem){
        val item = todoAdapter.getItem(position)
        todo.id = item.id
        todo?.let {
            todoItemTableViewModel.updateTodo(todo)
        }
    }

    override fun deleteTodoClickListener(position: Int) {
        var item = todoAdapter.getItem(position)
        item.let {
            val fragmentManager = supportFragmentManager
            val deleteFragment = DeleteFragment.newInstance(position,item)
            if (fragmentManager != null) {
                deleteFragment.show(fragmentManager, DeleteFragment.TAG)
                //deleteFragment.isCancelable = false
            }
        }
    }

    fun deleteTodo(position: Int){
        val todoItem = todoAdapter.getItem(position)
        todoItemTableViewModel.deleteTodo(todoItem)
    }

    fun showBottomSheetDialogForDelete(position:Int){
        var dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.fragment_delete,null)
        val closeBtn = view.findViewById<AppCompatButton>(R.id.cancel)
        val deleteBtn = view.findViewById<AppCompatButton>(R.id.delete)
        val title = view.findViewById<AppCompatTextView>(R.id.myTitle)
        val status = view.findViewById<AppCompatTextView>(R.id.myStatus)

        val item = this.todoAdapter.getItem(position)

        title.text = item.title.toString()
        status.text =item.status.toString()

        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        deleteBtn.setOnClickListener{
            todoViewModel.deletedData.value = TodoLive(true,this.todoAdapter.getItem(position))
            deleteTodo(position)
            dialog.dismiss()
        }

        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.setContentView(view)

        dialog.setCancelable(true) //false -> this will prevent the user drag down the dialog to dismiss
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun setTodoRemainderTimeWithBroadcast(todo: TodoItem, hour:Int, minute:Int){
        var calendar = Calendar.getInstance()
        calendar = calendar.setExactHrAndMinute(hour,minute)
        val intent = Intent(applicationContext,NotificationReceiver::class.java)
        intent.putExtra("name",todo.title)
        intent.putExtra("progress",todo.status)
        val notificationId = System.currentTimeMillis().toInt()
        val pendingIntent = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.getBroadcast(applicationContext,notificationId,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(applicationContext,notificationId,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val alarmManager:AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,AlarmManager.INTERVAL_DAY,pendingIntent)
        todoViewModel.remainderData.value = TodoRemainder(true,todo)
    }

    override fun todoRemainderClickListener(position: Int) {
        var item = todoAdapter.getItem(position)
        val calendar = Calendar.getInstance()
        val currentHr = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMin = calendar.get(Calendar.MINUTE)
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(currentHr)
            .setMinute(currentMin)
            .setTitleText("Set Todo Remainder")
            .build()
        timePicker.show(supportFragmentManager,"TimePicker")
        timePicker.addOnPositiveButtonClickListener {
            setTodoRemainderTimeWithBroadcast(item,timePicker.hour, timePicker.minute)
        }
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int) {
        println("itemTouchOnMove")
    }

    override fun itemTouchOnSwipe(viewHolder: RecyclerView.ViewHolder, position: Int) {
       println("itemTouchOnSwipe")
    }


}