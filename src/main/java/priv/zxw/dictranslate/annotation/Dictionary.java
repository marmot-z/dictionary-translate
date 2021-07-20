package priv.zxw.dictranslate.annotation;

import priv.zxw.dictranslate.translater.DictionaryTranslater;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Dictionary {
    int type();

    Class<? extends DictionaryTranslater> translater();
}