### 测试用例

1. 互相引用字段的class解析正常（即A中有B字段，B中有A字段）
2. 字段无getter方法时该字段不进行解析
3. 普通对象生成wrapper class成功，并赋值成功
4. 单层泛型对象生成wrapper class成功，并赋值成功
5. 多层泛型对象生成wrapper class成功，并赋值成功
6. 数组/Collection & 普通对象生成wrapper class成功，并赋值成功
7. 数组/Collection & 单层泛型对象生成wrapper class成功，并赋值成功
8. 数组/Collection & 多层泛型对象生成wrapper class成功，并赋值成功
9. Map & 普通对象生成wrapper class成功，并赋值成功
10. Map & 单层泛型对象生成wrapper class成功，并赋值成功
11. Map & 多层泛型对象生成wrapper class成功，并赋值成功
12. 自定义DictionaryEntity格式，生成wrapper class成功，并赋值成功
14. 测试创建动态 class是否会导致内存泄漏
15. 测试创建动态 class对象是否会导致内存泄漏
16. 测试创建动态class时间
16. ~~将用户自定义的 DictionaryTranslate 注册到BeanFactory~~

**备注**  
普通对象： `String、Integer`、自定义非泛型对象  
单层泛型对象： `RestResult<T>`  
多层泛型对象： `RestResult<SampleObject<T, E>>`或者更多层嵌套的泛型