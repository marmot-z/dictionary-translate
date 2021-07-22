package priv.zxw.dictranslate.annotation;

import org.springframework.context.annotation.Import;
import priv.zxw.dictranslate.DictionaryTranslateConfigurationSelector;
import priv.zxw.dictranslate.translater.DictionaryTranslater;
import priv.zxw.dictranslate.translater.NoopDictionaryTranslater;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(DictionaryTranslateConfigurationSelector.class)
public @interface EnableDictionaryTranslate {

    Class<? extends DictionaryTranslater> defaultTranslater() default NoopDictionaryTranslater.class;

    Mode dynamicClassGeneratorMode() default Mode.JAVASSIST;

    enum Mode {
        JAVASSIST,
        GROOVY
    }
}
