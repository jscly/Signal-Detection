package com.mashen.signaldetection;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Hx on 2016/10/21.
 */
public class SignalView extends LinearLayout {
    View view1,view2;
    TextView textView;
    int signal_dBm = -80;
    public SignalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.signal,this);
        LinearLayout root = (LinearLayout)findViewById(R.id.root);
        view1 = (View)findViewById(R.id.view1);
        view2 = (View)findViewById(R.id.view2);
        textView = (TextView)findViewById(R.id.signalText);
//        root.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                updateSignal(signal_dBm);
//            }
//        });

    }

    //更新信号强度
    public void updateSignal(int signal_dBm){

        if(signal_dBm>-45){
            view1.setBackground(new ColorDrawable(getResources().getColor(R.color.signal1)));
        }else if(signal_dBm>-60){
            view1.setBackground(new ColorDrawable(getResources().getColor(R.color.signal2)));
        }else if(signal_dBm>-75){
            view1.setBackground(new ColorDrawable(getResources().getColor(R.color.signal3)));
        }else if(signal_dBm>-90){
            view1.setBackground(new ColorDrawable(getResources().getColor(R.color.signal4)));
        }else{
            view1.setBackground(new ColorDrawable(getResources().getColor(R.color.signal5)));
        }


        int num = 100+signal_dBm;
        if(num<8){
            num=8;
        }

        LayoutParams lp1 = (LayoutParams) view1.getLayoutParams();
        lp1.width = dip2px(getContext(),num);
        view1.setLayoutParams(lp1);

        LayoutParams lp2 = (LayoutParams) view2.getLayoutParams();
        lp2.width = dip2px(getContext(),60-num);
        view2.setLayoutParams(lp2);

        textView.setText(signal_dBm+"dBm");
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}
