package priv.zxw.dictranslate.entity;

import priv.zxw.dictranslate.converter.DictionaryConverter;
import priv.zxw.dictranslate.translater.DictionaryTranslater;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DictionaryMetaInfo {

    /**
     * 初始响应class
     */
    private Class<?> originClass;

    /**
     * 字典包装类class
     */
    private Class<DictionaryConverter> wrapperClass;

    /**
     * 普通字段
     */
    private List<RegularField> regularFields = new ArrayList<>(5);

    /**
     * 字典注解字段
     */
    private List<DictionaryField> dictionaryFields = new ArrayList<>(5);

    /**
     * 嵌套类字段
     */
    private List<WrapperField> wrapperFieldTypes = new ArrayList<>(5);


    public class DictionaryField {
        /**
         * 字段名称
         */
        private String fieldName;

        /**
         * 字典翻译类
         */
        private Class<? extends DictionaryTranslater> translaterClass;

        /**
         * 字典类型
         */
        private Integer type;

        public DictionaryField(String fieldName, Integer type, Class<? extends DictionaryTranslater> translaterClass) {
            this.fieldName = fieldName;
            this.translaterClass = translaterClass;
            this.type = type;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Class<? extends DictionaryTranslater> getTranslaterClass() {
            return translaterClass;
        }

        public Integer getType() {
            return type;
        }
    }

    public class RegularField {
        /**
         * 字段名称
         */
        private String fieldName;

        /**
         * 字段类型
         */
        private Class<?> fieldType;

        public RegularField(String fieldName, Class<?> fieldType) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Class<?> getFieldType() {
            return fieldType;
        }
    }

    public class WrapperField {
        /**
         * 字段名称
         */
        private String fieldName;
        /**
         * 字段类型名称
         */
        private String fieldTypeName;

        public WrapperField(String fieldName, String fieldTypeName) {
            this.fieldName = fieldName;
            this.fieldTypeName = fieldTypeName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getFieldTypeName() {
            return fieldTypeName;
        }
    }

    public Class<?> getOriginClass() {
        return originClass;
    }

    public void setOriginClass(Class<?> originClass) {
        this.originClass = originClass;
    }

    public Class<DictionaryConverter> getWrapperClass() {
        return wrapperClass;
    }

    public void setWrapperClass(Class<DictionaryConverter> wrapperClass) {
        this.wrapperClass = wrapperClass;
    }

    public void addRegularField(RegularField field) {
        this.regularFields.add(field);
    }

    public void addDictionaryField(DictionaryField field) {
        this.dictionaryFields.add(field);
    }

    public void addWrapperFieldType(WrapperField wrapperField) {
        this.wrapperFieldTypes.add(wrapperField);
    }

    public Iterator<RegularField> regularFieldIterator() {
        return this.regularFields.iterator();
    }

    public Iterator<DictionaryField> dictionaryFieldIterator() {
        return this.dictionaryFields.iterator();
    }

    public Iterator<WrapperField> wrapperFieldTypeIterator() {
        return this.wrapperFieldTypes.iterator();
    }
}
