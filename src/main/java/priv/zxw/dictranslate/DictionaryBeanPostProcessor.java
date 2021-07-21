package priv.zxw.dictranslate;

import javassist.CannotCompileException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import priv.zxw.dictranslate.annotation.Dictionary;
import priv.zxw.dictranslate.converter.DictionaryConverter;
import priv.zxw.dictranslate.entity.DictionaryMetaInfo;
import priv.zxw.dictranslate.exception.IllegalDictionaryUsageException;
import priv.zxw.dictranslate.util.ClassParser;
import priv.zxw.dictranslate.util.DictionaryWrapperUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class DictionaryBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

    /**
     * 字典元信息集合
     * Class                字典字段所在原始类
     * DictionaryMetaInfo   字典元信息
     */
    public static Map<Class<?>, DictionaryMetaInfo> metaInfoMap = new HashMap<>(10);

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();

        if (!ClassParser.isControllerBean(clazz)) {
            return true;
        }

        Class<?>[] returnTypes = ClassParser.getResponseBodyMethodReturnTypes(clazz);
        for (Class<?> returnType : returnTypes) {
            // 验证
            ClassParser.valid(returnType);

            // 收集元信息
            ClassParser.collectDictionaryInfo(returnType, metaInfoMap);
            DictionaryMetaInfo metaInfo = metaInfoMap.get(returnType);

            // 生成对应的包装对象
            try {
                Class<DictionaryConverter> wrapperClass = DictionaryWrapperUtils.generateWrapper(returnType);
                metaInfo.setWrapperClass(wrapperClass);
            } catch (CannotCompileException e) {
                throw new BeanCreationException("动态创建 " + returnType.getName() + " 的字典包装类失败", e);
            }
        }

        return true;
    }
}
