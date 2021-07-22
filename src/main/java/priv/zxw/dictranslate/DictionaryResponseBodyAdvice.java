package priv.zxw.dictranslate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import priv.zxw.dictranslate.converter.DictionaryConverter;
import priv.zxw.dictranslate.entity.DictionaryEntity;
import priv.zxw.dictranslate.entity.DictionaryMetaInfo;
import priv.zxw.dictranslate.translater.DictionaryTranslater;
import priv.zxw.dictranslate.util.ClassParser;
import priv.zxw.dictranslate.util.DictionaryWrapperUtils;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Objects;

@ControllerAdvice
public class DictionaryResponseBodyAdvice implements ResponseBodyAdvice<Object>, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(DictionaryResponseBodyAdvice.class);

    private ApplicationContext applicationContext;

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        Method method = methodParameter.getMethod();
        if (Objects.isNull(method)) {
            return false;
        }

        String returnTypeName = ClassParser.getMethodReturnTypeName(method);
        return DictionaryBeanPostProcessor.metaInfoMap.containsKey(returnTypeName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (Objects.isNull(o)) {
            return o;
        }

        Method method = methodParameter.getMethod();
        if (Objects.isNull(method)) {
            return o;
        }

        String returnTypeName = ClassParser.getMethodReturnTypeName(method);
        return assignValue(o, returnTypeName);
    }

    private Object assignValue(Object origin, String typeName) {
        DictionaryMetaInfo metaInfo = DictionaryBeanPostProcessor.metaInfoMap.get(typeName);
        if (Objects.isNull(metaInfo)) {
            log.warn("{} 对应的字典元信息为空", typeName);
            return origin;
        }

        Object wrapper;
        Class<? extends DictionaryConverter> wrapperClass = metaInfo.getWrapperClass();
        try {
            wrapper = DictionaryWrapperUtils.newInstanceAndFillField(wrapperClass, origin);
        } catch (IllegalAccessException | InstantiationException e) {
            log.warn("创建 {} 字典包装对象失败", wrapperClass, e);
            return origin;
        }

        Iterator<DictionaryMetaInfo.DictionaryField> dictionaryFieldIterator = metaInfo.dictionaryFieldIterator();
        while (dictionaryFieldIterator.hasNext()) {
            DictionaryMetaInfo.DictionaryField field = dictionaryFieldIterator.next();
            DictionaryEntity entity;
            try {
                Long id = DictionaryWrapperUtils.toLongValue(DictionaryWrapperUtils.getFieldValue(origin, field.getFieldName()));
                entity = translate(field, id);
            } catch (Exception e) {
                log.warn("获取 {}#{} 字段值失败", origin.getClass().getName(), field.getFieldName(), e);
                entity = new DictionaryEntity(field.getType(), null);
            }

            try {
                DictionaryWrapperUtils.setFieldValue(wrapper, field.getFieldName(), entity);
            } catch (Exception e) {
                log.error("设置 {}#{} 字段值失败", origin.getClass().getName(), field.getFieldName(), e);
            }
        }

        Iterator<DictionaryMetaInfo.WrapperField> wrapperFieldIterator = metaInfo.wrapperFieldTypeIterator();
        while (wrapperFieldIterator.hasNext()) {
            DictionaryMetaInfo.WrapperField wrapperField = wrapperFieldIterator.next();
            Object fieldValue;
            try {
                fieldValue = DictionaryWrapperUtils.getFieldValue(origin, wrapperField.getFieldName());
            } catch (Exception e) {
                log.warn("获取 {}#{} 字段值失败", origin.getClass().getName(), wrapperField.getFieldName(), e);
                continue;
            }
            Object fieldWrapperValue = assignValue(fieldValue, wrapperField.getFieldTypeName());

            try {
                DictionaryWrapperUtils.setFieldValue(wrapper, wrapperField.getFieldName(), fieldWrapperValue);
            } catch (Exception e) {
                log.error("设置 {}#{} 字段值失败", origin.getClass().getName(), wrapperField.getFieldName(), e);
            }
        }

        return wrapper;
    }

    private DictionaryEntity translate(DictionaryMetaInfo.DictionaryField field, Long id) {
        if (Objects.isNull(id)) {
            return new DictionaryEntity(field.getType(), null);
        }

        DictionaryTranslater translater = getTranslater(field.getTranslaterClass());
        DictionaryEntity entity = new DictionaryEntity();
        entity.setId(id);
        entity.setType(field.getType());

        try {
            return translater.translate(field.getType(), id);
        } catch (Exception e) {
            log.warn("翻译 {} 字段失败", field.getFieldName(), e);
            return entity;
        }
    }

    private DictionaryTranslater getTranslater(Class<? extends DictionaryTranslater> clazz) {
        // 如果bean不存在则会抛出异常
        return applicationContext.getBean(clazz);
    }
}
