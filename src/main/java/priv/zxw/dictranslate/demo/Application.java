package priv.zxw.dictranslate.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2021/7/20
 */
@SpringBootApplication(scanBasePackages = "priv.zxw.dictranslate")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
