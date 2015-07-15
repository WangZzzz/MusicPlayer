package com.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.musicplayer.R;
import com.musicplayer.model.Songinfo;
import com.musicplayer.util.MusicUtils;

import java.util.List;

/**
 * Created by WangZ on 2015/7/15.
 */
public class MusicListAdapter extends ArrayAdapter<Songinfo> {

    private int resourceId;

    public MusicListAdapter(Context context, int resource, List<Songinfo> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Songinfo songinfo = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_musicitem_title = (TextView)view.findViewById(R.id.tv_musicitem_title);
            viewHolder.tv_musicitem_time = (TextView)view.findViewById(R.id.tv_musicitem_time);
            viewHolder.tv_musicitem_artist = (TextView)view.findViewById(R.id.tv_musicitem_artist);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.tv_musicitem_title.setText(songinfo.getTitle());
        viewHolder.tv_musicitem_time.setText(MusicUtils.formatDuration(songinfo.getDuration()));
        viewHolder.tv_musicitem_artist.setText(songinfo.getArtist());

        return view;
    }

    class ViewHolder{
        TextView tv_musicitem_title;
        TextView tv_musicitem_artist;
        TextView tv_musicitem_time;
    }
}
