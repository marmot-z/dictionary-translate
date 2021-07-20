package priv.zxw.dictranslate.demo.translater;

import org.springframework.stereotype.Component;
import priv.zxw.dictranslate.demo.entity.DemoEnum;
import priv.zxw.dictranslate.entity.DictionaryEntity;
import priv.zxw.dictranslate.translater.DictionaryTranslater;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2021/7/20
 */
@Component
public class EnumDictionaryTranslater implements DictionaryTranslater {

    @Override
    public DictionaryEntity translate(Integer type, Long id) {
        DictionaryEntity entity = new DictionaryEntity(id, type);

        DemoEnum e = DemoEnum.get(id.intValue());
        entity.setValue(e.getValue());

        return entity;
    }
}
