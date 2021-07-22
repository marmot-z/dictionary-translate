package priv.zxw.dictranslate;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import priv.zxw.dictranslate.annotation.EnableDictionaryTranslate;
import priv.zxw.dictranslate.translater.DictionaryTranslater;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2021/7/22
 */
public class DictionaryTranslateConfigurationSelector implements ImportSelector {

    public static Class<? extends DictionaryTranslater> defaultTranslaterClass;

    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        AnnotationAttributes attributes = getAttributes(annotationMetadata);
        String defaultTranslaterClassName = attributes.getString("defaultTranslater");
        Class<? extends DictionaryTranslater> defaultTranslaterClass;
        try {
            defaultTranslaterClass = (Class<? extends DictionaryTranslater>) Class.forName(defaultTranslaterClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("@Dictionary defaultTranslater class " + defaultTranslaterClassName + " not found", e);
        }

        DictionaryTranslateConfigurationSelector.defaultTranslaterClass = defaultTranslaterClass;

        // TODO 支持groovy生成动态class
        return new String[] {"priv.zxw.dictranslate.config.DictionaryTranslateConfiguration"};
    }

    protected AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
        String name = EnableDictionaryTranslate.class.getName();
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(name, true));
        Assert.notNull(attributes,
             "No auto-configuration attributes found. Is " + metadata.getClassName() + " annotated with " + ClassUtils.getShortName(name) + "?");
        return attributes;
    }
}
