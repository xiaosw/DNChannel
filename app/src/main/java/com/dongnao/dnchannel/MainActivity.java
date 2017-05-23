package com.dongnao.dnchannel;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.dongnao.channel.DNChannelHelper;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getChannel(View view) {
        Toast.makeText(this, "渠道信息是:" + DNChannelHelper.getChannel(this), 0).show();
    }
}
