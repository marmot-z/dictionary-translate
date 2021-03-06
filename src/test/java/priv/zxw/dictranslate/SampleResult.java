package priv.zxw.dictranslate;

import lombok.Data;
import priv.zxw.dictranslate.annotation.Dictionary;
import priv.zxw.dictranslate.translater.DictionaryTranslater;

@Data
public class SampleResult {

    @Dictionary(type = 1, translater = DictionaryTranslater.class)
    private String enumName;

    private String commonName;

    private Integer age;
}
