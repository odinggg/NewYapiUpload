package com.github.odinggg.newyapiupload.interaction;

import com.github.odinggg.newyapiupload.build.BuildJsonForDubbo;
import com.github.odinggg.newyapiupload.build.BuildJsonForYapi;
import com.github.odinggg.newyapiupload.constant.ProjectTypeConstant;
import com.github.odinggg.newyapiupload.constant.YapiConstant;
import com.github.odinggg.newyapiupload.dto.Database;
import com.github.odinggg.newyapiupload.dto.YapiApiDTO;
import com.github.odinggg.newyapiupload.dto.YapiDubboDTO;
import com.github.odinggg.newyapiupload.dto.YapiResponse;
import com.github.odinggg.newyapiupload.dto.YapiSaveParam;
import com.github.odinggg.newyapiupload.upload.UploadYapi;
import com.google.common.base.Strings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Override
    public void actionPerformed(AnActionEvent e) {
        executorService.submit(() -> {
            Editor editor = e.getDataContext().getData(CommonDataKeys.EDITOR);
            Project project = editor.getProject();
            String projectToken = null;
            String projectId = null;
            String yapiUrl = null;
            String projectType = null;
            String returnClass = null;
            String attachUpload = null;
            // 获取配置
            try {
                String projectConfig = new String(editor.getProject().getProjectFile().contentsToByteArray(), "utf-8");
                String[] modules = projectConfig.split("moduleList\">");
                if (modules.length > 1) {
                    String[] moduleList = modules[1].split("</")[0].split(",");
                    PsiFile psiFile = (PsiFile) e.getDataContext().getData(CommonDataKeys.PSI_FILE);
                    String virtualFile = psiFile.getVirtualFile().getPath();
                    for (int i = 0; i < moduleList.length; i++) {
                        if (virtualFile.contains(moduleList[i])) {
                            projectToken = projectConfig.split(moduleList[i] + "\\.projectToken\">")[1].split("</")[0];
                            projectId = projectConfig.split(moduleList[i] + "\\.projectId\">")[1].split("</")[0];
                            yapiUrl = projectConfig.split(moduleList[i] + "\\.yapiUrl\">")[1].split("</")[0];
                            projectType = projectConfig.split(moduleList[i] + "\\.projectType\">")[1].split("</")[0];
                            if (projectConfig.split(moduleList[i] + "\\.returnClass\">").length > 1) {
                                returnClass = projectConfig.split(moduleList[i] + "\\.returnClass\">")[1].split("</")[0];
                            }
                            String[] attachs = projectConfig.split(moduleList[i] + "\\.attachUploadUrl\">");
                            if (attachs.length > 1) {
                                attachUpload = attachs[1].split("</")[0];
                            }
                            break;
                        }
                    }
                } else {
                    projectToken = projectConfig.split("projectToken\">")[1].split("</")[0];
                    projectId = projectConfig.split("projectId\">")[1].split("</")[0];
                    yapiUrl = projectConfig.split("yapiUrl\">")[1].split("</")[0];
                    projectType = projectConfig.split("projectType\">")[1].split("</")[0];
                    if (projectConfig.split("returnClass\">").length > 1) {
                        returnClass = projectConfig.split("returnClass\">")[1].split("</")[0];
                    }

                    String[] attachs = projectConfig.split("attachUploadUrl\">");
                    if (attachs.length > 1) {
                        attachUpload = attachs[1].split("</")[0];
                    }
                }
            } catch (Exception e2) {
                Notification error = notificationGroup.createNotification("get config error:" + e2.getMessage(), NotificationType.ERROR);
                Notifications.Bus.notify(error, project);
                return;
            }
            // 配置校验
            if (Strings.isNullOrEmpty(projectToken) || Strings.isNullOrEmpty(projectId) || Strings.isNullOrEmpty(yapiUrl) || Strings
                    .isNullOrEmpty(projectType)) {
                Notification error = notificationGroup.createNotification("please check config,[projectToken,projectId,yapiUrl,projectType]", NotificationType.ERROR);
                Notifications.Bus.notify(error, project);
                return;
            }
            // 判断项目类型
            if (ProjectTypeConstant.dubbo.equals(projectType)) {
                // 获得dubbo需上传的接口列表 参数对象
                ArrayList<YapiDubboDTO> yapiDubboDTOs = new BuildJsonForDubbo().actionPerformedList(e);
                if (yapiDubboDTOs != null) {
                    for (YapiDubboDTO yapiDubboDTO : yapiDubboDTOs) {
                        YapiSaveParam yapiSaveParam = new YapiSaveParam(projectToken, yapiDubboDTO.getTitle(), yapiDubboDTO
                                .getPath(), yapiDubboDTO.getParams(), yapiDubboDTO.getResponse(), Integer.valueOf(projectId), yapiUrl, yapiDubboDTO
                                .getDesc());
                        yapiSaveParam.setStatus(yapiDubboDTO.getStatus());
                        if (!Strings.isNullOrEmpty(yapiDubboDTO.getMenu())) {
                            yapiSaveParam.setMenu(yapiDubboDTO.getMenu());
                        } else {
                            yapiSaveParam.setMenu(YapiConstant.menu);
                        }
                        try {
                            // 上传
                            YapiResponse yapiResponse = new UploadYapi().uploadSave(yapiSaveParam, null, project.getBasePath());
                            if (yapiResponse.getErrcode() != 0) {
                                Notification error = notificationGroup.createNotification("sorry ,upload api error cause:" + yapiResponse
                                        .getErrmsg(), NotificationType.ERROR);
                                Notifications.Bus.notify(error, project);
                            } else {
                                String url = yapiUrl + "/project/" + projectId + "/interface/api/cat_" + yapiResponse.getCatId();
                                this.setClipboard(url);
                                Notification error = notificationGroup.createNotification("success ,url: " + url, NotificationType.INFORMATION);
                                Notifications.Bus.notify(error, project);
                            }
                        } catch (Exception e1) {
                            Notification error = notificationGroup.createNotification("sorry ,upload api error cause:" + e1, NotificationType.ERROR);
                            Notifications.Bus.notify(error, project);
                        }
                    }
                }
            } else if (ProjectTypeConstant.api.equals(projectType)) {
                //获得api 需上传的接口列表 参数对象
                ArrayList<YapiApiDTO> yapiApiDTOS = new BuildJsonForYapi().actionPerformedList(e, attachUpload, returnClass);
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
        });
    }

    public static void main(String[] args) {
        Collection<File> pdms = FileUtils.listFiles(new File("D:\\work\\3.研发\\微服务"), FileFilterUtils.suffixFileFilter("pdm"), TrueFileFilter.INSTANCE);
        if (!CollectionUtils.isEmpty(pdms)) {
            List<Database> list = pdms.stream().flatMap(file -> {
                try {
                    SAXReader reader = new SAXReader();
                    Document document = reader.read(file);
                    List<Node> nodes = document.selectNodes("/Model/o:RootObject/c:Children/o:Model");
                    if (!CollectionUtils.isEmpty(nodes)) {
                        List<Database> collect = nodes.stream().map(node -> {
                            Database database = new Database();
                            database.setName(getValue(node, "./a:Name"));
                            database.setCode(getValue(node, "./a:Code"));
                            List<Node> tablesNode = node.selectNodes("./c:Tables/o:Table");
                            if (!CollectionUtils.isEmpty(tablesNode)) {
                                database.setTables(tablesNode.stream().map(tableNode -> {
                                    Database.Table table = new Database.Table();
                                    table.setCode(getValue(tableNode, "./a:Code"));
                                    table.setName(getValue(tableNode, "./a:Name"));
                                    List<Node> columnsNode = tableNode.selectNodes("./c:Columns/o:Column");
                                    if (!CollectionUtils.isEmpty(columnsNode)) {
                                        table.setColumns(columnsNode.stream().map(columnNode -> {
                                            Database.Column column = new Database.Column();
                                            column.setName(getValue(columnNode, "./a:Name"));
                                            column.setCode(getValue(columnNode, "./a:Code"));
                                            column.setComment(getValue(columnNode, "./a:Comment"));
                                            column.setType(getValue(columnNode, "./a:DataType"));
                                            column.setDefaultValue(getValue(columnNode, "./a:DefaultValue"));
                                            column.setListValues(getValue(columnNode, "./a:ListOfValues"));
                                            String man = getValue(columnNode, "./a:Column.Mandatory");
                                            column.setMandatory(man.equalsIgnoreCase("1"));
                                            return column;
                                        }).collect(Collectors.toList()));
                                    }
                                    return table;
                                }).collect(Collectors.toList()));
                            }
                            return database;
                        }).collect(Collectors.toList());
                        return collect.stream();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Stream.empty();
            }).collect(Collectors.toList());
            System.out.println(list.size());
        }
        System.out.println(pdms.size());
    }

    public static String getValue(Node node, String xpath) {
        List<Node> nodes = node.selectNodes(xpath);
        if (!CollectionUtils.isEmpty(nodes)) {
            return nodes.get(0).getText();
        }
        return "";
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
}
