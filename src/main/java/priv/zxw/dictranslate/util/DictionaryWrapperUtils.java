package priv.zxw.dictranslate.util;

import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import priv.zxw.dictranslate.converter.DictionaryConverter;
import priv.zxw.dictranslate.entity.DictionaryEntity;
import priv.zxw.dictranslate.entity.DictionaryMetaInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static org.springframework.util.StringUtils.capitalize;

public class DictionaryWrapperUtils {

    private static final Logger log = LoggerFactory.getLogger(DictionaryWrapperUtils.class);

    public static Class<DictionaryConverter> generateWrapper(DictionaryMetaInfo metaInfo, Map<String, DictionaryMetaInfo> metaInfoMap)
            throws CannotCompileException {
        if (Objects.nonNull(metaInfo.getWrapperClass())) {
            return metaInfo.getWrapperClass();
        }

        Class<?> targetClass = metaInfo.getOriginClass();
        ClassPool pool = ClassPool.getDefault();
        CtClass evalClass = pool.makeClass(getWrapperClassName(targetClass));
        evalClass.addConstructor(generateDefaultConstructor(evalClass));

        Iterator<DictionaryMetaInfo.RegularField> regularFieldIterator = metaInfo.regularFieldIterator();
        while (regularFieldIterator.hasNext()) {
            DictionaryMetaInfo.RegularField field = regularFieldIterator.next();
            addField(field.getFieldName(), field.getFieldType(), evalClass);
        }

        Iterator<DictionaryMetaInfo.DictionaryField> dictionaryFieldIterator = metaInfo.dictionaryFieldIterator();
        while (dictionaryFieldIterator.hasNext()) {
            DictionaryMetaInfo.DictionaryField field = dictionaryFieldIterator.next();
            addField(field.getFieldName(), DictionaryEntity.class, evalClass);
        }

        Iterator<DictionaryMetaInfo.WrapperField> wrapperFieldTypeIterator = metaInfo.wrapperFieldTypeIterator();
        while (wrapperFieldTypeIterator.hasNext()) {
            DictionaryMetaInfo.WrapperField wrapperField = wrapperFieldTypeIterator.next();
            DictionaryMetaInfo fieldMetaInfo = metaInfoMap.get(wrapperField.getFieldTypeName());
            if (Objects.nonNull(fieldMetaInfo)) {
                if (Objects.isNull(fieldMetaInfo.getWrapperClass())) {
                    fieldMetaInfo.setWrapperClass(generateWrapper(fieldMetaInfo, metaInfoMap));
                }

                addField(wrapperField.getFieldName(), fieldMetaInfo.getWrapperClass(), evalClass);
            }
        }

        try {
            evalClass.setInterfaces(new CtClass[] {pool.get(DictionaryConverter.class.getName())});
            evalClass.addMethod(generateConvertMethod(pool, evalClass, metaInfo));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }

        return (Class<DictionaryConverter>) evalClass.toClass();
    }

    private static void addField(String fieldName, Class<?> fieldType, CtClass evalClass)
            throws CannotCompileException {
        CtField ctField = CtField.make("private " + fieldType.getName() + " " + fieldName + ";", evalClass);

        evalClass.addMethod(CtNewMethod.getter("get" + capitalize(fieldName), ctField));
        evalClass.addMethod(CtNewMethod.setter("set" + capitalize(fieldName), ctField));

        evalClass.addField(ctField);
    }

    private static String getWrapperClassName(Class<?> targetClass) {
        return "$$" + targetClass.getName() + "DictionaryWrapper";
    }

    private static CtConstructor generateDefaultConstructor(CtClass evalClass) throws CannotCompileException {
        CtConstructor cons = new CtConstructor(new CtClass[]{}, evalClass);
        cons.setBody("{}");
        return cons;
    }

    private static CtMethod generateConvertMethod(ClassPool classPool, CtClass evalClass, DictionaryMetaInfo metaInfo)
            throws CannotCompileException, NotFoundException {
        String methodName = "convert";
        CtClass objectClass = classPool.get(Object.class.getName());
        Class<?> targetClass = metaInfo.getOriginClass();
        CtMethod method = new CtMethod(objectClass, methodName, new CtClass[]{objectClass}, evalClass);

        method.setModifiers(Modifier.PUBLIC);

        StringBuilder methodBody = new StringBuilder();
        methodBody.append("{ ")
                .append("if ($1 == null) return null; ")
                .append("if (!($1 instanceof ").append(targetClass.getName()).append(")) return $1; ")
                .append(targetClass.getName()).append(" cast = (").append(targetClass.getName()).append(") $1;")
                .append(evalClass.getName()).append(" result = ").append(" new ").append(evalClass.getName()).append("();");

        Iterator<DictionaryMetaInfo.RegularField> fieldIterator = metaInfo.regularFieldIterator();
        while (fieldIterator.hasNext()) {
            DictionaryMetaInfo.RegularField field = fieldIterator.next();
            String fieldName = field.getFieldName();
            methodBody.append(" result.set").append(capitalize(fieldName)).append("(cast.get").append(capitalize(fieldName)).append("()); ");
        }

        methodBody.append("return result; }");

        method.setBody(methodBody.toString());

        return method;
    }

    public static Object newInstanceAndFillField(Class<? extends DictionaryConverter> clazz, Object originValue)
            throws IllegalAccessException, InstantiationException {
        DictionaryConverter converter = clazz.newInstance();
        return converter.convert(originValue);
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        String getterMethodName = "get" + capitalize(fieldName);
        Method method = ReflectionUtils.findMethod(obj.getClass(), getterMethodName);

        if (Objects.isNull(method)) {
            log.warn("{}#{}() 方法不存在", obj.getClass().getName(), getterMethodName);
            return null;
        }

        return ReflectionUtils.invokeMethod(method, obj);
    }

    public static Long toLongValue(Object o) {
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

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        String setterMethodName = "set" + capitalize(fieldName);
        Method method = ReflectionUtils.findMethod(obj.getClass(), setterMethodName, value.getClass());

        if (Objects.isNull(method)) {
            log.warn("{}#{}({}) 方法不存在", obj.getClass().getName(), setterMethodName, value.getClass().getName());
            return;
        }

        ReflectionUtils.invokeMethod(method, obj, value);
    }
}
