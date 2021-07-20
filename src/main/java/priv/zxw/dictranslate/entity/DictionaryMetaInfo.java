package priv.zxw.dictranslate.entity;

import priv.zxw.dictranslate.converter.DictionaryConverter;
import priv.zxw.dictranslate.translater.DictionaryTranslater;

import java.lang.reflect.Field;
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
     * 字典注解标注的字段
     */
    private List<DictionaryField> fields = new ArrayList<>(5);


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

    public void addField(DictionaryField field) {
        this.fields.add(field);
    }

    public Iterator<DictionaryField> fieldIterator() {
        return this.fields.iterator();
    }
}
