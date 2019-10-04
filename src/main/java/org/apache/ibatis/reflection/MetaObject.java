/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 元数据对象(MetaObject)实际上就是提供 类|集合|Map 的一种自动识别的访问形式.
 * <p>
 * ObjectMetaInfo表示某个对象的元信息
 * <p>
 * 构造器是私有的, 通过forObject方法来获取MetaObject实例
 * <p>
 * 包含originalObject, wrapper, wrapperFactory, objectFactory四个属性
 * <p>
 * 元对象,各种get，set方法, 似乎是为ognl(Object-Graph Navigation Language)提供支持
 * <p>
 * 可以参考MetaObjectTest来跟踪调试，基本上用到了reflection包下所有的类
 */
public class MetaObject {

    //有一个原来的对象，对象包装器，对象工厂，对象包装器工厂
    /** 原始对象, 通过构造器参数初始化: this.originalObject = object; */
    private Object originalObject;
    /**
     * 包在originalObject外面
     */
    private Wrapper wrapper;
    /**
     * 用于得到wrapper, 好为wrapper属性赋值
     */
    private WrapperFactory wrapperFactory;

    private ObjectFactory objectFactory;

    /**
     * !!! 私有的构造器
     * <p>
     * 想法设法得到originalObject的wrapper
     *
     * @param originalObject 原始对象
     * @param objectFactory  对象工厂
     * @param wrapperFactory 对象Wrapper工厂
     */
    private MetaObject(Object originalObject, ObjectFactory objectFactory, WrapperFactory wrapperFactory) {
        this.originalObject = originalObject;
        this.objectFactory = objectFactory;
        this.wrapperFactory = wrapperFactory;

        //想法设法得到originalObject的wrapper
        // 根据originalObject的类型实例化具体的Wrapper, 将MetaObject自己注入进去，委派模式
        if (originalObject instanceof Wrapper) {
            //如果对象本身已经是ObjectWrapper型，则直接赋给objectWrapper
            this.wrapper = (Wrapper) originalObject;
        } else if (wrapperFactory.hasWrapperFor(originalObject)) {
            //尝试将object包装
            this.wrapper = wrapperFactory.getWrapperFor(this, originalObject);
        } else if (originalObject instanceof Map) {
            //如果是Map型，返回MapWrapper
            this.wrapper = new MapWrapper(this, (Map) originalObject);
        } else if (originalObject instanceof Collection) {
            //如果是Collection型，返回CollectionWrapper
            this.wrapper = new CollectionWrapper(this, (Collection) originalObject);
        } else {
            //除此以外，返回BeanWrapper
            this.wrapper = new BeanWrapper(this, originalObject);
        }
    }

    /**
     * 静态工厂
     * <p>
     * 根据originalObject new 一个MetaObject 返回<br/>
     */
    public static MetaObject forObject(Object originalObject, ObjectFactory objectFactory, WrapperFactory wrapperFactory) {
        if (originalObject == null) {
            //处理一下null, 如果object为null, 将其转换为NULL_META_OBJECT
            return SystemMetaObject.NULL_META_OBJECT;
        } else {
            return new MetaObject(originalObject, objectFactory, wrapperFactory);
        }
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public WrapperFactory getWrapperFactory() {
        return wrapperFactory;
    }

    public Object getOriginalObject() {
        return originalObject;
    }

    //--------以下方法都是委派给ObjectWrapper------
    //查找属性
    public String findProperty(String propName, boolean useCamelCaseMapping) {
        return wrapper.findProperty(propName, useCamelCaseMapping);
    }

    //取得getter的名字列表
    public String[] getGetterNames() {
        return wrapper.getGetterNames();
    }

    //取得setter的名字列表
    public String[] getSetterNames() {
        return wrapper.getSetterNames();
    }

    //取得setter的类型列表
    public Class<?> getSetterType(String name) {
        return wrapper.getSetterType(name);
    }

    /**
     * 传入"person[0].birthdate.year", 将返回int
     *
     * @param name 属性名(链式表示)
     */
    //取得getter的类型列表
    public Class<?> getGetterType(String name) {
        return wrapper.getGetterType(name);
    }

    //是否有指定的setter
    public boolean hasSetter(String name) {
        return wrapper.hasSetter(name);
    }

    //是否有指定的getter
    public boolean hasGetter(String name) {
        return wrapper.hasGetter(name);
    }

    /**
     * 传入属性名, 返回属性的值, 相当于originalObject.getXXX
     *
     * @param name 属性名
     */
    //取得值
    //如person[0].birthdate.year
    //具体测试用例可以看MetaObjectTest
    public Object getValue(String name) {
        //创建一个全名属性标记器
        PropertyTokenizer propertyTokenizer = new PropertyTokenizer(name);
        if (propertyTokenizer.hasNext()) {
            MetaObject metaValue = metaObjectForProperty(propertyTokenizer.getIndexedName());
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                //如果上层就是null了, 那就结束递归, 返回null
                return null;
            } else {
                //否则继续看下一层，递归调用getValue
                return metaValue.getValue(propertyTokenizer.getChildren());
            }
        } else {//递归出口, 如果游标已经到了最后, 则返回这个属性的值
            return wrapper.get(propertyTokenizer);
        }
    }

    /**
     * @param name  属性名
     * @param value 属性值
     */
    //设置值
    //如person[0].birthdate.year
    public void setValue(String name, Object value) {
        PropertyTokenizer propertyTokenizer = new PropertyTokenizer(name);
        if (propertyTokenizer.hasNext()) {
            MetaObject metaValue = metaObjectForProperty(propertyTokenizer.getIndexedName());
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                if (value == null && propertyTokenizer.getChildren() != null) {
                    // don't instantiate child path if value is null
                    //如果上层就是null了，还得看有没有儿子，没有那就结束
                    return;
                } else {
                    //否则还得new一个，委派给ObjectWrapper.instantiatePropertyValue
                    metaValue = wrapper.instantiatePropertyValue(name, propertyTokenizer, objectFactory);
                }
            }
            //递归调用setValue
            metaValue.setValue(propertyTokenizer.getChildren(), value);
        } else {
            //到了最后一层了，所以委派给ObjectWrapper.set
            wrapper.set(propertyTokenizer, value);
        }
    }

    /**
     * 为某个属性生成元对象
     *
     * @param name 属性名
     */
    public MetaObject metaObjectForProperty(String name) {
        //实际是递归调用
        //getValue和metaObjectForProperty相互来回调用
        //value就是name对应的值
        Object value = getValue(name);
        //把value作为originalObject, 生成一个MetaObject
        return MetaObject.forObject(value, objectFactory, wrapperFactory);
    }

    public Wrapper getWrapper() {
        return wrapper;
    }

    //是否是集合
    public boolean isCollection() {
        return wrapper.isCollection();
    }

    /**
     * 被包装对象为Collection时, 会用到此方法
     * <p>
     * 向集合中增加元素
     */
    public void add(Object element) {
        wrapper.add(element);
    }

    /**
     * 被包装对象为Collection时, 会用到此方法
     * <p>
     * 向集合中增加元素
     */
    public <E> void addAll(List<E> list) {
        wrapper.addAll(list);
    }

}
