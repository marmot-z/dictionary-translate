package priv.zxw.dictranslate.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import priv.zxw.dictranslate.DictionaryTranslateConfigurationSelector;
import priv.zxw.dictranslate.annotation.Dictionary;
import priv.zxw.dictranslate.entity.DictionaryMetaInfo;
import priv.zxw.dictranslate.exception.IllegalDictionaryUsageException;
import priv.zxw.dictranslate.translater.DictionaryTranslater;
import priv.zxw.dictranslate.translater.NoopDictionaryTranslater;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.reflect.*;
import java.util.*;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2021/7/21
 */
public class ClassParser {

    private static final Logger log = LoggerFactory.getLogger(ClassParser.class);

    public static boolean isControllerAnnotationPresentClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class);
    }

    public static void collectMetaInfoAndValid(Class<?> clazz, Map<String, DictionaryMetaInfo> metaInfoMap) {
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (isRequestMappingAnnotationPresentMethod(declaredMethod)
                    && hasReturnValue(declaredMethod) && isResponseBody(declaredMethod)) {
                Class<?> returnType = declaredMethod.getReturnType();
                Map<String, Class<?>> genericNameClassMap = getMethodReturnTypeGenericNameClassMap(declaredMethod);
                String returnTypeName = getMethodReturnTypeName(declaredMethod);

                collectMetaInfo(returnType, returnTypeName, genericNameClassMap, metaInfoMap);
            }
        }
    }

    private static Map<String, Class<?>> getMethodReturnTypeGenericNameClassMap(Method declaredMethod) {
        Class<?> returnType = declaredMethod.getReturnType();
        Type genericReturnType = declaredMethod.getGenericReturnType();

        if (!returnType.equals(genericReturnType) && (genericReturnType instanceof ParameterizedType)) {
            // ???method??????????????????????????????????????????class????????????????????????????????????????????????????????????????????????????????????
            Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
            TypeVariable<? extends Class<?>>[] typeParameters = returnType.getTypeParameters();

            if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length == typeParameters.length) {
                Map<String, Class<?>> genericNameClassMap = new HashMap<>(actualTypeArguments.length);

                for (int i = 0, length = actualTypeArguments.length; i < length; i++) {
                    String genericName = typeParameters[i].getName();
                    String genericTypeClassFullName = actualTypeArguments[i].getTypeName();
                    try {
                        genericNameClassMap.put(genericName, Class.forName(genericTypeClassFullName));
                    } catch (ClassNotFoundException e) {
                        log.warn("class {} not found", genericTypeClassFullName);
                    }
                }

                return genericNameClassMap;
            }
        }

        return null;
    }

    private static boolean collectMetaInfo(Class<?> clazz, String typeName, Map<String, Class<?>> genericNameClassMap, Map<String, DictionaryMetaInfo> metaInfoMap) {
        if (isJavaClass(clazz)) return false;

        if (metaInfoMap.containsKey(typeName)) return true;

        DictionaryMetaInfo metaInfo = new DictionaryMetaInfo();
        metaInfo.setOriginClass(clazz);

        boolean hasDictionaryField = false;
        for (Field declaredField : clazz.getDeclaredFields()) {
            Class<?> type = getFieldType(declaredField, genericNameClassMap);

            if (declaredField.isAnnotationPresent(Dictionary.class)) {
                Dictionary annotation = declaredField.getAnnotation(Dictionary.class);
                // ????????????????????????????????????
                valid(annotation, type);

                // ?????????????????????translater?????????????????????defaultTranslater
                Class<? extends DictionaryTranslater> translater = isNoopDictionaryTranslater(annotation.translater()) ?
                        DictionaryTranslateConfigurationSelector.defaultTranslaterClass :
                        annotation.translater();
                metaInfo.addDictionaryField(
                        metaInfo.new DictionaryField(declaredField.getName(), annotation.type(), translater));

                hasDictionaryField = true;
            } else {
                if (!isJavaClass(type)) {
                    // FIXME ??????threadLocal??????????????????????????????
                    boolean isDictionaryClass = collectMetaInfo(type, type.getName(), null, metaInfoMap);
                    if (isDictionaryClass) {
                        metaInfo.addWrapperFieldType(metaInfo.new WrapperField(declaredField.getName(), type.getName()));
                        hasDictionaryField = true;
                        continue;
                    }
                }

                metaInfo.addRegularField(metaInfo.new RegularField(declaredField.getName(), type));
            }
        }

        if (hasDictionaryField) {
            metaInfoMap.put(typeName, metaInfo);
        }

        return hasDictionaryField;
    }

    private static void valid(Dictionary annotation, Class<?> fieldType) {
        Class<? extends DictionaryTranslater> translaterClass = annotation.translater();
        if (isNoopDictionaryTranslater(translaterClass) &&
                isNoopDictionaryTranslater(DictionaryTranslateConfigurationSelector.defaultTranslaterClass)) {
            throw new IllegalDictionaryUsageException("?????????@Dictionary??????translater??????");
        }

        if (!isNumberType(fieldType)) {
            throw new IllegalDictionaryUsageException("@Dictionary?????????????????????short,int,long???????????????");
        }
    }

    private static boolean isNoopDictionaryTranslater(Class<? extends DictionaryTranslater> clazz) {
        return NoopDictionaryTranslater.class.equals(clazz);
    }

    private static Class<?> getFieldType(Field declaredField, Map<String, Class<?>> genericNameClassMap) {
        Class<?> type = declaredField.getType();
        Type genericType = declaredField.getGenericType();

        if (type.equals(genericType)) return type;

        // ??????????????????(??? T)?????????????????????class????????????(T -> class)????????????????????????
        String typeName = ((TypeVariableImpl) genericType).getName();
        Class<?> actualType = Objects.isNull(genericNameClassMap) ? null : genericNameClassMap.get(typeName);

        return Objects.isNull(actualType) ? type : actualType;
    }

    private static boolean isJavaClass(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.getName().startsWith("java");
    }

    private static boolean isRequestMappingAnnotationPresentMethod(Method declaredMethod) {
        return declaredMethod.isAnnotationPresent(RequestMapping.class) ||
                declaredMethod.isAnnotationPresent(GetMapping.class) ||
                declaredMethod.isAnnotationPresent(PostMapping.class) ||
                declaredMethod.isAnnotationPresent(PutMapping.class) ||
                declaredMethod.isAnnotationPresent(DeleteMapping.class);
    }

    private static boolean hasReturnValue(Method declaredMethod) {
        return !declaredMethod.getReturnType().equals(Void.TYPE);
    }

    private static boolean isResponseBody(Method declaredMethod) {
        if (declaredMethod.isAnnotationPresent(ResponseBody.class)) {
            return true;
        }

        Class<?> declaredClass = declaredMethod.getDeclaringClass();
        return declaredClass.isAnnotationPresent(RestController.class);
    }

    public static String getMethodReturnTypeName(Method declaredMethod) {
        Class<?> returnType = declaredMethod.getReturnType();
        Type genericReturnType = declaredMethod.getGenericReturnType();

        return returnType.equals(genericReturnType) ? returnType.getName() : genericReturnType.getTypeName();
    }

    private static boolean isNumberType(Class<?> clazz) {
        return clazz == Short.class || clazz == short.class ||
                clazz == Integer.class || clazz == int.class ||
                clazz == Long.class || clazz == long.class;
    }
}
