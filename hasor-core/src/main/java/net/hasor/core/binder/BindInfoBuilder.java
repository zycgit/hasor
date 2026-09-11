/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.binder;
import java.util.function.Supplier;
import net.hasor.cobble.dynamic.DynamicProperty;
import net.hasor.cobble.dynamic.ReadWriteType;
import net.hasor.cobble.provider.Scope;
import net.hasor.core.BindInfo;

/**
 * Bean配置接口，用于对Bean信息进行全方面配置。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014年7月2日
 */
public interface BindInfoBuilder<T> {
    /**
     * 为绑定设置ID。
     * @param newID newID
     */
    void setBindID(String newID);

    /**
     * 为类型绑定一个名称。
     * @param bindName 名称
     */
    void setBindName(String bindName);

    /**
     * 为类型绑定一个实现，当获取类型实例时其实获取的是实现对象。
     * @param sourceType 实现类
     */
    void setSourceType(Class<? extends T> sourceType);

    /**
     * 设置元信息。
     * @param key metaData key
     * @param value metaData value
     */
    void setMetaData(String key, Object value);

    /**
     * 开发者自定义的{@link Supplier}。
     * @param customerProvider 设置自定义{@link Supplier}
     */
    void setCustomerProvider(Supplier<? extends T> customerProvider);

    /**
     * 加入一个 Scope。
     * @param scopeProvider 命名空间
     */
    void addScopeProvider(Supplier<Scope> scopeProvider);

    /**
     * 加入一个 Scope。
     * @param scopeProvider 命名空间
     */
    default void addScopeProvider(Supplier<Scope>[] scopeProvider) {
        if (scopeProvider != null) {
            for (Supplier<Scope> scope : scopeProvider) {
                this.addScopeProvider(scope);
            }
        }
    }

    /**
     * 清空已经加入的所有 Scope。
     */
    void clearScope();

    /**
     * 设置构造参数。
     * @param index 参数索引
     * @param paramType 参数类型
     * @param valueProvider 参数值
     */
    void setConstructor(int index, Class<?> paramType, Supplier<?> valueProvider);

    /**
     * 设置构造参数。
     * @param index 参数索引
     * @param paramType 参数类型
     * @param valueInfo 参数值
     */
    void setConstructor(int index, Class<?> paramType, BindInfo<?> valueInfo);

    /**
     * 添加依赖注入。
     * @param property 属性名
     * @param valueProvider 属性值
     */
    void addInject(String property, Supplier<?> valueProvider);

    /**
     * 添加依赖注入。
     * @param property 属性名
     * @param valueInfo 属性值
     */
    void addInject(String property, BindInfo<?> valueInfo);

    /**
     * 动态添加属性。
     * @param name 属性名
     * @param propertyType 属性类型
     * @param delegate 属性值的委托
     */
    void addDynamicProperty(String name, Class<?> propertyType, Supplier<? extends DynamicProperty> delegate, ReadWriteType rwType);

    /**
     * 转化为{@link BindInfo}类型对象。
     * @return 返回{@link BindInfo}类型对象。
     */
    BindInfo<T> toInfo();

    /**
     * 设置初始化方法，一个无参的方法。例如：public void init(){ ... }。
     * @param methodName 方法名。
     */
    void initMethod(String methodName);

    /**
     * 设置初始化方法，一个无参的方法。例如：public void init(){ ... }。
     * @param methodName 方法名。
     */
    void destroyMethod(String methodName);

    /**
     * 是否强制忽略类的注解配置
     * @param overwrite 如果为 true 表示覆盖，默认是 false
     */
    void overwriteAnnotation(boolean overwrite);
}
