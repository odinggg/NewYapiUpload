package com.github.odinggg.newyapiupload.util;

import com.github.odinggg.newyapiupload.dto.Database;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 解析pdm文件
 *
 * @author QinHaoChun
 * @version 2020/6/17
 */
public class PDMUtil {
    public static final CopyOnWriteArrayList<Database> DATABASES = new CopyOnWriteArrayList<>();

    public static List<Database> parseDatabase(String filePath) {
        Collection<File> pdms = FileUtils.listFiles(new File(filePath), FileFilterUtils.suffixFileFilter("pdm"), TrueFileFilter.INSTANCE);
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
            return list;
        }
        return null;
    }

    public static String getValue(Node node, String xpath) {
        List<Node> nodes = node.selectNodes(xpath);
        if (!CollectionUtils.isEmpty(nodes)) {
            return nodes.get(0).getText();
        }
        return "";
    }

    public static String getDesc(String databaseName, String tableName, String columnName) {
        if (!CollectionUtils.isEmpty(DATABASES)) {
            Database.Column result = DATABASES.stream()
                    .filter(database -> database.getCode().equals(databaseName))
                    .map(Database::getTables)
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(Collection::stream)
                    .filter(table -> table.getCode().equals(tableName))
                    .map(Database.Table::getColumns)
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(Collection::stream)
                    .filter(column -> column.getCode().equals(columnName)).findAny().orElse(null);
            String sb = getString(result);
            if (sb != null) return sb;
        }
        return "";
    }

    @Nullable
    private static String getString(Database.Column result) {
        if (Objects.nonNull(result)) {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotBlank(result.getName())) {
                sb.append(result.getName()).append("\t");
            }
            if (StringUtils.isNotBlank(result.getComment())) {
                sb.append(result.getComment()).append("\t");
            }
            if (StringUtils.isNotBlank(result.getListValues())) {
                sb.append(result.getListValues()).append("\t");
            }
            return sb.toString();
        }
        return null;
    }

    public static String getDesc(String tableName, String columnName) {
        if (!CollectionUtils.isEmpty(DATABASES) && StringUtils.isNotBlank(tableName)) {
            Database.Column result = DATABASES.stream()
                    .map(Database::getTables)
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(Collection::stream)
                    .filter(table -> table.getCode().equals(tableName))
                    .map(Database.Table::getColumns)
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(Collection::stream)
                    .filter(column -> column.getCode().equals(columnName)).findAny().orElse(null);
            String sb = getString(result);
            if (sb != null) return sb;
        }
        return "";
    }

    public static String className2TableName(String className) {
        return className.replaceAll("([A-Z])", "\\_$1").toLowerCase().substring(1);
    }
}
