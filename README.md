**自动将字典类型字段值翻译成字典格式的工具**

受 [DictAspect.java](https://github.com/jeecgboot/jeecg-boot/blob/master/jeecg-boot/jeecg-boot-base/jeecg-boot-base-core/src/main/java/org/jeecg/common/aspect/DictAspect.java) 启发，使用注解对字典字段进行翻译，减少开发量。但是其有以下几个问题：
1. 不能对注解预处理，如验证注解参数是否有问题，获取注解元信息
2. 不能自定义字典的翻译逻辑
3. 不能和spring比较好的融合。可以ResponseBodyAdvice进行字典翻译逻辑处理(而不是使用额外的切面)。使用spring的序列化方式，而不是手动指定fastjson进行序列化。
4. 不能翻译嵌套格式的对象

### 前提
假设字典的格式为 `{id: 1, value: '字典值1', type: 1}`，通过id查询字典。查询方式可以有枚举、数据库、缓存等多种方式。

### 待开发功能
- [ ] 支持列表、数组类型字段上的字典翻译
- [ ] 支持groovy方式的生成动态class
- [ ] 支持自定义字典格式
- [ ] 支持嵌套复杂类型的格式
- [ ] 制作成SpringBoot starer