package priv.zxw.dictranslate.util;

import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import priv.zxw.dictranslate.annotation.Dictionary;
import priv.zxw.dictranslate.converter.DictionaryConverter;
import priv.zxw.dictranslate.entity.DictionaryEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

import static org.springframework.util.StringUtils.capitalize;

public class DictionaryWrapperUtils {

    private static final Logger log = LoggerFactory.getLogger(DictionaryWrapperUtils.class);

    /**
     * 根据bean生成字典包装类
     *
     * @param targetClass   bean class
     * @return  字典包装类
     * @throws CannotCompileException   动态生成wrapper class编译失败
     */
    public static Class<DictionaryConverter> generateWrapper(Class<?> targetClass) throws CannotCompileException {
        ClassPool pool = ClassPool.getDefault();
        CtClass evalClass = pool.makeClass(getWrapperClassName(targetClass));
        evalClass.addConstructor(generateDefaultConstructor(evalClass));

        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            Class<?> assignFieldType = field.isAnnotationPresent(Dictionary.class) ? DictionaryEntity.class : field.getType();
            CtField ctField = CtField.make("private " + assignFieldType.getName() + " " + fieldName + ";", evalClass);

            evalClass.addField(ctField);
            evalClass.addMethod(CtNewMethod.getter("get" + capitalize(fieldName), ctField));
            evalClass.addMethod(CtNewMethod.setter("set" + capitalize(fieldName), ctField));
        }

        try {
            evalClass.setInterfaces(new CtClass[] {pool.get(DictionaryConverter.class.getName())});
            evalClass.addMethod(generateConvertMethod(pool, evalClass, targetClass));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }

        return (Class<DictionaryConverter>) evalClass.toClass();
    }

    private static String getWrapperClassName(Class<?> targetClass) {
        return "$$" + targetClass.getName() + "DictionaryWrapper";
    }

    private static CtConstructor generateDefaultConstructor(CtClass evalClass) throws CannotCompileException {
        CtConstructor cons = new CtConstructor(new CtClass[]{}, evalClass);
        cons.setBody("{}");
        return cons;
    }

    private static CtMethod generateConvertMethod(ClassPool classPool, CtClass evalClass, Class<?> targetClass)
            throws CannotCompileException {
        String methodName = "convert";
        CtClass objectClass = null;
        try {
            objectClass = classPool.get(Object.class.getName());
        } catch (NotFoundException neverOccur) {}

        CtMethod method = new CtMethod(objectClass, methodName, new CtClass[]{objectClass}, evalClass);
        method.setModifiers(Modifier.PUBLIC);

        StringBuilder methodBody = new StringBuilder();
        methodBody.append("{ ")
                .append("if ($1 == null) return null; ")
                .append("if (!($1 instanceof ").append(targetClass.getName()).append(")) return $1; ")
                .append(targetClass.getName()).append(" cast = (").append(targetClass.getName()).append(") $1;")
                .append(evalClass.getName()).append(" result = ").append(" new ").append(evalClass.getName()).append("();");

        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (!declaredField.isAnnotationPresent(Dictionary.class)) {
                String fieldName = declaredField.getName();
                methodBody.append(" result.set").append(capitalize(fieldName)).append("(cast.get").append(capitalize(fieldName)).append("()); ");
            }
        }
        methodBody.append("return result; }");

        method.setBody(methodBody.toString());

        return method;
    }

    public static Object newInstanceAndFillField(Class<? extends DictionaryConverter> clazz, Object originValue) throws IllegalAccessException, InstantiationException {
        DictionaryConverter converter = clazz.newInstance();
        return converter.convert(originValue);
    }

    public static Long getDictionaryFieldValue(Object obj, String fieldName) {
        String getterMethodName = "get" + capitalize(fieldName);
        Method method = ReflectionUtils.findMethod(obj.getClass(), getterMethodName);

        if (Objects.isNull(method)) {
            log.warn("{}#{}() 方法不存在", obj.getClass().getName(), getterMethodName);
            return null;
        }

        Object o = ReflectionUtils.invokeMethod(method, obj);
        return toLongValue(o);
    }

    private static Long toLongValue(Object o) {
        if (Objects.isNull(o)) {
            return null;
        }

        if (o instanceof Short) return ((Short) o).longValue();
        if (o instanceof Integer) return ((Integer) o).longValue();
        if (o instanceof Long) return (Long) o;

        if (o.getClass().isPrimitive()) {
            return (long) o;
        }

        return null;
    }

    public static void setDictionaryFieldValue(Object obj, String fieldName, DictionaryEntity entity) {
        String setterMethodName = "set" + capitalize(fieldName);
        Method method = ReflectionUtils.findMethod(obj.getClass(), setterMethodName, DictionaryEntity.class);

        if (Objects.isNull(method)) {
            log.warn("{}#{}({}) 方法不存在", obj.getClass().getName(), setterMethodName, entity.getClass().getName());
            return;
        }

        ReflectionUtils.invokeMethod(method, obj, entity);
    }
}
