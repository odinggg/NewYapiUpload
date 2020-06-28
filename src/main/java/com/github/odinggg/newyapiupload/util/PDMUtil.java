package com.github.odinggg.newyapiupload.util;

import com.github.odinggg.newyapiupload.dto.Database;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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
            return pdms.stream().map(file -> {
                try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    PDMUtil.PDMXMLHandler pdmxmlHandler = new PDMUtil.PDMXMLHandler();
                    parser.parse(file, pdmxmlHandler);
                    return pdmxmlHandler.database;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return null;
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
        if (StringUtils.isBlank(className)) {
            return "";
        }
        return className.replaceAll("([A-Z])", "\\_$1").toLowerCase().substring(1);
    }

    public static class PDMXMLHandler extends DefaultHandler {
        private Database database;
        private String preTag;
        private List<Database.Column> columns;
        private List<Database.Table> tables;
        private Database.Column currentColumn;
        private Database.Table currentTable;
        private int level = -1;

        public Database getDatabase() {
            return database;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
        }

        @Override
        public void startDocument() throws SAXException {
            database = new Database();
            columns = new ArrayList<>();
            tables = new ArrayList<>();
            currentColumn = null;
            currentTable = null;
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            level += 1;
            if (level == 7 && "o:Column".equals(qName)) {
                currentColumn = new Database.Column();
            }
            if (level == 5 && "o:Table".equals(qName)) {
                currentTable = new Database.Table();
            }
            if (level == 3 && "o:Model".equals(qName)) {
                database = new Database();
            }
            preTag = qName;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (level == 7 && "o:Column".equals(qName)) {
                columns.add(currentColumn);
            }
            if (level == 5 && "o:Table".equals(qName)) {
                currentTable.setColumns(columns);
                tables.add(currentTable);
                columns = new ArrayList<>();
            }
            if (level == 3 && "o:Model".equals(qName)) {
                database.setTables(tables);
                tables = new ArrayList<>();
            }
            preTag = null;
            level -= 1;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (preTag != null) {
                String value = new String(ch, start, length);
            }
            if (level == 8 && "a:Name".equals(preTag) && currentColumn != null) {
                currentColumn.setName(new String(ch, start, length));
            }
            if (level == 8 && "a:Code".equals(preTag) && currentColumn != null) {
                currentColumn.setCode(new String(ch, start, length));
            }
            if (level == 8 && "a:Comment".equals(preTag) && currentColumn != null) {
                currentColumn.setComment(new String(ch, start, length));
            }
            if (level == 8 && "a:Column.Mandatory".equals(preTag) && currentColumn != null) {
                currentColumn.setMandatory("1".equals(new String(ch, start, length)));
            }
            if (level == 8 && "a:DataType".equals(preTag) && currentColumn != null) {
                currentColumn.setType(new String(ch, start, length));
            }
            if (level == 8 && "a:DefaultValue".equals(preTag) && currentColumn != null) {
                currentColumn.setDefaultValue(new String(ch, start, length));
            }
            if (level == 8 && "a:ListOfValues".equals(preTag) && currentColumn != null) {
                currentColumn.setListValues(new String(ch, start, length));
            }
            if (level == 6 && "a:Name".equals(preTag) && currentTable != null) {
                currentTable.setName(new String(ch, start, length));
            }
            if (level == 6 && "a:Code".equals(preTag) && currentTable != null) {
                currentTable.setCode(new String(ch, start, length));
            }
            if (level == 4 && "a:Name".equals(preTag) && database != null) {
                database.setName(new String(ch, start, length));
            }
            if (level == 4 && "a:Code".equals(preTag) && database != null) {
                database.setCode(new String(ch, start, length));
            }
        }
    }

}
