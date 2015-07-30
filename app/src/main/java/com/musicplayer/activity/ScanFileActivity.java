package com.musicplayer.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.util.ArrayList;

public class ScanFileActivity extends Activity implements View.OnClickListener{

    private FileAdapter adapter;
    private ArrayList<ScanFile> scanFiles = new ArrayList<ScanFile>();
    private TextView tv_currentpath;
    private ListView lv_scan_files;

    //返回键
    private ImageButton imgBtn_scanfile_back;

    //开始扫描键
    private Button bt_startscan;

    //扫描音乐时，显示的进度dialog
    private ProgressDialog dialog;

    //记录当前路径
    private String currentPath;

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
        adapter.notifyDataSetChanged();
        currentPath = file.getAbsolutePath();
        File[] files = file.listFiles();
        for(File tmp : files){
            String fileName = tmp.getName();
            String filePath = tmp.getAbsolutePath();
            boolean isDirectory = tmp.isDirectory();
            ScanFile scanFile = new ScanFile(filePath, fileName, false, isDirectory);
            scanFiles.add(scanFile);
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_startscan:
                Toast.makeText(ScanFileActivity.this, "开始扫描", Toast.LENGTH_SHORT).show();
                dialog.show();
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
}
