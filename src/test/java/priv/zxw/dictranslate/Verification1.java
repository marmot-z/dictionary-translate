package priv.zxw.dictranslate;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.springframework.util.ReflectionUtils;
import priv.zxw.dictranslate.converter.DictionaryConverter;
import priv.zxw.dictranslate.entity.DictionaryEntity;
import priv.zxw.dictranslate.util.DictionaryWrapperUtils;

import java.lang.reflect.Method;

public class Verification1 {

    public static void main(String[] args) throws NotFoundException, CannotCompileException, InstantiationException, IllegalAccessException {
        // 根据已知的class生成wrapper class
        Class<DictionaryConverter> converterClass = DictionaryWrapperUtils.generateWrapper(SampleResult.class);
        // 将对象值赋给wrapper bean上
        SampleResult bean = new SampleResult();
        bean.setCommonName("测试名称1");
        bean.setAge(23);
        bean.setEnumName("枚举值233");

        DictionaryConverter wrapper = converterClass.newInstance();
        Object result = wrapper.convert(bean);
        Method method = ReflectionUtils.findMethod(converterClass, "setEnumName", DictionaryEntity.class);
        ReflectionUtils.invokeMethod(method, result, new DictionaryEntity(1L, 2, "打卡时间发"));
        System.out.println(result);
    }
}
