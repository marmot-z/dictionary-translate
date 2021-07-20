package priv.zxw.dictranslate.demo.entity;

import lombok.Data;
import priv.zxw.dictranslate.annotation.Dictionary;
import priv.zxw.dictranslate.demo.translater.EnumDictionaryTranslater;
import priv.zxw.dictranslate.translater.DictionaryTranslater;

@Data
public class SampleResult {

    @Dictionary(type = 1, translater = EnumDictionaryTranslater.class)
    private Integer enumId;

    private String commonName;

    private Integer age;
}
