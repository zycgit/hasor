Hasor-Core v0.0.1
    01.)Hasor-Core：80%以上代码重构，重构主要涉及内容的是结构性重构。
    		重构之后将会使Hasor核心层的逻辑更加清晰，更便于扩展核心层功能。
        1.InitContext接口功能合并到Environment接口中。
        2.ApiBinder接口增加模块依赖管理。
        3.HasorModule接口更名为Module。
        4.HasorEventListener接口更名为EventListener。
        5.XmlProperty接口更名为XmlNode。
        6.config-mapping.properties属性文件的解析不在是必须的。
        7.重构Settings实现。Xml解析方式不在依赖ns.prop属性文件，实现方式改为Sax。
        8.@Module注解，更名为@AnnoModule。
        9.增加@GuiceModule注解，可以标记在com.google.inject.Module接口上，可以将Guice模块引入到Hasor中。
        A.重构AppContext实现。
        B.包空间整理，所有包都被移动到net.hasor下，整理License文件。删除残余的、无用的类。
        C.删除所有与Web相关的支持，这部分功能全部移动到Hasor-Web（Hasor-MVC更名而来）。
        D.生命周期：合并onReady和onInit两个生命周期阶段方法，删除销毁过程。
    02.)工具包修订：
        1.ResourcesUtils工具类中，类扫描代码优化。
        2.DecSequenceMap.java、DecStackMap.java两个类文件增加一些有用的方法。
    03.)所有Demo程序都汇总到demo-project项目中。

Hasor-Core v0.0.2
    1.修改：DefaultXmlProperty类更名为DefaultXmlNode，并且XmlNode增加几个常用方法。
    2.修改：删除所有Mapping部分支持，相关代码移到demo作为例子程序。
    3.修改：AbstractAppContext类中有关事件的声明移动到 AppContext 接口中。
    4.修改：@Before 更名为 @Aop，性能进行了优化。
    5.升级：ASM升级为4.0、ClassCode连带升级。
    6.增加：以模块类名为事件名，当执行 Init\Start\Stop时候，抛出对应事件。
    7.增加：增加 Gift 体系用于扩展非模块类小工具。
    8.修复：StandardAppContext调用无参构造方法引发异常的问题，同时修改几个核心类的构造方法。

Hasor-Core v0.0.3
	1.修改：根POM改为 0.0.2 该版本可以处理 GBK 编码下 Javadocs 生成。
	2.改进JavaDoc内容的质量。

Hasor-Core v0.0.4
	1.新增：ICache 缓存服务，使用 @NeedCache在方法上声明方法结果缓存。
