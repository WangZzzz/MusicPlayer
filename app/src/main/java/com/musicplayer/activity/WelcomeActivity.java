package com.musicplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.musicplayer.R;

/**
 * Created by WangZ on 2015/7/2.
 */
public class WelcomeActivity extends Activity {

    private Button Btn_go;
    private Button Btn_countgo;
    private RelativeLayout layout_countdown;
    private ImageView iv_countdownnum;
    private boolean isSaveModel = true;
    private ImageView iv_save;
    int count = 4;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    //根据不同的count设置不同的倒计时数字
                    switch (count){
                        case 4:
                            iv_countdownnum.setImageResource(R.drawable.countdown_num4);
                            count--;
                            handler.sendEmptyMessageDelayed(0, 1000);
                            break;
                        case 3:
                            iv_countdownnum.setImageResource(R.drawable.countdown_num3);
                            count--;
                            handler.sendEmptyMessageDelayed(0, 1000);
                            break;
                        case 2:
                            iv_countdownnum.setImageResource(R.drawable.countdown_num2);
                            count--;
                            handler.sendEmptyMessageDelayed(0, 1000);
                            break;
                        case 1:
                            iv_countdownnum.setImageResource(R.drawable.countdown_num1);
                            count--;
                            handler.sendEmptyMessageDelayed(0, 1000);
                            break;
                        case 0:
                            startActivity(new Intent(WelcomeActivity.this, PlayerActivity.class));
                            finish();
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);
        initView();
    }

    private void initView(){
        Btn_go = (Button)findViewById(R.id.Btn_go);
        Btn_countgo = (Button)findViewById(R.id.Btn_countgo);
        layout_countdown = (RelativeLayout)findViewById(R.id.layout_countdown);
        iv_countdownnum = (ImageView)findViewById(R.id.iv_countdown_num);
        iv_save = (ImageView)findViewById(R.id.iv_save);

        Btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this, PlayerActivity.class));
                finish();
            }
        });

        Btn_countgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_countdown.setVisibility(View.VISIBLE);
                //设置透明度
                layout_countdown.getBackground().setAlpha(180);
                //设置省电模式图标
                if(!isSaveModel){
                    iv_save.setImageResource(R.drawable.savemodel_true);
                }else{
                    iv_save.setImageResource(R.drawable.savemodel_false);
                }
                //更新倒计时图标
                handler.sendEmptyMessageDelayed(0, 1000);
            }
        });

        iv_countdownnum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this, PlayerActivity.class));
                //销毁handler
                handler.removeMessages(0);
                finish();
            }
        });
    }
}
