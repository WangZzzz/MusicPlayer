package com.musicplayer.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.db.MusicDbHelper;
import com.musicplayer.service.MusicService;
import com.musicplayer.util.MusicUtils;
import com.musicplayer.view.MyDialog;
import com.musicplayer.view.RoundImageView;

/**
 * Created by WangZ on 2015/7/1.
 */
public class PlayerActivity extends Activity implements View.OnClickListener {
    //播放状态 正在播放 暂停播放
    private TextView tv_music_status;

    //关闭界面
    private ImageButton imgBtn_music_close;

    //扫描歌曲按钮
    private ImageButton imgBtn_music_scan;

    //专辑封面
    private RoundImageView rdiv_song_cover;

    //歌曲名称
    private TextView tv_song_title;

    //歌手名
    private TextView tv_song_artist;

    //歌曲播放时间
    private TextView tv_song_time;

    //上一首控制按键
    private ImageButton imgBtn_music_last;

    //播放按键
    private ImageButton imgBtn_music_play;

    //下一首播放按键
    private ImageButton imgBtn_music_next;

    //播放模式按键
    private ImageButton imgBtn_music_model;

    //判断是否已经初始化播放列表的标记
    private boolean isInitSongList = false;

    //标记当前的播放模式
    private int CurrentModel = MusicUtils.MODEL_NORMAL;

    private MusicDbHelper dbHelper;

    //歌曲时长
    private int song_duration;

    //标识后台音乐服务是否已经准备完毕，若准备完毕，则可以点击播放按钮，否则不可以
    private boolean isPrepared = false;

    //接收后台音乐服务发送过来的广播信息
    private SongInfoRecevier songInfoRecevier = new SongInfoRecevier();

    //扫描音乐时，显示的进度dialog
    private ProgressDialog dialog;

    //表示扫描完成
    private static final int SCAN_OK = 1;

