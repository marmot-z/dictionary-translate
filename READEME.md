一个用于将web请求返回结果的字段值翻译成字典格式的工具

受 [DictAspect.java](https://github.com/jeecgboot/jeecg-boot/blob/master/jeecg-boot/jeecg-boot-base/jeecg-boot-base-core/src/main/java/org/jeecg/common/aspect/DictAspect.java) 启发

### 前提
假设字典的格式为 `{id: 1, value: '字典值1', type: 1}`

### 待开发功能
- [ ] 支持列表、数组类型字段上的字典翻译
- [ ] 支持groovy方式的生成动态class
- [ ] 支持id/value两种形式的字典翻译
- [ ] 支持自定义字典格式
- [ ] 支持嵌套复杂类型的格式