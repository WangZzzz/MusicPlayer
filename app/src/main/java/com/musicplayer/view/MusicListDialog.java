package com.musicplayer.view;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.musicplayer.R;
import com.musicplayer.adapter.MusicListAdapter;
import com.musicplayer.db.MusicDbHelper;
import com.musicplayer.model.Songinfo;

import java.util.ArrayList;

/**
 * Created by WangZ on 2015/7/16.
 */
public class MusicListDialog extends AlertDialog implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

    private ListView lv_dialog_musiclist;
    private DialogClickListener dialogClickListener;
    private MusicListAdapter adapter;
    private ArrayList<Songinfo> songList = new ArrayList<Songinfo>();
    private MusicDbHelper dbHelper;

    public MusicListDialog(Context context, DialogClickListener dialogClickListener) {
        super(context);
        this.dialogClickListener = dialogClickListener;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Songinfo songinfo = songList.get(i);
        Log.d("MusicListDialog", "click : " + songinfo.getTitle());
        dialogClickListener.onItemClick(adapterView, view, i, l);
        dismiss();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        Songinfo songinfo = songList.get(i);
        Log.d("MusicListDialog", "long : " + songinfo.getTitle());
        dialogClickListener.onItemLongClick(adapterView, view, i, l);
        dismiss();
        return false;
    }

    public interface DialogClickListener{
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l);
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_musiclistdialog);
        this.setCanceledOnTouchOutside(false);
        initView();
        initData();
    }

    private void initView(){
        lv_dialog_musiclist = (ListView)findViewById(R.id.lv_dialog_musiclist);
    }

    private void initData(){
        adapter = new MusicListAdapter(getContext(), R.layout.item_music, songList);
        lv_dialog_musiclist.setAdapter(adapter);
        dbHelper = new MusicDbHelper(getContext(), "Music", null, 1);
        initSongList();
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
            return;
        }
        adapter.notifyDataSetChanged();
    }
}
