package priv.zxw.dictranslate.config;

import javassist.ClassPool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import priv.zxw.dictranslate.DictionaryBeanPostProcessor;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2021/7/22
 */
@Configuration
@ConditionalOnClass(ClassPool.class)
public class DictionaryTranslateConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DictionaryBeanPostProcessor dictionaryBeanPostProcessor() {
        return new DictionaryBeanPostProcessor();
    }
}
