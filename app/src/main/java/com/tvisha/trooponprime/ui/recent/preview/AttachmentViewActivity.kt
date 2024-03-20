package com.tvisha.trooponprime.ui.recent.preview

import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.Glide
import com.tvisha.trooponprime.MyApplication
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.constants.Constants
import com.tvisha.trooponprime.databinding.AttachmentViewLayoutBinding
import com.tvisha.trooponprime.lib.clientModels.MediaModel
import com.tvisha.trooponprime.lib.utils.ConstantValues
import com.tvisha.trooponprime.lib.utils.Helper
import com.tvisha.trooponprime.ui.recent.customview.ZoomImageView

class AttachmentViewActivity:AppCompatActivity() {
    var entityId:String = ""
    var entityType:Int = 0
    var path : String = ""
    var senderName:String = ""
    var entityName:String = ""
    var createdAt:String= ""
    var adapter:ImagePagerAdapter? = null
    lateinit var dataBinding:AttachmentViewLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,R.layout.attachment_view_layout)
        entityId = intent.getStringExtra("entity_id")!!
        entityType = intent.getIntExtra("entity_type",0)
        path = intent.getStringExtra("path")!!
        senderName =intent.getStringExtra("sender_name")!!
        entityName = intent.getStringExtra("entity_name")!!
        createdAt = intent.getStringExtra("created_at")!!
        setView()
    }
    fun setView(){
        dataBinding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        dataBinding.tvSenderName.text = entityName
        dataBinding.tvMessageSendTime.text = senderName+" - "+Helper.getRecentListMessagesDateTime(createdAt)
        adapter = ImagePagerAdapter()
        dataBinding.viewPager.adapter = adapter
        loadImages()
        dataBinding.viewPager.addOnPageChangeListener(object :OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }
            override fun onPageSelected(position: Int) {
                if (position!=-1){
                    if (adapter!=null && adapter!!.list.size>0){
                        var data = adapter!!.list[position]
                        dataBinding.tvMessageSendTime.text = data.sender_name+" - "+Helper.getRecentListMessagesDateTime(data.created_at)

                    }
                }
            }
            override fun onPageScrollStateChanged(state: Int) {

            }


        })
    }
    private fun loadImages(){
        if (entityType==ConstantValues.Entity.USER) {
            MyApplication.troopClient.fetchUserMedia(entityId).observe(this, Observer {
                if (it!=null){
                    adapter!!.setTheList(it as ArrayList<MediaModel>)
                    adapter!!.notifyDataSetChanged()
                    if (!it.isEmpty()){
                        for (i in 0 until it.size){
                            if (it[i].attachment==path){
                                dataBinding.viewPager.currentItem = i
                                break
                            }
                        }
                    }
                }
            })
        }else {
            MyApplication.troopClient.fetchGroupMedia(entityId).observe(this, Observer {
                if (it!=null){
                    adapter!!.setTheList(it as ArrayList<MediaModel>)
                    adapter!!.notifyDataSetChanged()
                    if (!it.isEmpty()){
                        for (i in 0 until it.size){
                            if (it[i].attachment==path){
                                dataBinding.viewPager.currentItem = i
                                break
                            }
                        }
                    }
                }
            })
        }
    }
    inner class ImagePagerAdapter() : PagerAdapter() {
        var list = ArrayList<MediaModel>()
        override fun getCount(): Int {
            return list.size
        }
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater.inflate(R.layout.layout_image_viewer, container, false)

            var zoomImageView = view.findViewById<ZoomImageView>(R.id.zoomImageView)
            var video = view.findViewById<VideoView>(R.id.video)
            var video_button = view.findViewById<ImageView>(R.id.video_button)
            var webView = view.findViewById<WebView>(R.id.webView)
            var llAudioView = view.findViewById<LinearLayout>(R.id.llAudioView)
            var playAudio = view.findViewById<ImageView>(R.id.playAudio)
            var audioMessageTime = view.findViewById<TextView>(R.id.audioMessageTime)
            var sbaudioProgress = view.findViewById<SeekBar>(R.id.sbaudioProgress)
            var noPreviewAvailable = view.findViewById<ImageView>(R.id.noPreviewAvailable)

            var attachmentType = Helper.getFileIconPath(list[position].attachment)
            when(attachmentType){
                ConstantValues.Gallery.GALLERY_IMAGE->{
                    zoomImageView.visibility = View.VISIBLE
                    video.visibility = View.GONE
                    video_button.visibility = View.GONE
                    webView.visibility = View.GONE
                    llAudioView.visibility = View.GONE
                    noPreviewAvailable.visibility = View.GONE

                    Glide.with(this@AttachmentViewActivity).load(list[position].attachment)
                        .placeholder(ContextCompat.getDrawable(this@AttachmentViewActivity,R.drawable.attachment_pin_icon))
                        .error(ContextCompat.getDrawable(this@AttachmentViewActivity,R.drawable.attachment_pin_icon))
                        .into(zoomImageView)
                }
                ConstantValues.Gallery.GALLERY_VIDEO->{
                    zoomImageView.visibility = View.VISIBLE
                    video.visibility = View.VISIBLE
                    video_button.visibility = View.VISIBLE
                    webView.visibility = View.GONE
                    llAudioView.visibility = View.GONE
                    noPreviewAvailable.visibility = View.GONE
                    Glide.with(this@AttachmentViewActivity).load(list[position].attachment)
                        .placeholder(ContextCompat.getDrawable(this@AttachmentViewActivity,R.drawable.attachment_pin_icon))
                        .error(ContextCompat.getDrawable(this@AttachmentViewActivity,R.drawable.attachment_pin_icon))
                        .into(zoomImageView)
                }
                ConstantValues.Gallery.GALLERY_AUDIO->{
                    zoomImageView.visibility = View.GONE
                    video.visibility = View.GONE
                    video_button.visibility = View.GONE
                    webView.visibility = View.GONE
                    llAudioView.visibility = View.VISIBLE
                    noPreviewAvailable.visibility = View.GONE
                }
                else->{
                    if (attachmentType!=ConstantValues.Gallery.APK && attachmentType!=ConstantValues.Gallery.GALLERY_ZIP
                        && attachmentType!=ConstantValues.Gallery.GALLERY_OTHER) {
                        zoomImageView.visibility = View.GONE
                        video.visibility = View.GONE
                        video_button.visibility = View.GONE
                        webView.visibility = View.VISIBLE
                        llAudioView.visibility = View.GONE
                        noPreviewAvailable.visibility = View.GONE
                        val settings = webView.settings
                        settings.javaScriptEnabled = true
                        settings.allowContentAccess = true
                        webView.clearHistory()
                        webView.clearCache(true)
                        webView.isVerticalScrollBarEnabled = true
                        webView.isHorizontalScrollBarEnabled = true
                        webView.webViewClient =
                            AppWebViewClients()
                        if (attachmentType == ConstantValues.Gallery.GALLERY_DOC || attachmentType == ConstantValues.Gallery.GALLERY_XLS) {
                            webView.loadUrl("https://view.officeapps.live.com/op/embed.aspx?src=${list[position].attachment}")
                        }else if (attachmentType==ConstantValues.Gallery.GALLERY_PDF) {
                            webView.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=${list[position].attachment}}")
                        } else {
                            webView.loadUrl(list[position].attachment!!)
                        }
                    }else{
                        zoomImageView.visibility = View.GONE
                        video.visibility = View.GONE
                        video_button.visibility = View.GONE
                        webView.visibility = View.GONE
                        llAudioView.visibility = View.GONE
                        noPreviewAvailable.visibility = View.VISIBLE
                    }
                }
            }

            container.addView(view)

            return view
        }
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        fun setTheList(mediaModels: java.util.ArrayList<MediaModel>) {
            list = mediaModels
        }
        inner class AppWebViewClients : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }
        }

    }
}