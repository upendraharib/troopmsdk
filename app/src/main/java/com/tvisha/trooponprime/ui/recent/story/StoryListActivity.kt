package com.tvisha.trooponprime.ui.recent.story

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.MyApplication.Companion.appPreferences
import com.tvisha.trooponprime.MyApplication.Companion.troopClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.constants.Constants
import com.tvisha.trooponprime.databinding.StoryListLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.StoryModel
import com.tvisha.trooponprime.ui.recent.story.adapter.StoryListAdapter


class StoryListActivity :AppCompatActivity() {
    var adapter: StoryListAdapter? = null
    lateinit var dataBinding:StoryListLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.story_list_layout)
        setView()
    }
    fun setView(){
        dataBinding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        dataBinding.tvSend.setOnClickListener {
            createStory()
        }
        dataBinding.storyUserList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        adapter = StoryListAdapter(this,handler)
        dataBinding.storyUserList.adapter = adapter
        setList()
    }
    private var handler = @SuppressLint("HandlerLeak")
    object :Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1->{
                    readStory(msg.obj as StoryModel)
                }
                2->{
                    deleteStory(msg.obj as StoryModel)
                }
            }
        }
    }
    fun readStory(storyModel: StoryModel){
        Log.e("story==> "," read "+storyModel.id)
        var intent = Intent(this,StoryActivity::class.java)
        intent.putExtra("user_id", storyModel.user_id.toString())
        startActivity(intent)
        troopClient.storyViewed(storyModel.id)
    }
    fun deleteStory(storyModel: StoryModel){
        var list:ArrayList<Long> = ArrayList()
        list.add(storyModel.id)
        Log.e("story==> ","delete "+storyModel.id)
        troopClient.deleteStories(list)
    }
    fun setList(){
        troopClient.fetchOthersStories().observe(this, Observer {
            if (it!=null){
                adapter!!.setList(it as ArrayList<StoryModel>)
            }
        })
        troopClient.fetchMyStories().observe(this, Observer {
            Log.e("data==> ",""+(it!=null))
            if (it!=null && it.isNotEmpty()){
                dataBinding.llMyStories.visibility = View.VISIBLE
            }else{
                dataBinding.llMyStories.visibility = View.GONE
            }
        })
        dataBinding.llMyStories.setOnClickListener {
            var intent = Intent(this,StoryActivity::class.java)
            intent.putExtra("user_id", MyApplication.LOGINUSERTMID)
            startActivity(intent)
        }
    }
    var count = 0
    private fun createStory(){
        count++
        if (count==1) {
            troopClient.addTextStory("Hi Hello", "#cacaca", "poppins")
        }else if (count==2){
            troopClient.addImageStory("https://media.troopmessenger.com/desktop/attachments/1709291206289_ZwFxy/TroopMessenger_1709291206198.png")
        }else if (count==3){
            troopClient.addVideoStory("https://media.troopmessenger.com/android/attachments/1688392484170966/23_07_03_09_54_29.mp4")
        }
    }

}