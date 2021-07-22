package priv.zxw.dictranslate.translater;

import priv.zxw.dictranslate.entity.DictionaryEntity;

/**
 * 字典translater空实现
 * 一般作为占位符
 *
 * @author zhangxunwei
 * @date 2021/7/22
 */
public class NoopDictionaryTranslater implements DictionaryTranslater {

    @Override
    public DictionaryEntity translate(Integer type, Long id) {
        return new DictionaryEntity(type, id);
    }
}
