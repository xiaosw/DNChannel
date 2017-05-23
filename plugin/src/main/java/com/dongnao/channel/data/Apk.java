package com.dongnao.channel.data;

import java.io.File;

/**
 * Created by xiang on 2017/5/20.
 */

public class Apk {

    private File file;
    private Eocd eocd;
    private V2SignBlock v2SignBlock;
    private boolean isV2;
    private boolean isV1;

    public Apk(File file) {

        this.file = file;
    }

    public void setEocd(Eocd eocd) {
        this.eocd = eocd;
    }

    public Eocd getEocd() {
        return eocd;
    }

    public void setV2SignBlock(V2SignBlock v2SignBlock) {
        this.v2SignBlock = v2SignBlock;
        if (v2SignBlock != null) {
            isV2 = true;
        }
    }

    public V2SignBlock getV2SignBlock() {
        return v2SignBlock;
    }

    public boolean isV2() {
        return isV2;
    }

    public void setV2(boolean v2) {
        isV2 = v2;
    }

    public boolean isV1() {
        return isV1;
    }

    public void setV1(boolean v1) {
        isV1 = v1;
    }

    public File getFile() {
        return file;
    }
}
