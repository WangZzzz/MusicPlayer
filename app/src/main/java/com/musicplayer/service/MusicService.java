package com.musicplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.musicplayer.db.MusicDbHelper;
import com.musicplayer.model.Songinfo;
import com.musicplayer.util.MusicUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by WangZ on 2015/6/29.
 *  音乐播放服务，还需要两个广播接收器，一个用来监听用户的来电情况，当来电时，需要停止播放，电话结束或挂断时，需要继续播放
 *      一个广播接收器负责接收用户的指令，来播放、暂停、停止、下一首音乐
 */
public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private MusicDbHelper dbHelper;
    //播放模式
    private int model = MusicUtils.MODEL_NORMAL;
    private ArrayList<Songinfo> songList = new ArrayList<Songinfo>();
    //记录当前播放位置
    private int currentIndex = 0;
    //一个记录当前mediaplayer是否是被释放掉了
    private boolean isRelease = false;

    private MusicControlReceiver musicControlReceiver = new MusicControlReceiver();

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    Songinfo songinfo = (Songinfo)msg.obj;
//                    play();
                    updateSongInfo(songinfo);
                    break;
                case 1:
                    Songinfo songinfo1 = (Songinfo)msg.obj;
                    play();
                    updateSongInfo(songinfo1);
//                    updateProgress();
                    break;
                case 2:
                    updateProgress();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MusicServie", "onDestory");
        unregisterReceiver(musicControlReceiver);
        //退出的时候还需要记录当前播放的索引
        MusicUtils.writeData(MusicService.this, -1, currentIndex);
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            isRelease = true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //注册广播接收器
        registerMusicReceiver();

        //从sp中获取之前用户设置的播放模式
        model = MusicUtils.getModel(this);

        //初始化数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerMusicReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ANSWER");
        intentFilter.addAction(MusicUtils.MUSIC_RECEIVER_INTENT);
        registerReceiver(musicControlReceiver, intentFilter);
    }

    /*
        初始化各种数据
     */
    private void initData(){
        //初始化数据库和播放列表
        dbHelper = new MusicDbHelper(this, "Music", null, 1);
        initList();
        if(songList.size() == 0){
            return;
        }
        //初始化MediaPlayer
        if(mediaPlayer != null){
//            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();

        /*
            从sp中读取用户上一次播放的音乐
        */
        currentIndex = MusicUtils.getIndex(MusicService.this);
        PrepareSource(currentIndex);

        //通过handler告诉主线程，已经准备完毕数据,可以播放了
        Message message = Message.obtain();
        message.what = 0;
        message.obj = songList.get(currentIndex);
        handler.sendMessage(message);

        //监听播放完成事件，根据不同的播放模式，选择下一首歌曲
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(model == MusicUtils.MODEL_SINGLE){
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    PrepareSource(currentIndex);
                    mediaPlayer.start();
                    updateSongInfo(songList.get(currentIndex));
                    updateProgress();
                }else {
                    playNextSong();
                }
            }
        });
    }

    /*
        从数据库中初始化播放列表
     */
    private boolean initList(){
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
            return true;
        }else{
            return false;
        }
    }

    /*
        准备播放资源
     */
    private void PrepareSource(int index){
        if(index < 0 || index >= songList.size()){
            throw new RuntimeException("超出范围");
        }
        Songinfo songinfo = songList.get(index);

        try {
            mediaPlayer.setDataSource(this, Uri.parse(songinfo.getUrl()));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //根据模式的不同，下一首播放的不同
    private void playNextSong(){
        if(mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        switch (model){
            //单曲循环时点击下一首和正常播放时点击下一首逻辑一样
            case MusicUtils.MODEL_SINGLE:
            case MusicUtils.MODEL_NORMAL:
                //当前索引+1，看是否溢出
                if((currentIndex + 1) > (songList.size() - 1))
                {
                    currentIndex = 0;
                }else{
                    currentIndex = currentIndex + 1;
                }
                PrepareSource(currentIndex);
                //通过handler告诉主线程，已经准备完毕数据,可以播放了
                Message message1 = Message.obtain();
                message1.what = 1;
                message1.obj = songList.get(currentIndex);
                handler.sendMessage(message1);
                break;
            case MusicUtils.MODEL_RANDOM:
                //随机选取一首歌播放
                Random random = new Random();
                currentIndex = random.nextInt(songList.size() - 1);
                PrepareSource(currentIndex);
                //通过handler告诉主线程，已经准备完毕数据,可以播放了
                Message message2 = Message.obtain();
                message2.what = 1;
                message2.obj = songList.get(currentIndex);
                handler.sendMessage(message2);
                break;
            default:
                break;
        }
    }
    //播放前一首音乐
    private void playLastSong(){
        if(mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        switch (model){
            //单曲循环时点击上一首和正常播放时点击上一首逻辑一样
            case MusicUtils.MODEL_SINGLE:
            case MusicUtils.MODEL_NORMAL:
                //当前索引-1，看是否溢出
                if((currentIndex - 1) < 0)
                {
                    currentIndex = songList.size() - 1;
                }else{
                    currentIndex = currentIndex - 1;
                }
                PrepareSource(currentIndex);
                //通过handler告诉主线程，已经准备完毕数据,可以播放了
                Message message1 = Message.obtain();
                message1.what = 1;
                message1.obj = songList.get(currentIndex);
                handler.sendMessage(message1);
                break;
            case MusicUtils.MODEL_RANDOM:
                //随机选取一首歌播放
                Random random = new Random();
                currentIndex = random.nextInt(songList.size() - 1);
                PrepareSource(currentIndex);
                //通过handler告诉主线程，已经准备完毕数据,可以播放了
                Message message2 = Message.obtain();
                message2.what = 1;
                message2.obj = songList.get(currentIndex);
                handler.sendMessage(message2);
                break;
            default:
                break;
        }
    }

    //播放音乐
    private void play(){
        if(mediaPlayer != null){
            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
                updateProgress();
            }
        }
    }

    //暂停
    private void pasue(){
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }
        }
    }


    /*
        更新播放歌曲信息
     */
    private void updateSongInfo(Songinfo songinfo){
        Intent intent = new Intent();
        intent.setAction(MusicUtils.UPDATE_SONGINFO_INTENT);
        intent.putExtra("song_id", songinfo.getId());
        intent.putExtra("song_title", songinfo.getTitle());
        intent.putExtra("song_artist", songinfo.getArtist());
        intent.putExtra("song_album_id", songinfo.getAlbum_id());
        intent.putExtra("song_duration", songinfo.getDuration());
        sendBroadcast(intent);
    }

    /*
    更新播放进度
     */
    private void updateProgress() {
        if (!isRelease){
            if(mediaPlayer.isPlaying()) {
                Log.d("MussicServie", "updateProgress");
                int progress = mediaPlayer.getCurrentPosition();
                Intent intent = new Intent();
                intent.setAction(MusicUtils.UPDATE_SONGPROGRESS_INTENT);
                intent.putExtra("song_progress", progress);
                sendBroadcast(intent);
                handler.sendEmptyMessageDelayed(2, 1000);
            }
        }
    }


    private class MusicControlReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MusicUtils.MUSIC_RECEIVER_INTENT)) {
                int control = intent.getIntExtra("control", -1);
                switch (control) {
                    //根据用户传来的不同的参数执行不同的操作
                    case MusicUtils.CONTROL_PLAY:
                        //播放音乐
                        play();
                        break;
                    case MusicUtils.CONTROL_PAUSE:
                        //暂停音乐
                        pasue();
                        break;
                    case MusicUtils.CONTROL_NEXT:
                        //下一首音乐
                        playNextSong();
                        break;
                    case MusicUtils.CONTROL_LAST:
                        playLastSong();
                        break;
                    /*
                        以下是不同的播放模式，正常、单曲、随机
                     */
                    case MusicUtils.MODEL_NORMAL:
                        model = MusicUtils.MODEL_NORMAL;
                        Log.d("MusicService", "model : " + model);
                        MusicUtils.writeData(MusicService.this, model, -1);
                        break;
                    case MusicUtils.MODEL_RANDOM:
                        model = MusicUtils.MODEL_RANDOM;
                        Log.d("MusicService", "model : " + model);
                        MusicUtils.writeData(MusicService.this, model, -1);
                        break;
                    case MusicUtils.MODEL_SINGLE:
                        model = MusicUtils.MODEL_SINGLE;
                        Log.d("MusicService", "model : " + model);
                        MusicUtils.writeData(MusicService.this, model, -1);
                        break;
                    case MusicUtils.MUSICE_SERVICE_STOP:
                        Log.d("MusicServie", "MUSICE_SERVICE_STOP");
                        stopSelf();
                        break;
                    case MusicUtils.UPDATE_MUSIC_LIST:
                        initList();
                        break;
                    case MusicUtils.PLAY_MUSIC_LIST:
                        if(mediaPlayer != null) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                            mediaPlayer.reset();
                        }
                        long music_id = intent.getLongExtra("music_id", -1);
                        if(music_id == -1){
                            break;
                        }else{
                            int index = 0;
                            for(Songinfo songinfo : songList){
                                if(songinfo.getId() == music_id){
                                    PrepareSource(index);
                                    mediaPlayer.start();
                                    //还需要把前端界面播放按钮状态更新
                                    Intent intent1 = new Intent();
                                    intent1.setAction(MusicUtils.UPDATE_PLAY_BUTTON);
                                    sendBroadcast(intent1);
                                    updateSongInfo(songinfo);
                                    updateProgress();
                                }else{
                                    index++;
                                }
                            }
                        }
                    default:
                        break;
                }
            }else if(action.equals("android.intent.action.ANSWER")){
            /*
                 监听来电的广播接收器
            */
                TelephonyManager telephonymanager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                switch (telephonymanager.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING:// 当有来电时候，暂停音乐，可我试了试，只是把声音降低而已
                        pasue();
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK://挂断电话时，继续播放
                        play();
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
