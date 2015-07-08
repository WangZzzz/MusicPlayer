package com.musicplayer.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicplayer.R;


/**
 * Created by WangZ on 2015/6/24.
 */
public class MyDialog extends AlertDialog implements View.OnClickListener {

    private Context context;
    private TextView tv_dlg_content;
    private Button btn_dlg_yes;
    private Button btn_dlg_no;
    private String content;
    private CustomDialogListener customDialogListener;


    public interface CustomDialogListener{
        public void OnClick(View v);
    }

    public MyDialog(Context context, String content, CustomDialogListener customDialogListener) {
        super(context);
        this.context = context;
        this.content = content;
        this.customDialogListener = customDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog);
        this.setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView(){
        tv_dlg_content = (TextView)findViewById(R.id.tv_dlg_content);
        btn_dlg_yes = (Button)findViewById(R.id.btn_dlg_yes);
        btn_dlg_no = (Button)findViewById(R.id.btn_dlg_no);

        tv_dlg_content.setText(content);
        btn_dlg_yes.setOnClickListener(this);
        btn_dlg_no.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        customDialogListener.OnClick(v);
        dismiss();
    }
}
