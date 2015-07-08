package com.musicplayer.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by WangZ on 2015/6/29.
 */
public class MusicDbHelper extends SQLiteOpenHelper {

    private static final String CREATE_SONG = "create table if not exists song (id integer primary key," +
            "title text not null, album text not null, album_id integer not null,artist text not null, url text not null," +
            "duration integer not null, size integer not null)";


    public MusicDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_SONG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    /*
    执行插入、删除、更新等语句
 */
    public void execSQL(String sql, Object[] args){
        SQLiteDatabase db = this.getWritableDatabase();
        if(args == null){
            db.execSQL(sql);
        }else {
            db.execSQL(sql, args);
        }
    }

    /*
        执行查询语句
     */
    public Cursor querySQL(String sql, String[] args){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery(sql, args);
    }
}
