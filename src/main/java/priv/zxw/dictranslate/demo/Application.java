package priv.zxw.dictranslate.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import priv.zxw.dictranslate.annotation.EnableDictionaryTranslate;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2021/7/20
 */
@SpringBootApplication(scanBasePackages = "priv.zxw.dictranslate")
@EnableDictionaryTranslate
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
