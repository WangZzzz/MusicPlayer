package com.musicplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.adapter.MusicListAdapter;
import com.musicplayer.db.MusicDbHelper;
import com.musicplayer.model.Songinfo;
import com.musicplayer.util.MusicUtils;

import java.util.ArrayList;

/**
 * Created by WangZ on 2015/7/15.
 */
public class TestActivity extends Activity {

    private ListView lv_music_list;
    private ArrayList<Songinfo> songList = new ArrayList<Songinfo>();
    private MusicListAdapter adapter;
    private MusicDbHelper dbHelper;
    private PopupWindow popupWindow;
    private View view;

    private Button btn_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }

    private void initView(){
        btn_test = (Button)findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWindow(view);
            }
        });
    }

    private void initData(){
        dbHelper = new MusicDbHelper(this, "Music", null, 1);
        adapter = new MusicListAdapter(TestActivity.this, R.layout.item_music, songList);
        lv_music_list.setAdapter(adapter);
        initSongList();

        lv_music_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Songinfo songinfo = songList.get(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
                builder.setCancelable(false);
                builder.setMessage("删除？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteFromdb(songinfo.getId());
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
                return false;
            }
        });

        lv_music_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Songinfo songinfo = songList.get(i);
                //点击播放列表的歌曲，直接播放
                Intent intent = new Intent();
                intent.setAction(MusicUtils.MUSIC_RECEIVER_INTENT);
                intent.putExtra("control", MusicUtils.PLAY_MUSIC_LIST);
                intent.putExtra("music_id", songinfo.getId());
                sendBroadcast(intent);
                finish();
            }
        });
    }

    private void showWindow(View parent){
        if(popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.popupwindow_musiclist, null);
            lv_music_list = (ListView) view.findViewById(R.id.lv_window_musiclist);
            initSongList();
            adapter = new MusicListAdapter(TestActivity.this, R.layout.item_music, songList);
            lv_music_list.setAdapter(adapter);
            popupWindow = new PopupWindow(view, 800, 1000);
        }
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);

        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        int xPos = windowManager.getDefaultDisplay().getWidth() / 2
                - popupWindow.getWidth() / 2;
        Log.i("coder", "windowManager.getDefaultDisplay().getWidth()/2:"
                    + windowManager.getDefaultDisplay().getWidth() / 2);
                    //
        Log.i("coder", "popupWindow.getWidth()/2:" + popupWindow.getWidth() / 2);

        Log.i("coder", "xPos:" + xPos);

        popupWindow.showAsDropDown(parent, xPos, 0);

        lv_music_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Songinfo songinfo = songList.get(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
                builder.setCancelable(false);
                builder.setMessage("删除？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteFromdb(songinfo.getId());
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

        lv_music_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Songinfo songinfo = songList.get(i);
                //点击播放列表的歌曲，直接播放
                Intent intent = new Intent();
                intent.setAction(MusicUtils.MUSIC_RECEIVER_INTENT);
                intent.putExtra("control", MusicUtils.PLAY_MUSIC_LIST);
                intent.putExtra("music_id", songinfo.getId());
                sendBroadcast(intent);
                finish();
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
//        adapter.notifyDataSetChanged();
    }

    private void deleteFromdb(long id){
        String sql = "delete from song where id = " + id;
        dbHelper.execSQL(sql, null);
    }

    //向后台播放音乐的服务发送广播
    private void sendMusicBroadCast(int i){
        Intent intent = new Intent();
        intent.setAction(MusicUtils.MUSIC_RECEIVER_INTENT);
        intent.putExtra("control", i);
        sendBroadcast(intent);
    }
}
