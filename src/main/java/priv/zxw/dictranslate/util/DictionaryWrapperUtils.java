package priv.zxw.dictranslate.util;

import javassist.*;
import priv.zxw.dictranslate.converter.DictionaryConverter;
import priv.zxw.dictranslate.entity.DictionaryEntity;
import priv.zxw.dictranslate.annotation.Dictionary;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.springframework.util.StringUtils.capitalize;

public class DictionaryWrapperUtils {

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
}
