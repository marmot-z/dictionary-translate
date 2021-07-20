package priv.zxw.dictranslate;

import lombok.Data;
import priv.zxw.dictranslate.annotation.Dictionary;
import priv.zxw.dictranslate.translater.AbstractEnumDictionaryTranslater;

@Data
public class SampleResult {

    @Dictionary(type = 1, translater = AbstractEnumDictionaryTranslater.class)
    private String enumName;

    private String commonName;

    private Integer age;
}
