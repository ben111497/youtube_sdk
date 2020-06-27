package com.example.youtubesdk

import android.app.VoiceInteractor
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.view.get
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : YouTubeBaseActivity() , YouTubePlayer.OnInitializedListener {
    //youtube字幕api==================
    private data class u2(
        val errCodes: List<Any>,
        val errMsgs: List<Any>,
        val errorInfo: List<Any>,
        val result: Result,
        val status: Int
    )

    data class Result(
        val audio: String,
        val collections: List<Collection>,
        val content: String,
        val createTime: Long,
        val language: Int,
        val messages: List<Message>,
        val privacy: Int,
        val teachers: List<Any>,
        val userID: String,
        val userName: String,
        val userPhoto: String,
        val videoID: String,
        val videoInfo: VideoInfo,
        val viewer: Int
    )

    data class Collection(
        val userID: String,
        val userName: String,
        val userPhoto: String
    )

    data class Message(
        val content: String,
        val sendTime: Long,
        val subMessage: List<Any>,
        val timeTag: Int,
        val uid: String,
        val userID: String,
        val userName: String,
        val userPhoto: String
    )

    data class VideoInfo(
        val captionResult: CaptionResult,
        val description: String,
        val duration: Int,
        val publishedAt: String,
        val thumbnails: String,
        val title: String,
        val translatedLanguage: Int,
        val type: Int,
        val videourl: String
    )

    data class CaptionResult(
        val collectionList: List<Any>,
        val collections: List<Any>,
        val results: List<ResultX>,
        val state: Int,
        val totalCorrectCount: Int,
        val totalRecordCount: Int
    )

    data class ResultX(
        val captions: List<Caption>,
        val language: String
    )

    data class Caption(
        val content: String,
        val contentSimple: String,
        val correctCount: Int,
        val highestRate: Int,
        val ipa: List<Ipa>,
        val pinyinList: List<Pinyin>,
        val practiceCount: Int,
        val recordCount: Int,
        val roman: List<Any>,
        val time: Int
    )

    data class Ipa(
        val pinyin: String,
        val text: String
    )

    data class Pinyin(
        val pinyinSplit: List<PinyinSplit>,
        val pinyinType: Int
    )

    data class PinyinSplit(
        val pinyin: String,
        val text: String
    )

    //=====================================================================
    private val API_KEY = "AIzaSyByTQ7JIj8xnasmTzaHjpkbLADPTgqPuUI"
    private val VIDEO_ID = "9nhhQhAxhjo"
    private val APIUrl = "https://api.italkutalk.com/api/video/detail"
    private val API_ID: String = "5edfb3b04486bc1b20c2851a"
    private val guest_key: String = "44f6cfed-b251-4952-b6ab-34de1a599ae4"
    private lateinit var u2_data: u2
    private lateinit var youtube: YouTubePlayer
    private lateinit var handler: Handler
    private var stat:Boolean=true
    private var play_stat=false
    private lateinit var captions:Array<String?>
    private val updateTextTask = object :Runnable{
        override fun run() {
            minusOneSecond()
            handler.postDelayed(this,300)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var player_view = findViewById<YouTubePlayerView>(R.id.player_view)
        player_view.initialize(API_KEY, this)
        val intentfilter = IntentFilter("MyMessage")
        registerReceiver(receiver, intentfilter)

        val json = "{\"guestKey\":\"$guest_key\",\"videoID\":\"$API_ID\",\"mode\":1}"

        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)

        val req = Request.Builder()
            .header("Content-Type", "application/json")
            .url(APIUrl)
            .post(body)
            .build()

        OkHttpClient().newCall(req).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                sendBroadcast(
                    Intent("MyMessage")
                        .putExtra("json", response.body()?.string())
                )
            }

            override fun onFailure(call: Call, e: IOException?) {
                Log.e("查詢失敗", "$e")
            }
        })
        //跳至指定選擇時間=======================================================================================================================
        listview.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
        youtube.seekToMillis((u2_data.result.videoInfo.captionResult.results[0].captions[position].time) * 1000) //millis
            stat=false
            val myListAdapter = MyListAdapter(this@MainActivity, captions,position)
            listview.adapter = myListAdapter
            if(position>5){
                    listview.setSelection(position)
                }
        })
        //按鈕暫停開始===================
        status.setOnClickListener {
            if(play_stat) {
                youtube.pause()
                status.setImageResource(R.drawable.start)
                play_stat=false
            }
            else{
                youtube.play()
                status.setImageResource(R.drawable.stop)
                play_stat=true
            }
        }

    }
        //廣播=============================================================================================================================
        private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                intent.extras?.getString("json")?.let {
                    u2_data = Gson().fromJson(it, u2::class.java)
                    captions = arrayOfNulls<String?>(u2_data.result.videoInfo.captionResult.results[0].captions.size)
                    for (i in 0 until u2_data.result.videoInfo.captionResult.results[0].captions.size) {
                        captions[i] =
                            u2_data.result.videoInfo.captionResult.results[0].captions[i].content
                    }
                    val myListAdapter = MyListAdapter(this@MainActivity, captions,-1)
                    listview.adapter = myListAdapter
                    handler= Handler(Looper.getMainLooper())
                    handler.post(updateTextTask)
                }

            }
        }
        //=======================================================================================================================
        //youtube初始化
        override fun onInitializationSuccess(
            p0: YouTubePlayer.Provider?,
            youTubePlayer: YouTubePlayer?,
            wasRestored: Boolean
        ) {
            if (youTubePlayer == null) {
                return
            }
            if (!wasRestored) {
                youtube = youTubePlayer
                youtube.cueVideo(VIDEO_ID)
                youtube.setPlaybackEventListener(object : YouTubePlayer.PlaybackEventListener {
                    override fun onSeekTo(p0: Int) {

                    }

                    override fun onBuffering(p0: Boolean) {

                    }

                    override fun onPlaying() {
                        status.setImageResource(R.drawable.stop)
                        play_stat=true
                    }

                    override fun onStopped() {
                        status.setImageResource(R.drawable.start)
                        play_stat=false
                    }

                    override fun onPaused() {
                        status.setImageResource(R.drawable.start)
                        play_stat=false
                    }
                })
            }
        }

        override fun onInitializationFailure(
            p0: YouTubePlayer.Provider?,
            p1: YouTubeInitializationResult?
        ) {
            Toast.makeText(this, "失敗", Toast.LENGTH_LONG).show()
        }

    //=======================================================================================================================
    //每秒監聽時間
        fun minusOneSecond() {
            if(play_stat==true){
                    countrol()
            }
        }
        fun countrol(){
            var youtune_time =(youtube.currentTimeMillis/1000)
            for (i in 0 until u2_data.result.videoInfo.captionResult.results[0].captions.size) {
                if(youtune_time==u2_data.result.videoInfo.captionResult.results[0].captions[i].time){
                    val myListAdapter = MyListAdapter(this@MainActivity, captions,i)
                    listview.adapter = myListAdapter
                    listview.setSelection(i)
                }

            }
        }

}

