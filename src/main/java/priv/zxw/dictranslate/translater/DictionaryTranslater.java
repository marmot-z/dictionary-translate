package priv.zxw.dictranslate.translater;

import priv.zxw.dictranslate.entity.DictionaryEntity;

public interface DictionaryTranslater {

    /**
     * 翻译字典
     *
     * @param type  字典类型
     * @param id    字典唯一标识
     * @return
     */
    DictionaryEntity translate(Integer type, Integer id);
}
