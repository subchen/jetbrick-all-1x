Version 1.0.5 (2014-06-05)
-----------------------------

* [新增] RequestContext允许获取 Controller 和 Action 的元数据
* [新增] 允许动态设置 ASM 生效规则
* [增强] 设置 jsp, jetx 视图默认的 ContentType 为 text/html

Version 1.0.4 (2014-05-30)
-----------------------------

* [新增] 支持 Object 作为 Action 返回值
* [新增] 支持 subclass 作为 Action 的返回值
* [修复] 无法自动发现 ResultHandler 和 ArgumentGetter
* [修复] 调用 private 的默认构造函数没有权限

Version 1.0.3 (2014-05-26)
-----------------------------

* [新增] 新增 JDK8 方法参数名支持
* [新增] 新增 更多的 View 支持
* [新增] 新增 更多的 Action 参数注入
* [新增] 新增 AOP 风格的 Interceptor
* [增强] 增强 RawData
* [增强] 替换 IoC 以及所有相关的反射代码为 KlassInfo
* [增强] 大量优化 KlassInfo
* [修复] 部分 bugs

Version 1.0.2 (2014-05-23)
-----------------------------

* [新增] 新增 PlainTextViewHandler 和 JavaScriptViewHandler
* [新增] KlassInfo 相关反射 ASM 自适应增强功能
* [增强] 增强 KlassInfo 相关类库
* [修复] 在 JDK 6 下面的 KlassInfo 反射库报错
* [修复] AbstractTemplateViewHandler 中关于 suffix 的初始化问题


Version 1.0.1 (2014-05-18)
-----------------------------

* [新增] jetbrick-orm 模块
* [新增] jetbrick.lang.concurrent
* [新增] jetbrick.asm (asm-5.0.2)
* [新增] jetbrick.reflect, jetbrick.reflect.asm
* [变更] jetbrick-commons 模块包名设为顶层包
* [变更] jetbrick.web 移到 jetbrick-commons 中

Version 1.0.0 (2014-05-11)
--------------------------------

- jetbrick-commons
- jetbrick-ioc
- jetbrick-webmvc
- First Public Release

