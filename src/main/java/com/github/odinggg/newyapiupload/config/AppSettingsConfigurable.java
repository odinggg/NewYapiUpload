// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.odinggg.newyapiupload.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides controller functionality for application settings.
 */
public class AppSettingsConfigurable implements Configurable {
    private AppSettingsComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "YApi: Upload Plugin Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settings = AppSettingsState.getInstance();
        boolean modified = !mySettingsComponent.getProjectToken().equals(settings.projectToken);
        modified |= !mySettingsComponent.getProjectId().equals(settings.projectId);
        modified |= !mySettingsComponent.getYapiUrl().equals(settings.yapiUrl);
        modified |= !mySettingsComponent.getProjectType().equals(settings.projectType);
        modified |= !mySettingsComponent.getReturnClass().equals(settings.returnClass);
        modified |= !mySettingsComponent.getPdmFilePath().equals(settings.pdmFilePath);
        modified |= mySettingsComponent.getUsePDMCheckBox() != settings.usePDMCheck;
        modified |= mySettingsComponent.getSyncCheckBox() != settings.syncCheck;
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.projectToken = mySettingsComponent.getProjectToken();
        settings.projectId = mySettingsComponent.getProjectId();
        settings.yapiUrl = mySettingsComponent.getYapiUrl();
        settings.projectType = mySettingsComponent.getProjectType();
        settings.returnClass = mySettingsComponent.getReturnClass();
        settings.pdmFilePath = mySettingsComponent.getPdmFilePath();
        settings.usePDMCheck = mySettingsComponent.getUsePDMCheckBox();
        settings.syncCheck = mySettingsComponent.getSyncCheckBox();
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setProjectToken(settings.projectToken);
        mySettingsComponent.setProjectId(settings.projectId);
        mySettingsComponent.setYapiUrl(settings.yapiUrl);
        mySettingsComponent.setProjectType(settings.projectType);
        mySettingsComponent.setReturnClass(settings.returnClass);
        mySettingsComponent.setPdmFilePath(settings.pdmFilePath);
        mySettingsComponent.setUsePDMCheckBox(settings.usePDMCheck);
        mySettingsComponent.setSyncCheckBox(settings.syncCheck);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
