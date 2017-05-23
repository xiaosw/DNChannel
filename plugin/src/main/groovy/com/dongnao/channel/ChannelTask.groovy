package com.dongnao.channel

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.dongnao.channel.DNApkParser.parser

class ChannelTask extends DefaultTask {

    ChannelTask() {
        group '渠道包'
        description '生成渠道包'
    }

    @TaskAction
    def run() {
        def baseApk = new File(project.channel.baseApk)
        def channelFile = new File(project.channel.channelFile)
        def outDir = new File(project.channel.outDir)
        outDir.mkdirs()

        def name = baseApk.name
        name = name.substring(0, name.lastIndexOf("."))

        channelFile.readLines().each {
            def apk = parser(baseApk)
            DNApkBuilder.generateChannel(it, apk, new File(outDir, "${name}-${it}.apk"))
        }
    }
}