package com.dongnao.dnchannel;

import com.dongnao.channel.DNApkBuilder;
import com.dongnao.channel.DNApkParser;
import com.dongnao.channel.data.Apk;

import org.junit.Test;

import java.io.File;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        Apk apk = DNApkParser.parser
                ("/Users/xiang/listener/DNChannel/v1.apk");

        for (int i = 0; i < 10; ++i) {
            DNApkBuilder.generateChannel("渠道", apk, new File("/Users/xiang/listener/DNChannel/v1"
                    + i + ".apk"));
        }
    }
}