package com.github.odinggg.newyapiupload.dto;

import java.io.File;
import java.util.List;

/**
 * 表格实体
 *
 * @author QinHaoChun
 * @version 2020/6/16
 */
public class Database {
    private String name;

    private String code;

    private List<Table> tables;

    public static class Table {
        private String name;

        private String code;

        private List<Column> columns;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public List<Column> getColumns() {
            return columns;
        }

        public void setColumns(List<Column> columns) {
            this.columns = columns;
        }
    }

    public static class Column {
        private String name;

        private String code;

        private String comment;

        private Boolean mandatory;

        private String type;

        private String defaultValue;

        private String listValues;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Boolean getMandatory() {
            return mandatory;
        }

        public void setMandatory(Boolean mandatory) {
            this.mandatory = mandatory;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getListValues() {
            return listValues;
        }

        public void setListValues(String listValues) {
            this.listValues = listValues;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public static Database parseDatabase(File file) {
        return new Database();
    }
}
