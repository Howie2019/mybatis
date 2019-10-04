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
package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.*;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.util.List;

/**
 * 把被包装对象和其对应的MetaClass wrap一下, 就得到了BeanWrapper
 */
public class BeanWrapper extends BaseWrapper {

    /** 被包装的对象 */
    private Object bean;
    /** bean所属类的MetaClass, 换言之metaClass是由bean导出的 */
    private MetaClass metaClass;

    public BeanWrapper(MetaObject metaObject, Object bean) {
        super(metaObject);
        this.bean = bean;
        this.metaClass = MetaClass.forClass(bean.getClass());
    }

    /**
     * 获得PropertyTokenizer所指示的属性的值
     */
    @Override
    public Object get(PropertyTokenizer propertyTokenizer) {
        //如果有索引([]部分),说明是集合，那就要解析集合,调用的是BaseWrapper.resolveCollection 和 getCollectionValue
        if (propertyTokenizer.getIndex() != null) {//说明propertyTokenizer所指属性是集合
            // 使用MetaObject逐级实例化集合对象
            Object collection = resolveCollection(propertyTokenizer, bean);
            // 使用MetaObject获得Value
            return getCollectionValue(propertyTokenizer, collection);
        } else {
            //否则，getBeanProperty
            return getBeanProperty(propertyTokenizer, bean);
        }
    }

    @Override
    public void set(PropertyTokenizer propertyTokenizer, Object value) {
        //如果有index,说明是集合，那就要解析集合,调用的是BaseWrapper.resolveCollection 和 setCollectionValue
        if (propertyTokenizer.getIndex() != null) {
            Object collection = resolveCollection(propertyTokenizer, bean);
            setCollectionValue(propertyTokenizer, collection, value);
        } else {
            //否则，setBeanProperty
            setBeanProperty(propertyTokenizer, bean, value);
        }
    }

    /**
     * 类似于一种模糊查询. 如果useCamelCaseMapping为true, 那么name的大小写会被忽略(Age和age被视作相同).
     *
     * @param name                属性名(全名)
     * @param useCamelCaseMapping 是否映射为驼峰命名
     * @return 实际属性名
     */
    @Override
    public String findProperty(String name, boolean useCamelCaseMapping) {
        return metaClass.findProperty(name, useCamelCaseMapping);
    }

    @Override
    public String[] getGetterNames() {
        return metaClass.getGetterNames();
    }

    @Override
    public String[] getSetterNames() {
        return metaClass.getSetterNames();
    }

    /**
     * SetterType就是属性的类型
     * <p>
     * 假设传入"person[0].birthdate.year",则会返回year的属性类型
     *
     * @param name 属性全名, 如person[0].birthdate.year
     */
    @Override
    public Class<?> getSetterType(String name) {
        //name->propertyTokenizer
        PropertyTokenizer propertyTokenizer = new PropertyTokenizer(name);
        if (propertyTokenizer.hasNext()) {//propertyTokenizer没有指向属性链的末节点
            MetaObject metaValue = metaObject.metaObjectForProperty(propertyTokenizer.getIndexedName());
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {//这个分支是递归出口
                return metaClass.getSetterType(name);
            } else {
                //MetaObject的getSetterType和Wrapper的getSetterType递归起来了
                //通过递归, 将propertyTokenizer指向属性链的末节点
                return metaValue.getSetterType(propertyTokenizer.getChildren());
            }
        } else {//propertyTokenizer已经指向属性链的末节点
            return metaClass.getSetterType(name);
        }
    }

    @Override
    public Class<?> getGetterType(String name) {
        PropertyTokenizer propertyTokenizer = new PropertyTokenizer(name);
        if (propertyTokenizer.hasNext()) {
            MetaObject metaValue = metaObject.metaObjectForProperty(propertyTokenizer.getIndexedName());
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                return metaClass.getGetterType(name);
            } else {
                return metaValue.getGetterType(propertyTokenizer.getChildren());
            }
        } else {
            return metaClass.getGetterType(name);
        }
    }

    @Override
    public boolean hasSetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            if (metaClass.hasSetter(prop.getIndexedName())) {
                MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
                if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                    return metaClass.hasSetter(name);
                } else {
                    return metaValue.hasSetter(prop.getChildren());
                }
            } else {
                return false;
            }
        } else {
            return metaClass.hasSetter(name);
        }
    }

    @Override
    public boolean hasGetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            if (metaClass.hasGetter(prop.getIndexedName())) {
                MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
                if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                    return metaClass.hasGetter(name);
                } else {
                    return metaValue.hasGetter(prop.getChildren());
                }
            } else {
                return false;
            }
        } else {
            return metaClass.hasGetter(name);
        }
    }

    @Override
    public MetaObject instantiatePropertyValue(String name, PropertyTokenizer propertyTokenizer, ObjectFactory objectFactory) {
        MetaObject metaValue;
        Class<?> type = getSetterType(propertyTokenizer.getName());
        try {
            Object newObject = objectFactory.create(type);
            metaValue = MetaObject.forObject(newObject, metaObject.getObjectFactory(), metaObject.getWrapperFactory());
            set(propertyTokenizer, newObject);
        } catch (Exception e) {
            throw new ReflectionException("Cannot set value of property '" + name + "' because '" + name + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
        }
        return metaValue;
    }

    private Object getBeanProperty(PropertyTokenizer prop, Object object) {
        try {
            //得到getter方法，然后调用
            Invoker method = metaClass.getGetInvoker(prop.getName());
            try {
                return method.invoke(object, NO_ARGUMENTS);
            } catch (Throwable t) {
                throw ExceptionUtil.unwrapThrowable(t);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new ReflectionException("Could not get property '" + prop.getName() + "' from " + object.getClass() + ".  Cause: " + t.toString(), t);
        }
    }

    private void setBeanProperty(PropertyTokenizer prop, Object object, Object value) {
        try {
            //得到setter方法，然后调用
            Invoker method = metaClass.getSetInvoker(prop.getName());
            Object[] params = {value};
            try {
                method.invoke(object, params);
            } catch (Throwable t) {
                throw ExceptionUtil.unwrapThrowable(t);
            }
        } catch (Throwable t) {
            throw new ReflectionException("Could not set property '" + prop.getName() + "' of '" + object.getClass() + "' with value '" + value + "' Cause: " + t.toString(), t);
        }
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public void add(Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E> void addAll(List<E> list) {
        throw new UnsupportedOperationException();
    }

}
