package priv.zxw.dictranslate.util;

import com.fasterxml.classmate.GenericType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import priv.zxw.dictranslate.annotation.Dictionary;
import priv.zxw.dictranslate.entity.DictionaryMetaInfo;
import priv.zxw.dictranslate.exception.IllegalDictionaryUsageException;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2021/7/21
 */
public class ClassParser {

    public static boolean isControllerBean(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class);
    }

    public static Class<?>[] getResponseBodyMethodReturnTypes(Class<?> clazz) {
        Method[] declaredMethods = clazz.getDeclaredMethods();

        List<Class<?>> returnTypes = new ArrayList<>(declaredMethods.length);
        for (Method declaredMethod : declaredMethods) {
            if (isRequestMappingMethod(declaredMethod) &&
                    hasReturnValue(declaredMethod) && isResponseBody(declaredMethod)) {
                Class<?> returnType = declaredMethod.getReturnType();

                Type genericReturnType = declaredMethod.getGenericReturnType();
                Map<String, Class<?>> genericNameClassMap = null;
                if (genericReturnType instanceof ParameterizedType) {
                    // 从method中获取泛型实际参数类型，获取class的泛型对应的字符，得到泛型符号对应的实际参数类型映射关系
                    Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                    TypeVariable<? extends Class<?>>[] typeParameters = returnType.getTypeParameters();

                    if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length == typeParameters.length) {
                        genericNameClassMap = new HashMap<>(actualTypeArguments.length);
                        for (int i = 0, length = actualTypeArguments.length; i < length; i++) {
                            String genericName = typeParameters[i].getName();
                            String genericTypeClassFullName = actualTypeArguments[i].getTypeName();
                            try {
                                genericNameClassMap.put(genericName, Class.forName(genericTypeClassFullName));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }

                if (isDictionaryAnnotationPresentClass(returnType, genericNameClassMap)) {
                    returnTypes.add(returnType);
                }
            }
        }

        return returnTypes.toArray(new Class<?>[] {});
    }

    private static boolean isDictionaryAnnotationPresentClass(Class<?> clazz, Map<String, Class<?>> genericNameClassMap) {
        // 只对自定义的class进行字段检查
        if (isJavaClass(clazz)) {
            return false;
        }

        for (Field declaredField : clazz.getDeclaredFields()) {
            Class<?> type = declaredField.getType();

            if (declaredField.isAnnotationPresent(Dictionary.class)) {
                return true;
            }

            Type fieldGenericType = declaredField.getGenericType();
            // 泛型字段genericType为 T 之类的类型，type为Object.class
            if (fieldGenericType != type) {
                String genericName = ((TypeVariableImpl) fieldGenericType).getName();
                Class<?> genericClass = genericNameClassMap.get(genericName);

                if (isJavaClass(genericClass)) {
                    return false;
                }

                if (isDictionaryAnnotationPresentClass(genericClass, genericNameClassMap)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isJavaClass(Class<?> clazz) {
        return clazz.getName().startsWith("java");
    }

    private static boolean isRequestMappingMethod(Method declaredMethod) {
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


    public static void valid(Class<?> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Dictionary.class)) {
                Class<?> type = declaredField.getType();

                if (!(isNumberType(type))) {
                    throw new IllegalDictionaryUsageException("@Dictionary 注解只能修饰数值类型字段");
                }
            }
        }
    }

    private static boolean isNumberType(Class<?> clazz) {
        return clazz == Short.class || clazz == short.class ||
                clazz == Integer.class || clazz == int.class ||
                clazz == Long.class || clazz == long.class;
    }


    public static void collectDictionaryInfo(Class<?> clazz, Map<Class<?>, DictionaryMetaInfo> metaInfoMap) {
        DictionaryMetaInfo metaInfo = new DictionaryMetaInfo();
        metaInfo.setOriginClass(clazz);

        metaInfoMap.put(clazz, metaInfo);

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Dictionary.class)) {
                Dictionary annotation = declaredField.getAnnotation(Dictionary.class);
                DictionaryMetaInfo.DictionaryField fieldInfo =
                        metaInfo.new DictionaryField(declaredField.getName(), annotation.type(), annotation.translater());

                metaInfo.addField(fieldInfo);
            }
        }
    }
}
