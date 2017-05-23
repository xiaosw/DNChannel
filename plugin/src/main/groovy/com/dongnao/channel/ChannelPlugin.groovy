package com.dongnao.channel

import org.gradle.api.Plugin
import org.gradle.api.Project

class ChannelPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def channel = project.extensions.create('channel', ChannelExtensions)

        project.afterEvaluate {
            project.tasks.create('assembleChannel',ChannelTask)
        }
    }
}