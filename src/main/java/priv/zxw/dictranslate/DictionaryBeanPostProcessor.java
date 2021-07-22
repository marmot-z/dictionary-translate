package priv.zxw.dictranslate;

import javassist.CannotCompileException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;
import priv.zxw.dictranslate.converter.DictionaryConverter;
import priv.zxw.dictranslate.entity.DictionaryMetaInfo;
import priv.zxw.dictranslate.exception.DictionaryWrapperClassGenerateException;
import priv.zxw.dictranslate.util.ClassParser;
import priv.zxw.dictranslate.util.DictionaryWrapperUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class DictionaryBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

    /**
     * 字典元信息集合
     * Class                字典字段类型名称
     * DictionaryMetaInfo   字典元信息
     */
    public static Map<String, DictionaryMetaInfo> metaInfoMap = new HashMap<>(10);

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();

        if (!ClassParser.isControllerAnnotationPresentClass(clazz)) {
            return true;
        }

        ClassParser.collectMetaInfoAndValid(clazz, metaInfoMap);

        for (Map.Entry<String, DictionaryMetaInfo> entry : metaInfoMap.entrySet()) {
            DictionaryMetaInfo metaInfo = entry.getValue();
            if (Objects.isNull(metaInfo.getWrapperClass())) {
                try {
                    Class<DictionaryConverter> wrapperClass = DictionaryWrapperUtils.generateWrapper(metaInfo, metaInfoMap);
                    metaInfo.setWrapperClass(wrapperClass);
                } catch (CannotCompileException e) {
                    throw new DictionaryWrapperClassGenerateException("动态创建 " + entry.getValue() + " 的字典包装类失败", e);
                }
            }
        }

        return true;
    }
}
