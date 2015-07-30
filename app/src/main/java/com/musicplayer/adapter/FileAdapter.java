package com.musicplayer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.model.ScanFile;

import java.util.ArrayList;

/**
 * Created by WangZ on 2015/7/30.
 */
public class FileAdapter extends BaseAdapter {

    private ArrayList<ScanFile> scanFiles = new ArrayList<ScanFile>();
    private Context context;
    private int resource;

    public FileAdapter(Context context, int resource, ArrayList<ScanFile> scanFiles) {
        this.context = context;
        this.resource = resource;
        this.scanFiles = scanFiles;
    }

    @Override
    public int getCount() {
        return scanFiles.size();
    }

    @Override
    public Object getItem(int i) {
        return scanFiles.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertview, ViewGroup viewGroup) {
        View view;
        ViewHolder viewHolder;
        final ScanFile scanFile = scanFiles.get(i);
        if(convertview == null){
            view = LayoutInflater.from(context).inflate(resource, null);
            viewHolder = new ViewHolder();
            viewHolder.iv_file_pic = (ImageView)view.findViewById(R.id.iv_file_pic);
            viewHolder.tv_file_name = (TextView)view.findViewById(R.id.tv_file_name);
            viewHolder.cb_file_ischosen = (CheckBox)view.findViewById(R.id.cb_file_ischosen);
            view.setTag(viewHolder);
        }else{
            view = convertview;
            viewHolder = (ViewHolder) view.getTag();
        }
        if(scanFile.isDirectory()){
            viewHolder.iv_file_pic.setImageResource(R.drawable.icon_folder);
        }else{
            viewHolder.iv_file_pic.setImageBitmap(null);
        }
        viewHolder.tv_file_name.setText(scanFile.getFileName());
        viewHolder.cb_file_ischosen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.i(FileAdapter.this.getClass().getSimpleName(), "onCheckedChanged : " + b);
                scanFile.setChosen(b);
            }
        });
        return view;
    }

    class ViewHolder{
        ImageView iv_file_pic;
        TextView tv_file_name;
        CheckBox cb_file_ischosen;
    }
}
