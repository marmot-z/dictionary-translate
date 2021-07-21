**自动将字典类型字段值翻译成字典格式的工具**

受 [DictAspect.java](https://github.com/jeecgboot/jeecg-boot/blob/master/jeecg-boot/jeecg-boot-base/jeecg-boot-base-core/src/main/java/org/jeecg/common/aspect/DictAspect.java) 启发，使用注解对字典字段进行翻译，减少开发量。但是其有以下几个问题：
1. 不能对注解预处理，如验证注解参数是否有问题，获取注解元信息
2. 不能自定义字典的翻译逻辑
3. 不能和spring比较好的融合。可以ResponseBodyAdvice进行字典翻译逻辑处理(而不是使用额外的切面)。使用spring的序列化方式，而不是手动指定fastjson进行序列化。
4. 不能翻译嵌套格式的对象

### 前提
对字典翻译做以下约定：
1. 翻译后的字典格式为: `{id: 1, value: '字典值1', type: 1}`
2. 翻译方式：通过字典id和字典type进行字典翻译
3. 翻译途径：枚举、数据库、缓存或自定义等多种方式

### 使用
在响应结果类中为字典字段加上`@Dictionary`注解
```java
class Demo {
    @Dictionary(type= 1, translater = SexEnumDictionaryTranslater.class)
    private Integer sex;
    
    private String otherField1;
    
    private Date otherField2;
}
```
编写对应的字典翻译类
```java
// 请确保你编写的字典翻译类被spring管理
@Component
class DemoEnumTranslater implements DictionaryTranslater {
    @Override
    public DictionaryEntity translate(Integer type, Long id) {
        DictionaryEntity entity = new DictionaryEntity(type, id);
        try {
            SexEnum sexEnum = SexEnum.get((int) id);
            entity.setValue(sexEnum.getValue());
            return entity;
        } catch (Exception e) {
            return entity;
        }
    }
}

class SexEnum {
    MAN(1,"男"),
    WOMAN(2,"女"),
    UNKNOWN(3,"未知");

    private int code;
    private String value;

    SexEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SexEnum get(Integer code) {
        for (SexEnum value : SexEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("无此枚举");
    }
}
```
原先结果
```json
{
  "sex": 1,
  "otherField1": "xxx",
  "otherField2": "2021-07-18 12:00:00"
}
```
字典翻译后结果
```json
{
  "sex": {
    "type": 1,
    "value": "男",
    "id": 1
  },
  "otherField1": "xxx",
  "otherField2": "2021-07-18 12:00:00"
}
```
### 待开发功能
- [ ] 支持列表、数组类型字段上的字典翻译
- [ ] 支持自定义的返回字典格式
- [ ] 支持嵌套复杂格式类型的字典翻译
- [ ] 支持自定义配置，包括但不限于默认translater，动态生成class方式
- [ ] 支持getter方法的字典注解  
- [ ] 支持groovy方式的生成动态class
- [ ] 编写成SpringBoot starer