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

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.util.List;

/**
 * 不管Bean, Map 还是 Collection, 只要包装一下, 就能把他们统一对待(利用了多态)
 *
 * @see BaseWrapper
 */

public interface Wrapper {//本接口原名ObjectWrapper, 改为Wrapper更易理解

    //--------以下方法用于BeanWrapper和MapWrapper--------

    /** 取得PropertyTokenizer所指属性的值 */
    Object get(PropertyTokenizer propertyTokenizer);

    //set
    void set(PropertyTokenizer propertyTokenizer, Object value);

    //查找属性
    String findProperty(String name, boolean useCamelCaseMapping);

    //取得getter的名字列表
    String[] getGetterNames();

    //取得setter的名字列表
    String[] getSetterNames();

    /**
     * 假设传入
     */
    //取得setter的类型
    Class<?> getSetterType(String name);

    /**
     * 传入"person[0].birthdate.year", 将返回int
     *
     * @param name 属性名(链式表示)
     */
    //取得getter的类型
    Class<?> getGetterType(String name);

    //是否有指定的setter
    boolean hasSetter(String name);

    //是否有指定的getter
    boolean hasGetter(String name);

    /**
     * 这个方法用于BeanWrapper和MapWrapper, 而在CollectionWrapper上调用会抛出异常
     * <p>
     * 实例化属性值
     *
     * @param name              属性名. 感觉这个参数没啥用, 因为根据propertyTokenizer可以得到name
     * @param propertyTokenizer 属性标记器
     * @param objectFactory     用哪个ObjectFactory实例化这个PropertyValue
     */
    MetaObject instantiatePropertyValue(String name, PropertyTokenizer propertyTokenizer, ObjectFactory objectFactory);

    //--------以上方法用于BeanWrapper和MapWrapper--------

    //是否是集合
    boolean isCollection();

    /**
     * 被包装对象为Collection时, 会用到此方法
     * <p>
     * 向集合中增加元素
     */
    public void add(Object element);

    /**
     * 被包装对象为Collection时, 会用到此方法
     * <p>
     * 向集合中增加元素
     */
    public <E> void addAll(List<E> element);

}
