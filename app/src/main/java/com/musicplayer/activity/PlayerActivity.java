package com.musicplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.adapter.MusicListAdapter;
import com.musicplayer.db.MusicDbHelper;
import com.musicplayer.model.Songinfo;
import com.musicplayer.service.MusicService;
import com.musicplayer.util.MusicUtils;
import com.musicplayer.view.MyDialog;
import com.musicplayer.view.RoundImageView;

import java.util.ArrayList;

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

    //播放列表
    private ImageButton imgBtn_music_list;

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
    private PlayerActivityRecevier recevier = new PlayerActivityRecevier();

    //扫描音乐时，显示的进度dialog
    private ProgressDialog dialog;

    //表示扫描完成
    private static final int SCAN_OK = 1;

    //标识是否正在播放音乐
    private boolean isPlaying = false;

    //以下是popupwindow需要的：
    private ListView lv_window_musiclist;
    private ArrayList<Songinfo> songList = new ArrayList<Songinfo>();
    private MusicListAdapter adapter;
    private PopupWindow popupWindow;
    private PopupWindow VolumePopupWindow;
    private View view;

    //音量按钮，单击弹出seekbar
    private ImageButton imgBtn_music_volume;
    private boolean isVolumeShown = false;
    private SeekBar sb_music_volume;
    private View volumeView;
    private View parentView;

    private AudioManager audioManager;
    private int currentVolume;
    private int maxVolume;

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
        parentView = LayoutInflater.from(this).inflate(R.layout.activity_musicplayer, null);
        initView();
        initData();
    }

    private void initView(){
        tv_music_status = (TextView)findViewById(R.id.tv_music_status);
        rdiv_song_cover = (RoundImageView)findViewById(R.id.rdiv_song_cover);
        tv_song_title = (TextView)findViewById(R.id.tv_song_title);
        tv_song_artist = (TextView)findViewById(R.id.tv_song_artist);
        tv_song_time = (TextView)findViewById(R.id.tv_song_time);

        imgBtn_music_list = (ImageButton)findViewById(R.id.imgBtn_music_list);
        imgBtn_music_close = (ImageButton)findViewById(R.id.imgBtn_music_close);
        imgBtn_music_scan = (ImageButton)findViewById(R.id.imgBtn_music_scan);
        imgBtn_music_last = (ImageButton)findViewById(R.id.imgBtn_control_last);
        imgBtn_music_play = (ImageButton)findViewById(R.id.imgBtn_control_play);
        imgBtn_music_next = (ImageButton)findViewById(R.id.imgBtn_control_next);
        imgBtn_music_model = (ImageButton)findViewById(R.id.imgBtn_music_model);
        imgBtn_music_volume = (ImageButton)findViewById(R.id.imgBtn_control_volume);


        imgBtn_music_list.setOnClickListener(this);
        imgBtn_music_close.setOnClickListener(this);
        imgBtn_music_scan.setOnClickListener(this);
        imgBtn_music_last.setOnClickListener(this);
        imgBtn_music_play.setOnClickListener(this);
        imgBtn_music_next.setOnClickListener(this);
        imgBtn_music_model.setOnClickListener(this);
        imgBtn_music_volume.setOnClickListener(this);
        imgBtn_music_volume.setOnClickListener(this);
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
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.i(PlayerActivity.this.getClass().getSimpleName(), maxVolume + "");
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
        intentFilter.addAction(MusicUtils.UPDATE_PLAY_BUTTON);
        registerReceiver(recevier, intentFilter);
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
        unregisterReceiver(recevier);
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
                break;
            case R.id.imgBtn_music_list:
                showWindow(view);
                break;
            case R.id.imgBtn_control_volume:
/*                if(!isVolumeShown){
                    isVolumeShown = true;
                    showVolumeWindow(view);
                }else{
                    isVolumeShown = false;
                    VolumePopupWindow.dismiss();
                }*/
                if(VolumePopupWindow != null){
                    if(!VolumePopupWindow.isShowing()){
                        showVolumeWindow(view);
                    }else{
                        VolumePopupWindow.dismiss();
                    }
                }else{
                    showVolumeWindow(view);
                }
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
                    case R.id.btn_dlg_all:
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
                    case R.id.btn_dlg_customed:
//                        Toast.makeText(PlayerActivity.this, "取消扫描", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PlayerActivity.this, ScanFileActivity.class);
                        startActivity(intent);
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
        intent.setAction(MusicUtils.MUSIC_SERVIE_CONTROL);
        intent.putExtra("control", i);
        sendBroadcast(intent);
    }

    private class PlayerActivityRecevier extends BroadcastReceiver {

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
            }else if(action.equals(MusicUtils.UPDATE_PLAY_BUTTON)){
                Log.d(PlayerActivity.this.getClass().getSimpleName(), "UPDATE_PLAY_BUTTON");
                isPlaying = true;
                tv_music_status.setText("");
                tv_music_status.setText("正在播放");
                imgBtn_music_play.setBackground(null);
                imgBtn_music_play.setImageResource(R.drawable.music_control_pause);
            }
        }
    }

    /**
     * 显示音乐播放列表的popupwindow
     * @param parent
     */
    private void showWindow(View parent){
        if(popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.popupwindow_musiclist, null);
            lv_window_musiclist = (ListView) view.findViewById(R.id.lv_window_musiclist);
            adapter = new MusicListAdapter(PlayerActivity.this, R.layout.item_music, songList);
            lv_window_musiclist.setAdapter(adapter);
            popupWindow = new PopupWindow(view, 800, 1000);
        }
        initSongList();
        adapter.notifyDataSetChanged();
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);

        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        //居中显示
        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);

        lv_window_musiclist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Songinfo songinfo = songList.get(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
                builder.setCancelable(false);
                builder.setMessage("删除所选歌曲？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteFromdb(songinfo.getTitle());
                        initSongList();
                        adapter.notifyDataSetChanged();
                        sendMusicBroadCast(MusicUtils.UPDATE_MUSIC_LIST);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
                return true;
            }
        });

        lv_window_musiclist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Songinfo songinfo = songList.get(i);
                //点击播放列表的歌曲，直接播放
                Intent intent = new Intent();
                intent.setAction(MusicUtils.MUSIC_SERVIE_CONTROL);
                intent.putExtra("control", MusicUtils.PLAY_MUSIC_LIST);
                intent.putExtra("music_id", songinfo.getId());
                sendBroadcast(intent);
                popupWindow.dismiss();
            }
        });
    }

    private void initSongList(){
        if(dbHelper == null)
            dbHelper = new MusicDbHelper(this, "Music", null, 1);
        Cursor cursor = dbHelper.querySQL("select * from song", null);
        songList.clear();
        if(cursor.moveToFirst()){
            Log.d("MainActivity", "true");
            do{
                long id = cursor.getLong(cursor.getColumnIndex("id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String album = cursor.getString(cursor.getColumnIndex("album"));
                long album_id = cursor.getLong(cursor.getColumnIndex("album_id"));
                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                String url = cursor.getString(cursor.getColumnIndex("url"));
                int duration = cursor.getInt(cursor.getColumnIndex("duration"));
                long size = cursor.getLong(cursor.getColumnIndex("size"));
//                Log.d("MainActivity", title + " @ " + url);
                Songinfo songinfo = new Songinfo();
                songinfo.setId(id);
                songinfo.setTitle(title);
                songinfo.setAlbum(album);
                songinfo.setAlbum_id(album_id);
                songinfo.setArtist(artist);
                songinfo.setUrl(url);
                songinfo.setDuration(duration);
                songinfo.setSize(size);
                songList.add(songinfo);
            }while(cursor.moveToNext());
        }else{
            Toast.makeText(this, "没有初始化播放列表...", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void deleteFromdb(String title){
        String sql = "delete from song where title = '" + title + "'";
        dbHelper.execSQL(sql, null);
    }

    /**
     * 显示音量的popupwindow
     */
    private void showVolumeWindow(View parent) {
        if (VolumePopupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            volumeView = layoutInflater.inflate(R.layout.popupwindow_musicvolume, null);
            sb_music_volume = (SeekBar) volumeView.findViewById(R.id.sb_music_volume);
            VolumePopupWindow = new PopupWindow(volumeView, 800, 150);
        }
        // 使其聚集
        VolumePopupWindow.setFocusable(true);
        // 设置允许在外点击消失
        VolumePopupWindow.setOutsideTouchable(true);

        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        VolumePopupWindow.setBackgroundDrawable(new BitmapDrawable());
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        //居中显示
        VolumePopupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);

        sb_music_volume.setMax(maxVolume);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        sb_music_volume.setProgress(currentVolume);

        sb_music_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
