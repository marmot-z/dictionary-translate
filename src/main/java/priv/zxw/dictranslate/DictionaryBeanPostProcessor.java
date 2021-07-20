package priv.zxw.dictranslate;

import javassist.CannotCompileException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import priv.zxw.dictranslate.annotation.Dictionary;
import priv.zxw.dictranslate.converter.DictionaryConverter;
import priv.zxw.dictranslate.entity.DictionaryMetaInfo;
import priv.zxw.dictranslate.exception.IllegalDictionaryUsageException;
import priv.zxw.dictranslate.util.DictionaryWrapperUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (!isControllerBean(clazz)) {
            return true;
        }

        List<Class<?>> returnTypes = getMethodReturnTypes(clazz);
        for (Class<?> returnType : returnTypes) {
            // 验证
            valid(returnType);

            // 收集信息
            DictionaryMetaInfo metaInfo = collectDictionaryInfo(returnType);

            // 生成对应的包装对象
            try {
                Class<DictionaryConverter> wrapperClass = DictionaryWrapperUtils.generateWrapper(returnType);
                metaInfo.setWrapperClass(wrapperClass);
            } catch (CannotCompileException e) {
                throw new BeanCreationException("动态创建 " + returnType.getName() + " 的字典包装类失败", e);
            }

            metaInfoMap.put(returnType, metaInfo);
        }

        return true;
    }

    private boolean isControllerBean(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class);
    }

    private List<Class<?>> getMethodReturnTypes(Class<?> clazz) {
        Method[] declaredMethods = clazz.getDeclaredMethods();

        List<Class<?>> returnTypes = new ArrayList<>(declaredMethods.length);
        for (Method declaredMethod : declaredMethods) {
            if (isRequestMappingMethod(declaredMethod) &&
                    hasReturnValue(declaredMethod) && isResponseBody(declaredMethod)) {
                returnTypes.add(declaredMethod.getReturnType());
            }
        }

        return returnTypes;
    }

    private boolean isRequestMappingMethod(Method declaredMethod) {
        return declaredMethod.isAnnotationPresent(RequestMapping.class) ||
                declaredMethod.isAnnotationPresent(GetMapping.class) ||
                declaredMethod.isAnnotationPresent(PostMapping.class) ||
                declaredMethod.isAnnotationPresent(PutMapping.class) ||
                declaredMethod.isAnnotationPresent(DeleteMapping.class);
    }

    private boolean hasReturnValue(Method declaredMethod) {
        return !declaredMethod.getReturnType().equals(Void.TYPE);
    }

    private boolean isResponseBody(Method declaredMethod) {
        if (declaredMethod.isAnnotationPresent(ResponseBody.class)) {
            return true;
        }

        Class<?> declaredClass = declaredMethod.getDeclaringClass();
        return declaredClass.isAnnotationPresent(RestController.class);
    }

    private void valid(Class<?> returnType) {
        Field[] declaredFields = returnType.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Dictionary.class)) {
                Class<?> type = declaredField.getType();

                if (!(isNumberType(type))) {
                    throw new IllegalDictionaryUsageException("@Dictionary 注解只能修饰数值类型字段");
                }
            }
        }
    }

    private boolean isNumberType(Class<?> clazz) {
        return clazz == Short.class || clazz == short.class ||
                clazz == Integer.class || clazz == int.class ||
                clazz == Long.class || clazz == long.class;
    }

    private DictionaryMetaInfo collectDictionaryInfo(Class<?> returnType) {
        DictionaryMetaInfo metaInfo = new DictionaryMetaInfo();
        metaInfo.setOriginClass(returnType);

        Field[] declaredFields = returnType.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Dictionary.class)) {
                Dictionary annotation = declaredField.getAnnotation(Dictionary.class);
                DictionaryMetaInfo.DictionaryField fieldInfo =
                        metaInfo.new DictionaryField(declaredField.getName(), annotation.type(), annotation.translater());

                metaInfo.addField(fieldInfo);
            }
        }

        return metaInfo;
    }
}
