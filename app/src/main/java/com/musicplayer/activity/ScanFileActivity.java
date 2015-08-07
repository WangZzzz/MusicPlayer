package com.musicplayer.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.musicplayer.R;
import com.musicplayer.adapter.FileAdapter;
import com.musicplayer.model.ScanFile;
import com.musicplayer.util.MusicUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ScanFileActivity extends Activity implements View.OnClickListener{

    private FileAdapter adapter;
    private ArrayList<ScanFile> scanFiles = new ArrayList<ScanFile>();
    private TextView tv_currentpath;
    private ListView lv_scan_files;

    //记录歌曲的名称，做过滤用
    private ArrayList<String> titles = new ArrayList<String>();

    //返回键
    private ImageButton imgBtn_scanfile_back;

    //开始扫描键
    private Button bt_startscan;

    //扫描音乐时，显示的进度dialog
    private ProgressDialog dialog;

    //记录当前路径
    private String currentPath;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dialog.dismiss();
            Toast.makeText(ScanFileActivity.this, "扫描完成", Toast.LENGTH_SHORT).show();
            Log.i(ScanFileActivity.this.getClass().getSimpleName(), "" + titles.size());
            sendMusicBroadCast(MusicUtils.UPDATE_MUSIC_LIST);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_file);
        initView();
        initData();
    }

    private void initView(){
        tv_currentpath = (TextView)findViewById(R.id.tv_currentpath);
        lv_scan_files = (ListView)findViewById(R.id.lv_scan_files);
        bt_startscan = (Button)findViewById(R.id.bt_startscan);
        imgBtn_scanfile_back = (ImageButton)findViewById(R.id.imgBtn_scanfile_back);
        imgBtn_scanfile_back.setOnClickListener(this);
    }

    private void initData(){
        initDialog();
        adapter = new FileAdapter(this, R.layout.item_file, scanFiles);
        lv_scan_files.setAdapter(adapter);
        lv_scan_files.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ScanFile scanFile = scanFiles.get(i);
//                Toast.makeText(ScanFileActivity.this, scanFile.getFilePath(), Toast.LENGTH_SHORT).show();
                if(scanFile.isDirectory()){
                    File file = new File(scanFile.getFilePath());
                    initScanFiles(file);
                    tv_currentpath.setText(currentPath);
                }else{
                    Toast.makeText(ScanFileActivity.this, "已经是一个文件...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt_startscan.setOnClickListener(this);
        initScanFiles(Environment.getExternalStorageDirectory());
        tv_currentpath.setText(currentPath);
        adapter.notifyDataSetChanged();
    }

    private void initScanFiles(File file){
        scanFiles.clear();
//        adapter.notifyDataSetChanged();
        currentPath = file.getAbsolutePath();
        File[] files = file.listFiles();
        for(File tmp : files){
            String fileName = tmp.getName();
            if(fileName.startsWith(".")){
                //去掉.开头的隐藏文件
                continue;
            }
            String filePath = tmp.getAbsolutePath();
            boolean isDirectory = tmp.isDirectory();
            ScanFile scanFile = new ScanFile(filePath, fileName, false, isDirectory);
            scanFiles.add(scanFile);
        }
        Collections.sort(scanFiles, new FileComparator());
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_startscan:
                Toast.makeText(ScanFileActivity.this, "开始扫描", Toast.LENGTH_SHORT).show();
                dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(ScanFile scanFile : scanFiles){
                            if(scanFile.isChosen()){
                                scanFile(new File(scanFile.getFilePath()));
                            }
                        }
                        //至少扫描两秒，防止闪屏
                        handler.sendEmptyMessageDelayed(1, 2000);
                    }
                }).start();
                break;
            case R.id.imgBtn_scanfile_back:
                if(currentPath.endsWith("/")){
                    finish();
                }else{
                    String previousPath = getPreviousPath(currentPath);
//                    Toast.makeText(ScanFileActivity.this, previousPath, Toast.LENGTH_SHORT).show();
                    initScanFiles(new File(previousPath));
                    tv_currentpath.setText(currentPath);
                }
            default:
                break;
        }
    }

    /**
     * 重写返回键
     */
    @Override
    public void onBackPressed() {
        if(currentPath.endsWith("/storage/sdcard0")){
            finish();
        }else{
            String previousPath = getPreviousPath(currentPath);
//                    Toast.makeText(ScanFileActivity.this, previousPath, Toast.LENGTH_SHORT).show();
            initScanFiles(new File(previousPath));
            tv_currentpath.setText(currentPath);
        }
    }

    /**
     * 根据当前路径得到前一个路径
     * @param path
     * @return
     */
    private String getPreviousPath(String path){
        int cnt = 0;
        for(int i = 0; i < path.length(); i++){
            if(path.charAt(i) == '/'){
                cnt++;
            }
        }
        int tmp = 0;
        for(int i = 0; i < path.length(); i++){
            tmp++;
            if(path.charAt(i) == '/'){
                cnt--;
            }
            if(cnt == 0){
                break;
            }
        }
        return path.substring(0, tmp - 1);
    }

    private void initDialog(){
        dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("扫描中，请稍后...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    private void scanFile(File rootFile){
        for(int i=0; i< rootFile.listFiles().length; i++) {
            File childFile = rootFile.listFiles()[i];

            //假如是目录的话就继续调用getSDcardFile（）将childFile作为参数传递的方法里面
            if (childFile.isDirectory()) {
                scanFile(childFile);
            } else {
//                Log.i("ScanFileActivity", "@" + childFile.getName());
                //如果是文件的话，判断是不是以.mp3结尾，是就加入到List里面
                if (childFile.getName().endsWith(".mp3")) {
//                    Log.i("ScanFileActivity", childFile.getName());
                    titles.add(getSongTitle(childFile.getName()));
                    MusicUtils.scanSongs(ScanFileActivity.this, titles);
                }
            }
        }
    }

    /**
     * 将文件名后面的.mp3去掉
     * @param str
     * @return
     */
    private String getSongTitle(String str){
        int index = str.indexOf(".mp3");
        return str.substring(0, index);
    }

    //向前台音乐界面发送广播更新播放列表
    private void sendMusicBroadCast(int i){
        Intent intent = new Intent();
        intent.setAction(MusicUtils.MUSIC_SERVIE_CONTROL);
        intent.putExtra("control", i);
        sendBroadcast(intent);
    }

    //扫描到文件，按文件名排序用的
    private class FileComparator implements Comparator<ScanFile>{

        @Override
        public int compare(ScanFile lhs, ScanFile rhs) {
            return lhs.getFileName().toLowerCase().compareTo(rhs.getFileName().toLowerCase());
        }
    }
}
