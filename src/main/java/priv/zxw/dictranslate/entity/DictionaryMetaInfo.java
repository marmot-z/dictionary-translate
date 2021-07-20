package priv.zxw.dictranslate.entity;

import priv.zxw.dictranslate.converter.DictionaryConverter;
import priv.zxw.dictranslate.translater.DictionaryTranslater;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DictionaryMetaInfo {
    private Class<?> originClass;
    private Class<DictionaryConverter> wrapperClass;
    private List<Field> dictionaryAnnotationFields = new ArrayList<>(5);
    private Class<? extends DictionaryTranslater> translaterClass;

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

    public void addDictionaryAnnotationField(Field field) {
        dictionaryAnnotationFields.add(field);
    }

    public Class<? extends DictionaryTranslater> getTranslaterClass() {
        return translaterClass;
    }

    public void setTranslaterClass(Class<? extends DictionaryTranslater> translaterClass) {
        this.translaterClass = translaterClass;
    }
}
