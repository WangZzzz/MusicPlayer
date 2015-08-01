package com.musicplayer.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.musicplayer.R;
import com.musicplayer.db.MusicDbHelper;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by WangZ on 2015/6/30.
 */
public class MusicUtils {
    //用户控制
    public static final int MUSICE_SERVICE_STOP = -1;

    public static final int CONTROL_PLAY = 0;
    public static final int CONTROL_PAUSE = 1;
    public static final int CONTROL_NEXT = 2;
    public static final int CONTROL_LAST = 3;

    //正常模式
    public static final int MODEL_NORMAL = 4;
    //随机播放模式
    public static final int MODEL_RANDOM = 5;
    //单曲循环模式
    public static final int MODEL_SINGLE = 6;

    //更新播放列表
    public static final int UPDATE_MUSIC_LIST = 7;

    //点击播放列表直接播放
    public static final int PLAY_MUSIC_LIST = 8;



    public static final String SP_NAME = "MUSIC_MODEL";
    //activity发送到service的广播名称
    public static final String MUSIC_SERVIE_CONTROL = "com.mtt.music.control";

    //service发送到activity的更新歌曲信息的广播名称
    public static final String UPDATE_SONGINFO_INTENT = "com.mtt.songinfo.update";
    //service发送到activity的更新进度的广播名称
    public static final String UPDATE_SONGPROGRESS_INTENT = "com.mtt.songprogress.update";

    //更新前端播放按钮
    public static final String UPDATE_PLAY_BUTTON = "com.mtt.update.playbutton";

    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static Bitmap mCachedBit = null;

    /*
        将播放模式、之前用户播放到的歌曲的索引写入sp
     */
    public static void writeData(Context context, int model, int index){
        if (null == context) {
            return;
        }
        Log.d("MusicUtils", model + "");
        SharedPreferences sp = context.getSharedPreferences(MusicUtils.SP_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = sp.edit();
        /*
            有可能只写入一个，当传入参数是-1时，不写入
         */
        if(model != -1) {
            editor.putInt("model", model);
        }
        if(index != -1) {
            editor.putInt("index", index);
        }
        editor.commit();
    }

    /*
        得到sp中的播放模式
     */
    public static int getModel(Context context){
        if(context == null){
            return MODEL_NORMAL;
        }
        SharedPreferences sp = context.getSharedPreferences(MusicUtils.SP_NAME, Context.MODE_APPEND);
        int model = sp.getInt("model", MODEL_NORMAL);
        Log.d("MusicUtils", model + "");
        return model;
    }

    /*
      得到sp中的播放模式
   */
    public static int getIndex(Context context){
        if(context == null){
            return MODEL_NORMAL;
        }
        SharedPreferences sp = context.getSharedPreferences(MusicUtils.SP_NAME, Context.MODE_APPEND);
        int model = sp.getInt("index", 0);
        Log.d("MusicUtils", model + "");
        return model;
    }

    /*
       使用媒体库内容提供者获取mp3信息
    */
    public static void scanSongs(Context context){
        MusicDbHelper dbHelper = new MusicDbHelper(context, "Music", null, 1);
        String sql = "delete from song";
        dbHelper.execSQL(sql, null);
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //遍历媒体库
        if(cursor.moveToFirst()){
            do{
                //歌曲编号
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //歌曲标题
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                //歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

                if((size > 1024*3072) && url.endsWith("mp3")) {//大于2M并且只能是mp3格式的，防止不兼容
                    String query = "insert into song (id,title,album,album_id,artist,url,duration,size) values(?,?,?,?,?,?,?,?)";
                    dbHelper.execSQL(query, new Object[]{id, title, album,album_id, artist, url, duration, size});
                }
            }while (cursor.moveToNext());
        }
    }

    public static void scanSongs(Context context, ArrayList<String> titles){
        MusicDbHelper dbHelper = new MusicDbHelper(context, "Music", null, 1);
/*        String sql = "delete from song";
        dbHelper.execSQL(sql, null);*/
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //遍历媒体库
        if(cursor.moveToFirst()){
            do{
                //歌曲编号
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //歌曲标题
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                //歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

//                Log.i("MusicUtils", title);

                if((size > 1024*3072) && url.endsWith("mp3") && mp3Filter(dbHelper, titles, title)) {//大于2M并且只能是mp3格式的，防止不兼容
                    String query = "insert into song (id,title,album,album_id,artist,url,duration,size) values(?,?,?,?,?,?,?,?)";
                    dbHelper.execSQL(query, new Object[]{id, title, album,album_id, artist, url, duration, size});
                }
            }while (cursor.moveToNext());
        }
    }

    /**
     * 查看数据库中是否已经有的数据中是否包含用户自定义扫描的数据，同时查看扫描到的音乐是否在用户自定义的列表中
     */
    private static boolean mp3Filter(MusicDbHelper dbHelper, ArrayList<String> titles, String title){
        if(titles.contains(title)){
            String sql = "select * from song where title = '" + title +"'";
            Log.i("MusicUtils", sql);
            Cursor cursor = dbHelper.querySQL(sql, null);
            if(cursor.moveToFirst()){
                String tmp = cursor.getString(cursor.getColumnIndex("title"));
                Log.i("MusicUtils", "false" + tmp);
                return false;
            }else{
                Log.i("MusicUtils", "true");
                return true;
            }
        }else{
            return false;
        }
    }


    /**
     * 时间转换
     * @param milliseconds
     * @return 输出00：00时间
     *
     * */
    public static String formatDuration(int milliseconds){
        int seconds = milliseconds / 1000;
        int secondPart = seconds % 60;
        int minutePart = seconds / 60;
        return (minutePart >= 10 ? minutePart : "0" + minutePart) + ":" + (secondPart >= 10 ? secondPart : "0" + secondPart);
    }

    public static Bitmap getArtwork(Context context, long song_id, long album_id,
                                    boolean allowdefault) {
        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly
            // from the file.
            if (song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefault) {
                return getDefaultArtwork(context);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context);
                        }
                    }
                } else if (allowdefault) {
                    bm = getDefaultArtwork(context);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }

        return null;
    }

    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        byte [] art = null;
        String path = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (FileNotFoundException ex) {

        }
        if (bm != null) {
            mCachedBit = bm;
        }
        return bm;
    }

    /*
        当没有封面图片时，默认返回的封面图片
     */
    private static Bitmap getDefaultArtwork(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeResource(context.getResources(),R.drawable.music_cover_default, opts);
    }
}
