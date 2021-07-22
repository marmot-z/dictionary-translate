package priv.zxw.dictranslate.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import priv.zxw.dictranslate.annotation.Dictionary;
import priv.zxw.dictranslate.entity.DictionaryMetaInfo;
import priv.zxw.dictranslate.exception.IllegalDictionaryUsageException;
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
            // 从method中获取泛型实际参数类型，获取class的泛型对应的字符，得到泛型符号对应的实际参数类型映射关系
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
                        log.error("class {} not found", genericTypeClassFullName, e);
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
                // 验证注解使用是否合乎规范
                valid(annotation, type);
                metaInfo.addDictionaryField(
                        metaInfo.new DictionaryField(declaredField.getName(), annotation.type(), annotation.translater()));

                hasDictionaryField = true;
            } else {
                if (!isJavaClass(type)) {
                    // FIXME 使用threadLocal来解决循环嵌套的情况
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
        if (!isNumberType(fieldType)) {
            throw new IllegalDictionaryUsageException("@Dictionary注解只能作用于short,int,long类型字段上");
        }
    }

    private static Class<?> getFieldType(Field declaredField, Map<String, Class<?>> genericNameClassMap) {
        Class<?> type = declaredField.getType();
        Type genericType = declaredField.getGenericType();

        if (type.equals(genericType)) return type;

        // 根据泛型符号(如 T)查找泛型符号和class对应关系(T -> class)获取泛型实际类型
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
