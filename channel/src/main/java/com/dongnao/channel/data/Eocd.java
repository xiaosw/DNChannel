package com.dongnao.channel.data;

import java.nio.ByteBuffer;

/**
 * Created by xiang on 2017/5/20.
 */

public class Eocd {

    private ByteBuffer data;

    public Eocd(ByteBuffer data) {
        this.data = data;
    }


    public int getCdOffset() {
        return data.getInt(Constants.EOCD_CD_OFFSET);
    }

    public int getCdSize() {
        return data.getInt(Constants.EOCD_CD_SIZE_OFFSET);
    }

    public ByteBuffer getData() {
        return data;
    }
}
