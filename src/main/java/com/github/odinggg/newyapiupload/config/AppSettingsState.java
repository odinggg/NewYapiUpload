// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.odinggg.newyapiupload.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Supports storing the application settings in a persistent way.
 * The State and Storage annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(
        name = "com.github.odinggg.newyapiupload.config.AppSettingsState",
        storages = {@Storage("NewYApiUploadPlugin.xml")}
)
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {
    public String projectToken = "";
    public String projectId = "";
    public String yapiUrl = "";
    public String projectType = "api";
    public String returnClass = "";
    public String pdmFilePath = "";
    public boolean usePDMCheck = true;
    public boolean syncCheck = false;

    public static AppSettingsState getInstance(Project project) {
        return ServiceManager.getService(project, AppSettingsState.class);
    }

    @Nullable
    @Override
    public AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
