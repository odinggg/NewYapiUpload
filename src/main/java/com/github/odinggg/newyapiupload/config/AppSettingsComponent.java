// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.odinggg.newyapiupload.config;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Supports creating and managing a JPanel for the Settings Dialog.
 */
public class AppSettingsComponent {
    private final JPanel myMainPanel;
    private final JBTextField projectToken = new JBTextField();
    private final JBTextField projectId = new JBTextField();
    private final JBTextField yapiUrl = new JBTextField();
    private final JBTextField projectType = new JBTextField();
    private final JBTextField returnClass = new JBTextField();
    private final JBTextField pdmFilePath = new JBTextField();
    private final JBCheckBox usePDMCheckBox = new JBCheckBox("使用pdm");
    private final JBCheckBox syncCheckBox = new JBCheckBox("同步上传");

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("projectToken: "), projectToken, 1, false)
                .addLabeledComponent(new JBLabel("projectId: "), projectId, 1, false)
                .addLabeledComponent(new JBLabel("yapiUrl: "), yapiUrl, 1, false)
                .addLabeledComponent(new JBLabel("projectType: "), projectType, 1, false)
                .addLabeledComponent(new JBLabel("returnClass: "), returnClass, 1, false)
                .addLabeledComponent(new JBLabel("pdmFilePath: "), pdmFilePath, 1, false)
                .addComponent(usePDMCheckBox, 1)
                .addComponent(syncCheckBox, 0)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return projectToken;
    }

    @NotNull
    public String getProjectToken() {
        return projectToken.getText();
    }

    @NotNull
    public String getProjectId() {
        return projectId.getText();
    }

    @NotNull
    public String getYapiUrl() {
        return yapiUrl.getText();
    }

    @NotNull
    public String getProjectType() {
        return projectType.getText();
    }

    @NotNull
    public String getReturnClass() {
        return returnClass.getText();
    }

    @NotNull
    public String getPdmFilePath() {
        return pdmFilePath.getText();
    }

    public void setProjectToken(@NotNull String newText) {
        projectToken.setText(newText);
    }

    public void setProjectId(@NotNull String newText) {
        projectId.setText(newText);
    }

    public void setYapiUrl(@NotNull String newText) {
        yapiUrl.setText(newText);
    }

    public void setProjectType(@NotNull String newText) {
        projectType.setText(newText);
    }

    public void setReturnClass(@NotNull String newText) {
        returnClass.setText(newText);
    }

    public void setPdmFilePath(@NotNull String newText) {
        pdmFilePath.setText(newText);
    }

    public boolean getUsePDMCheckBox() {
        return usePDMCheckBox.isSelected();
    }

    public void setUsePDMCheckBox(boolean newStatus) {
        usePDMCheckBox.setSelected(newStatus);
    }

    public boolean getSyncCheckBox() {
        return syncCheckBox.isSelected();
    }

    public void setSyncCheckBox(boolean newStatus) {
        syncCheckBox.setSelected(newStatus);
    }
}