    //标识是否正在播放音乐
    private boolean isPlaying = false;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SCAN_OK:
                    dialog.dismiss();
                    Toast.makeText(PlayerActivity.this, "扫描成功", Toast.LENGTH_SHORT).show();
                    isInitSongList = true;
                    //开启后台音乐服务
                    Intent intent = new Intent(PlayerActivity.this, MusicService.class);
                    startService(intent);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_musicplayer);
        initView();
        initData();
    }

    private void initView(){
        tv_music_status = (TextView)findViewById(R.id.tv_music_status);
        rdiv_song_cover = (RoundImageView)findViewById(R.id.rdiv_song_cover);
        tv_song_title = (TextView)findViewById(R.id.tv_song_title);
        tv_song_artist = (TextView)findViewById(R.id.tv_song_artist);
        tv_song_time = (TextView)findViewById(R.id.tv_song_time);

        imgBtn_music_close = (ImageButton)findViewById(R.id.imgBtn_music_close);
        imgBtn_music_scan = (ImageButton)findViewById(R.id.imgBtn_music_scan);
        imgBtn_music_last = (ImageButton)findViewById(R.id.imgBtn_control_last);
        imgBtn_music_play = (ImageButton)findViewById(R.id.imgBtn_control_play);
        imgBtn_music_next = (ImageButton)findViewById(R.id.imgBtn_control_next);
        imgBtn_music_model = (ImageButton)findViewById(R.id.imgBtn_music_model);

        imgBtn_music_close.setOnClickListener(this);
        imgBtn_music_scan.setOnClickListener(this);
        imgBtn_music_last.setOnClickListener(this);
        imgBtn_music_play.setOnClickListener(this);
        imgBtn_music_next.setOnClickListener(this);
        imgBtn_music_model.setOnClickListener(this);
    }

    private void initData(){
        dbHelper = new MusicDbHelper(PlayerActivity.this, "Music", null, 1);
        initDialog();
        if(checkDatabase()){
            //开启后台音乐服务
            Intent intent = new Intent(PlayerActivity.this, MusicService.class);
            startService(intent);
            isInitSongList = true;
        }else{
            //弹窗提示用户去扫描播放列表
            showScanDialog();
        }
        registerSongInfoReceiver();
    }

    private void initDialog(){
        dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("扫描中，请稍后...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    /*
        注册广播接收器
     */
    private void registerSongInfoReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicUtils.UPDATE_SONGINFO_INTENT);
        intentFilter.addAction(MusicUtils.UPDATE_SONGPROGRESS_INTENT);
        registerReceiver(songInfoRecevier, intentFilter);
    }


    /*
        查询数据库中是否已经有播放列表了
     */
    private boolean checkDatabase(){
        String sql = "select * from song";
        Cursor cursor = dbHelper.querySQL(sql, null);
        if(cursor.moveToFirst()){
            return true;
        }else{
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("PlayerActivity", "onDestory");
        unregisterReceiver(songInfoRecevier);
        sendMusicBroadCast(MusicUtils.MUSICE_SERVICE_STOP);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgBtn_control_play:
//                Log.d("PlayerActiviyt", "imgBtn_control_play");
                if(!isInitSongList){
                    Toast.makeText(PlayerActivity.this, "请先初始化播放列表", Toast.LENGTH_SHORT).show();
                }
                if(isPrepared) {
                    if(!isPlaying) {
                        Log.d("PlayerActivity", "play");
                        sendMusicBroadCast(MusicUtils.CONTROL_PLAY);
                        isPlaying = true;
                        tv_music_status.setText("");
                        tv_music_status.setText("正在播放");
                        imgBtn_music_play.setBackground(null);
                        imgBtn_music_play.setImageResource(R.drawable.music_control_pause);
                    }else{
                        if(isPlaying){
                            sendMusicBroadCast(MusicUtils.CONTROL_PAUSE);
                            isPlaying = false;
                            tv_music_status.setText("");
                            tv_music_status.setText("暂停播放");
                            imgBtn_music_play.setImageResource(R.drawable.music_control_play);
                        }
                    }
                }
                break;
            case R.id.imgBtn_control_last:
                isPlaying = true;
                tv_music_status.setText("");
                tv_music_status.setText("正在播放");
                imgBtn_music_play.setBackground(null);
                imgBtn_music_play.setImageResource(R.drawable.music_control_pause);
                sendMusicBroadCast(MusicUtils.CONTROL_LAST);
                break;
            case R.id.imgBtn_control_next:
                isPlaying = true;
                tv_music_status.setText("");
                tv_music_status.setText("正在播放");
                imgBtn_music_play.setBackground(null);
                imgBtn_music_play.setImageResource(R.drawable.music_control_pause);
                sendMusicBroadCast(MusicUtils.CONTROL_NEXT);
                break;
            case R.id.imgBtn_music_model:
                //播放模式选择按钮
                if(CurrentModel == MusicUtils.MODEL_NORMAL){
                    CurrentModel = MusicUtils.MODEL_RANDOM;
                    imgBtn_music_model.setBackground(null);
                    imgBtn_music_model.setImageResource(R.drawable.music_model_random);
                    Toast.makeText(PlayerActivity.this, "随机播放", Toast.LENGTH_SHORT).show();
                    sendMusicBroadCast(MusicUtils.MODEL_RANDOM);
                }else if(CurrentModel == MusicUtils.MODEL_RANDOM){
                    CurrentModel = MusicUtils.MODEL_SINGLE;
                    imgBtn_music_model.setImageResource(R.drawable.music_model_single);
                    Toast.makeText(PlayerActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                    sendMusicBroadCast(MusicUtils.MODEL_RANDOM);
                }else if(CurrentModel == MusicUtils.MODEL_SINGLE){
                    CurrentModel = MusicUtils.MODEL_NORMAL;
                    imgBtn_music_model.setImageResource(R.drawable.music_model_normal);
                    Toast.makeText(PlayerActivity.this, "列表循环", Toast.LENGTH_SHORT).show();
                    sendMusicBroadCast(MusicUtils.MODEL_NORMAL);
                }
                break;
            case R.id.imgBtn_music_scan:
                showScanDialog();
                break;
            case R.id.imgBtn_music_close:
                finish();
            default:
                break;
        }
    }

    //弹窗提示用户去扫描音乐
    private void showScanDialog(){
        final MyDialog exitDialog = new MyDialog(this, "扫描播放列表？", new MyDialog.CustomDialogListener() {
            @Override
            public void OnClick(View v) {
                switch (v.getId()){
                    case R.id.btn_dlg_yes:
                        if(dialog != null){
                            dialog.show();
                        }
                        if(isPlaying){
                            sendMusicBroadCast(MusicUtils.MUSICE_SERVICE_STOP);
                            tv_music_status.setText("");
                            isPlaying = false;
                            imgBtn_music_play.setImageResource(R.drawable.music_control_play);
                        }
                       scanSongList();
                        break;
                    case R.id.btn_dlg_no:
                        Toast.makeText(PlayerActivity.this, "取消扫描", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
        exitDialog.show();
    }

    //扫描播放列表
    private void scanSongList(){
        //由于扫描音乐数据时耗时操作，所以新开一个线程去做这件事
        new Thread(new Runnable() {
            @Override
            public void run() {
                MusicUtils.scanSongs(PlayerActivity.this);
                handler.sendEmptyMessage(SCAN_OK);
            }
        }).start();
    }

    //向后台播放音乐的服务发送广播
    private void sendMusicBroadCast(int i){
        Intent intent = new Intent();
        intent.setAction(MusicUtils.MUSIC_RECEIVER_INTENT);
        intent.putExtra("control", i);
        sendBroadcast(intent);
    }

    private class SongInfoRecevier extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(MusicUtils.UPDATE_SONGINFO_INTENT)){
                tv_song_title.setText(intent.getStringExtra("song_title"));
                tv_song_artist.setText("歌手 ： " + intent.getStringExtra("song_artist"));
                song_duration = intent.getIntExtra("song_duration", 0);
                long song_id = intent.getLongExtra("song_id", 0);
                long song_album_id = intent.getLongExtra("song_album_id", 0);
                rdiv_song_cover.setImageBitmap(MusicUtils.getArtwork(PlayerActivity.this, song_id, song_album_id, true));
                tv_song_time.setText("");
                tv_song_time.setText("00:00 / " + MusicUtils.formatDuration(song_duration));
                isPrepared = true;//说明后台服务的音乐资料已经准备就绪
//                Log.d("MainActivity", song_duration + "");
            }else if(action.equals(MusicUtils.UPDATE_SONGPROGRESS_INTENT)){
                int progress = intent.getIntExtra("song_progress", 0);
                if(song_duration != 0){
                    String content = MusicUtils.formatDuration(progress) + " / " + MusicUtils.formatDuration(song_duration);
                    tv_song_time.setText(content);
                }
            }
        }
    }
}