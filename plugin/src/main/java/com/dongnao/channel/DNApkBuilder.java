package com.dongnao.channel;

import com.dongnao.channel.data.Apk;
import com.dongnao.channel.data.Constants;
import com.dongnao.channel.data.V2SignBlock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

/**
 * Created by xiang on 2017/5/20.
 */

public class DNApkBuilder {

    public static void generateChannel(String channel, Apk apk, File out) throws
            Exception {
        if (apk.isV2()) {
            generateV2Channel(channel, apk, out);
        } else if (apk.isV1()) {
            generateV1Channel(channel, apk, out);
        }
    }

    static void generateV1Channel(String channel, Apk apk, File out) throws IOException {
        FileOutputStream fos = new FileOutputStream(out);


        //获取第一部分内容
        int cdOffset = apk.getEocd().getCdOffset();
        int cdSize = apk.getEocd().getCdSize();
        RandomAccessFile apkFile = new RandomAccessFile(apk.getFile(), "r");
        byte[] cozeBytes = new byte[cdOffset];
        apkFile.read(cozeBytes);
        //写入到输出文件
        fos.write(cozeBytes);


        //写入cd
        apkFile.seek(cdOffset);
        byte[] cdBytes = new byte[cdSize];
        apkFile.read(cdBytes);
        fos.write(cdBytes);


        //写入eocd
        ByteBuffer data = apk.getEocd().getData();
        byte[] bytes = new byte[Constants.EOCD_COMMENT_LEN_OFFSET];
        data.get(bytes);
        data.flip();
        fos.write(bytes);
        byte[] channelBytes = channel.getBytes(Constants.CHARSET);
        ByteBuffer commentlen = ByteBuffer.allocate(2);
        commentlen.order(ByteOrder.LITTLE_ENDIAN);
        commentlen.putShort((short) channelBytes.length);
        fos.write(commentlen.array());

        fos.write(channelBytes);


        apkFile.close();
        fos.flush();
        fos.close();
    }

    static void generateV2Channel(String channel, Apk apk, File out) throws Exception {
        FileOutputStream fos = new FileOutputStream(out);
        //获取第一部分内容
        int v2Size = apk.getV2SignBlock().getData().capacity();
        int cdOffset = apk.getEocd().getCdOffset();
        int cdSize = apk.getEocd().getCdSize();
        int coze_len = cdOffset - v2Size;
        RandomAccessFile apkFile = new RandomAccessFile(apk.getFile(), "r");
        byte[] cozeBytes = new byte[coze_len];
        apkFile.read(cozeBytes);
        //写入到输出文件
        fos.write(cozeBytes);

        //写入签名块
        V2SignBlock v2SignBlock = apk.getV2SignBlock();
        ByteBuffer v2Block = v2SignBlock.getData();
        int capacity = v2Block.capacity();
        byte[] channelBytes = channel.getBytes(Constants.CHARSET);
        //新签名块总大小
        int block_size = capacity + 8 + 4 + channelBytes.length;
        //如果已经存在了我们要添加的渠道信息的id
        if (v2SignBlock.getPair().containsKey(Constants
                .APK_SIGNATURE_SCHEME_V2_CHANNEL_ID)) {
            //获取已经存在的v2渠道数据
            ByteBuffer v2ChannelValue = v2SignBlock.getPair().get(Constants
                    .APK_SIGNATURE_SCHEME_V2_CHANNEL_ID);
            block_size = block_size - 8 - 4 - v2ChannelValue.capacity();
        }
        ByteBuffer newV2Block = ByteBuffer.allocate(block_size);
        newV2Block.order(ByteOrder.LITTLE_ENDIAN);
        long blockSizeFieldValue = block_size - 8;
        newV2Block.putLong(blockSizeFieldValue);
        Set<Integer> ids = v2SignBlock.getPair().keySet();
        for (Integer id : ids) {
            //跳过已经存在的渠道id数据
            if (id != Constants.APK_SIGNATURE_SCHEME_V2_CHANNEL_ID) {
                ByteBuffer value = v2SignBlock.getPair().get(id);
                newV2Block.putLong(4 + value.capacity());
                newV2Block.putInt(id);
                newV2Block.put(value);
            }
        }
        //添加渠道信息
        newV2Block.putLong(4 + channelBytes.length);
        newV2Block.putInt(Constants.APK_SIGNATURE_SCHEME_V2_CHANNEL_ID);
        newV2Block.put(channelBytes);


        newV2Block.putLong(blockSizeFieldValue);
        newV2Block.put(Constants.APK_SIGNING_BLOCK_MAGIC);
        fos.write(newV2Block.array());

        //写入cd
        apkFile.seek(cdOffset);
        byte[] cdBytes = new byte[cdSize];
        apkFile.read(cdBytes);
        fos.write(cdBytes);

        //修改eocd中的内容
        byte[] bytes = new byte[16];
        ByteBuffer data = apk.getEocd().getData();
        data.get(bytes);
        data.getInt();
        byte[] comment = new byte[data.capacity() - data.position()];
        data.get(comment);
        ByteBuffer newEocd = ByteBuffer.allocate(data.capacity());
        newEocd.order(ByteOrder.LITTLE_ENDIAN);
        newEocd.put(bytes);
        newEocd.putInt(newV2Block.capacity() + coze_len);
        newEocd.put(comment);

        fos.write(newEocd.array());

        data.flip();
        apkFile.close();
        fos.flush();
        fos.close();
    }

}
