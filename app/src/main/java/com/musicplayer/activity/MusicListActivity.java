package com.musicplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
public class MusicListActivity extends Activity {

    private ListView lv_music_list;
    private ArrayList<Songinfo> songList = new ArrayList<Songinfo>();
    private MusicListAdapter adapter;
    private MusicDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_musiclistactivity);
        initView();
        initData();
    }

    private void initView(){
        lv_music_list = (ListView)findViewById(R.id.lv_music_list);
    }

    private void initData(){
        dbHelper = new MusicDbHelper(this, "Music", null, 1);
        adapter = new MusicListAdapter(MusicListActivity.this, R.layout.item_music, songList);
        lv_music_list.setAdapter(adapter);
        initSongList();

        lv_music_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Songinfo songinfo = songList.get(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(MusicListActivity.this);
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
    }

    private void initSongList(){
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
        adapter.notifyDataSetChanged();
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
