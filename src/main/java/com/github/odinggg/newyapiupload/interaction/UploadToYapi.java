package com.github.odinggg.newyapiupload.interaction;

import com.github.odinggg.newyapiupload.build.BuildJsonForYapi;
import com.github.odinggg.newyapiupload.config.AppSettingsState;
import com.github.odinggg.newyapiupload.constant.ProjectTypeConstant;
import com.github.odinggg.newyapiupload.constant.YapiConstant;
import com.github.odinggg.newyapiupload.dto.Database;
import com.github.odinggg.newyapiupload.dto.YapiApiDTO;
import com.github.odinggg.newyapiupload.dto.YapiResponse;
import com.github.odinggg.newyapiupload.dto.YapiSaveParam;
import com.github.odinggg.newyapiupload.upload.UploadYapi;
import com.github.odinggg.newyapiupload.util.PDMUtil;
import com.google.common.base.Strings;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description: 入口
 * @author: Hansen
 * @date: 2019/5/15
 */
public class UploadToYapi extends AnAction {

    private static NotificationGroup notificationGroup;


    static {
        notificationGroup = new NotificationGroup("Java2Json.NotificationGroup", NotificationDisplayType.BALLOON, true);
    }

    public void mainMethod(AnActionEvent e) {
        AppSettingsState instance = AppSettingsState.getInstance(e.getProject());
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        if (instance.usePDMCheck && StringUtils.isNotBlank(instance.pdmFilePath)) {
            List<Database> databases = PDMUtil.parseDatabase(instance.pdmFilePath);
            PDMUtil.DATABASES.addAll(databases);
            Notification error = notificationGroup.createNotification("read pdm success:  " + PDMUtil.DATABASES.size(), NotificationType.INFORMATION);
            Notifications.Bus.notify(error, project);
        }
        Editor editor = e.getDataContext().getData(CommonDataKeys.EDITOR);
        List<@NotNull PsiClass> collect = null;
        if (editor == null) {
            PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
            if (psiElement != null) {
                @NotNull PsiElement[] children = psiElement.getChildren();
                if (children != null) {
                    collect = getPsiClasses(children);
                } else {
                    Notification error = notificationGroup.createNotification("请选择正确的包路径", NotificationType.ERROR);
                    Notifications.Bus.notify(error, project);
                    return;
                }
            } else {
                AbstractProjectViewPane viewPane = ProjectView.getInstance(project)
                        .getCurrentProjectViewPane();
                if (viewPane != null) {
                    @NotNull PsiElement[] psiElements = viewPane.getSelectedPSIElements();
                    if (psiElements != null) {
                        collect = getPsiClasses(psiElements);
                    } else {
                        Notification error = notificationGroup.createNotification("请选择正确的类文件", NotificationType.ERROR);
                        Notifications.Bus.notify(error, project);
                        return;
                    }
                } else {
                    Notification error = notificationGroup.createNotification("请选择正确的类文件", NotificationType.ERROR);
                    Notifications.Bus.notify(error, project);
                    return;
                }
            }
        }
        String projectToken = instance.projectToken;
        String projectId = instance.projectId;
        String yapiUrl = instance.yapiUrl;
        String projectType = instance.projectType;
        String returnClass = instance.returnClass;
        String attachUpload = "http://localhost/fileupload";
        // 配置校验
        if (Strings.isNullOrEmpty(projectToken) || Strings.isNullOrEmpty(projectId) || Strings.isNullOrEmpty(yapiUrl) || Strings
                .isNullOrEmpty(projectType)) {
            Notification error = notificationGroup.createNotification("please check config,[projectToken,projectId,yapiUrl,projectType]", NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
            return;
        }
        // 判断项目类型
        if (ProjectTypeConstant.dubbo.equals(projectType)) {
        } else if (ProjectTypeConstant.api.equals(projectType)) {
            if (collect != null) {
                collect.forEach(psiClass -> sendYapi(e, project, projectToken, projectId, yapiUrl, returnClass, attachUpload, psiClass));
            } else {
                sendYapi(e, project, projectToken, projectId, yapiUrl, returnClass, attachUpload, null);
            }
        }
    }

    @NotNull
    private List<@NotNull PsiClass> getPsiClasses(@NotNull PsiElement[] children) {
        List<@NotNull PsiClass> collect;
        collect = Stream.of(children)
                .map(PsiElement::getContainingFile)
                .flatMap(psiFile -> {
                    if (psiFile instanceof PsiJavaFileImpl) {
                        return Stream.of(((PsiJavaFileImpl) psiFile).getClasses());
                    } else {
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());
        return collect;
    }

    private void sendYapi(AnActionEvent e, Project project, String projectToken, String projectId, String yapiUrl, String returnClass, String attachUpload, PsiClass psiClass) {
        //获得api 需上传的接口列表 参数对象
        ArrayList<YapiApiDTO> yapiApiDTOS = new BuildJsonForYapi().actionPerformedList(e, attachUpload, returnClass, psiClass);
        if (yapiApiDTOS != null) {
            for (YapiApiDTO yapiApiDTO : yapiApiDTOS) {
                YapiSaveParam yapiSaveParam = new YapiSaveParam(projectToken, yapiApiDTO.getTitle(), yapiApiDTO.getPath(), yapiApiDTO
                        .getParams(), yapiApiDTO.getRequestBody(), yapiApiDTO.getResponse(), Integer.valueOf(projectId), yapiUrl, true, yapiApiDTO
                        .getMethod(), yapiApiDTO.getDesc(), yapiApiDTO.getHeader());
                yapiSaveParam.setReq_body_form(yapiApiDTO.getReq_body_form());
                yapiSaveParam.setReq_body_type(yapiApiDTO.getReq_body_type());
                yapiSaveParam.setReq_params(yapiApiDTO.getReq_params());
                yapiSaveParam.setStatus(yapiApiDTO.getStatus());
                if (!Strings.isNullOrEmpty(yapiApiDTO.getMenu())) {
                    yapiSaveParam.setMenu(yapiApiDTO.getMenu());
                } else {
                    yapiSaveParam.setMenu(YapiConstant.menu);
                }
                try {
                    // 上传
                    YapiResponse yapiResponse = new UploadYapi().uploadSave(yapiSaveParam, attachUpload, project
                            .getBasePath());
                    if (yapiResponse.getErrcode() != 0) {
                        Notification error = notificationGroup.createNotification("sorry ,upload api error cause:" + yapiResponse
                                .getErrmsg(), NotificationType.ERROR);
                        Notifications.Bus.notify(error, project);
                    } else {
                        String url = yapiUrl + "/project/" + projectId + "/interface/api/cat_" + yapiResponse.getCatId();
                        this.setClipboard(url);
                        Notification error = notificationGroup.createNotification("success ,url:  " + url, NotificationType.INFORMATION);
                        Notifications.Bus.notify(error, project);
                    }
                } catch (Exception e1) {
                    Notification error = notificationGroup.createNotification("sorry ,upload api error cause:" + e1, NotificationType.ERROR);
                    Notifications.Bus.notify(error, project);
                }
            }
        }
    }

    /**
     * @description: 设置到剪切板
     * @param: [content]
     * @return: void
     * @author: Hansen
     * @date: 2019/7/3
     */
    private void setClipboard(String content) {
        //获取系统剪切板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //构建String数据类型
        StringSelection selection = new StringSelection(content);
        //添加文本到系统剪切板
        clipboard.setContents(selection, null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        AppSettingsState instance = AppSettingsState.getInstance(e.getProject());
        if (instance.syncCheck) {
            mainMethod(e);
        } else {
            ApplicationManager.getApplication().runReadAction(() -> mainMethod(e));
        }
    }
}
