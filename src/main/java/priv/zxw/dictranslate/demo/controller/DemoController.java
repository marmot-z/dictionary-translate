package priv.zxw.dictranslate.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import priv.zxw.dictranslate.demo.entity.RestResult;
import priv.zxw.dictranslate.demo.entity.SampleResult;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2021/7/20
 */
@RequestMapping("/demo")
@RestController
public class DemoController {

    @RequestMapping("/method1")
    public SampleResult method1() {
        SampleResult sampleResult = new SampleResult();
        sampleResult.setAge(233);
        sampleResult.setCommonName("名称233");
        sampleResult.setEnumId(1);
        return sampleResult;
    }

    @RequestMapping("/method2")
    public RestResult<SampleResult> method2() {
        SampleResult sampleResult = new SampleResult();
        sampleResult.setAge(233);
        sampleResult.setCommonName("名称233");
        sampleResult.setEnumId(1);
        return new RestResult<>(sampleResult);
    }
}
