package com.dongnao.channel;

import android.content.Context;
import android.util.Log;

import com.dongnao.channel.data.Apk;
import com.dongnao.channel.data.Constants;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by xiang on 2017/5/21.
 */

public class DNChannelHelper {

    private static String channel;

    private static boolean isReaded;

    public static String getChannel(Context context) {
        if (isReaded) {
            return channel;
        }
        isReaded = true;
        try {
            Apk apk = DNApkParser.parser(context.getApplicationInfo().sourceDir);
            if (apk.isV1()) {
                return v1Channel(apk);
            } else if (apk.isV2()) {
                return v2Channel(apk);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static String v1Channel(Apk apk) throws UnsupportedEncodingException {
        ByteBuffer data = apk.getEocd().getData();
        short commentlen = data.getShort(Constants.EOCD_COMMENT_LEN_OFFSET);
        if (commentlen == 0) {
            return null;
        }
        byte[] commentBytes = new byte[commentlen];
        data.position(Constants.EOCD_COMMENT_OFFSET);
        data.get(commentBytes);
        Log.d("channel","使用v1获取渠道信息");
        return new String(commentBytes, Constants.CHARSET);
    }

    private static String v2Channel(Apk apk) throws UnsupportedEncodingException {
        ByteBuffer byteBuffer = apk.getV2SignBlock().getPair().get(Constants
                .APK_SIGNATURE_SCHEME_V2_CHANNEL_ID);
        channel = new String(byteBuffer.array(), Constants.CHARSET);
        Log.d("channel","使用v2获取渠道信息");
        return channel;

    }

}
