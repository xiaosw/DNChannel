package com.dongnao.channel;

import com.dongnao.channel.data.Apk;
import com.dongnao.channel.data.Constants;
import com.dongnao.channel.data.Eocd;
import com.dongnao.channel.data.V2SignBlock;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by xiang on 2017/5/20.
 */

public class DNApkParser {

    public static Apk parser(String path) throws Exception {
        return parser(new File(path));
    }

    public static Apk parser(File file) throws Exception {
        Apk apk = new Apk(file);
        RandomAccessFile apkFile = new RandomAccessFile(file, "r");
        //查找eocd数据
        ByteBuffer eocdBuffer = findEocd(apkFile, Constants.EOCD_COMMENT_OFFSET);
        if (null == eocdBuffer)
            eocdBuffer = findEocd(apkFile, Constants.EOCD_COMMENT_OFFSET + Constants
                    .EOCD_COMMENT_MAX_LEN);
        if (null == eocdBuffer) {
            apkFile.close();
            throw new Exception(file.getPath() + " 不是一个标准的apk文件");
        }
        apk.setEocd(new Eocd(eocdBuffer));
        //获取核心目录数据
        int cdOffset = apk.getEocd().getCdOffset();
//        apkFile.seek(cdOffset);

        //查找v2签名块
        apk.setV2SignBlock(findV2SignBlock(apkFile, cdOffset));
        if (!apk.isV2()) {
            apk.setV1(isV1(file));
        }
        if (!apk.isV1() && !apk.isV2()) {
            apkFile.close();
            throw new Exception(file.getPath() + " 没有签名");
        }
        apkFile.close();
        return apk;
    }

    static boolean isV1(File file) throws IOException {
        JarFile jarFile = new JarFile(file);
        JarEntry manifest = jarFile.getJarEntry("META-INF/MANIFEST.MF");
        if (null == manifest) {
            return false;
        }
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().matches("META-INF/\\w+\\.SF")) {
                return true;
            }

        }
        return false;
    }


    static V2SignBlock findV2SignBlock(RandomAccessFile apkFile, int offset) throws IOException {
        //第二个v2签名块中的block of size 的偏移
        int block_2size_offset = offset - 24;
        apkFile.seek(block_2size_offset);
        ByteBuffer v2BlockMagicBuffer = ByteBuffer.allocate(24);
        v2BlockMagicBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //读取v2签名块中第二个block size 与magic
        apkFile.read(v2BlockMagicBuffer.array());
        //读取blocksize
        long block2_size = v2BlockMagicBuffer.getLong();
        //读取magic
        byte[] block_magic = new byte[16];
        v2BlockMagicBuffer.get(block_magic);
        //如果magic等于v2签名的magic
        if (Arrays.equals(Constants.APK_SIGNING_BLOCK_MAGIC, block_magic)) {
            //根据block size 读取所有的v2 block数据
            apkFile.seek(offset - block2_size - 8);
            ByteBuffer v2BlockBuffer = ByteBuffer.allocate((int) (block2_size + 8));
            v2BlockBuffer.order(ByteOrder.LITTLE_ENDIAN);
            apkFile.read(v2BlockBuffer.array());
            //保存id-value
            Map pair = new LinkedHashMap<Integer, ByteBuffer>();
            //如果第一个blocksize等于第二个
            if (v2BlockBuffer.getLong() == block2_size) {
                //循环获取 id-value
                while (v2BlockBuffer.position() < v2BlockBuffer.capacity() - 8 - 16) {
                    //读取id value数据的总长度
                    long id_value_size = v2BlockBuffer.getLong();
                    //读取id
                    int id = v2BlockBuffer.getInt();
                    //读取value
                    ByteBuffer value = ByteBuffer.allocate((int) (id_value_size - 4));
                    value.order(ByteOrder.LITTLE_ENDIAN);
                    v2BlockBuffer.get(value.array());
                    pair.put(id, value);
                }
                if (pair.containsKey(Constants.APK_SIGNATURE_SCHEME_V2_BLOCK_ID)) {
                    v2BlockBuffer.flip();
                    return new V2SignBlock(pair, v2BlockBuffer);
                }
            }
        }

        return null;
    }

    static ByteBuffer findEocd(RandomAccessFile apkFile, int offset) throws Exception {
        apkFile.seek(apkFile.length() - offset);
        //读取offset长的数据
        ByteBuffer eocd_buffer = ByteBuffer.allocate(offset);
        eocd_buffer.order(ByteOrder.LITTLE_ENDIAN);
        apkFile.read(eocd_buffer.array());
        //循环查找eocd数据
        for (int current_eocd_offset = 0; current_eocd_offset + Constants.EOCD_COMMENT_OFFSET <=
                offset; current_eocd_offset++) {
            int eocd_tag = eocd_buffer.getInt(current_eocd_offset);
            if (eocd_tag == Constants.EOCD_TAG) {
                int comment_index = current_eocd_offset + Constants
                        .EOCD_COMMENT_LEN_OFFSET;
                short comment_len = eocd_buffer.getShort(comment_index);
                if (comment_len == offset - comment_index - 2) {
                    byte[] array = eocd_buffer.array();
                    ByteBuffer eocd = ByteBuffer.allocate(offset - current_eocd_offset);
                    eocd.order(ByteOrder.LITTLE_ENDIAN);
                    System.arraycopy(array, current_eocd_offset, eocd.array(), 0, eocd.capacity());
                    return eocd;
                }
            }
        }
        return null;
    }
}
