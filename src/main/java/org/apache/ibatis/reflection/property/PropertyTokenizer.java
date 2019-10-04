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
package org.apache.ibatis.reflection.property;

import java.util.Iterator;

/**
 * PropertyTokenizer中文名: 属性标记器. 类似一个游标<br/>
 * <p>
 * mybatis把嵌套的属性(person.birthdate.year)看做一条链, PropertyTokenizer就是这条链上的游标,通过调用next方法, 可以遍历整条属性链
 * <p>
 * 使用了迭代子模式, 又名游标(Cursor)模式
 * 如person.birthdate.year，将依次取得person, birthdate, year
 * Collection接口就继承了Iterable接口, 所以所有集合都是可迭代的.
 * 这里PropertyTokenizer实现了Iterable, 说明PropertyTokenizer是可迭代的.
 * PropertyTokenizer更进一步, 将一个属性看做一棵树或者一个森林, 游标可以进入到属性的属性里面去, 而不仅仅是在集合中遍历.
 */
//Iterable规定PropertyTokenizer能够返回一个Iterator
//
//Iterator规定PropertyTokenizer能够遍历自己,
// 换言之PropertyTokenizer没有像ArrayList那样用内部类实现Iterator, 而是自己实现了Iterator
public class PropertyTokenizer implements Iterable<PropertyTokenizer>, Iterator<PropertyTokenizer> {
    //例子： person[0].birthdate.year
    /**
     * 相比indexedName, 截去了"[0]"
     */
    private String name; //person
    /**
     * 带下标的name, 比如person[0]
     */
    private String indexedName;//person[0]
    /**
     * 索引部分, 这个地方不一定是数字, 字符串也是可以的(比如:map['name']). 借用了php的设计思想
     */
    private String index;//0
    /** 属性的属性属性 */
    private String children; //birthdate.year

    /**
     * 用属性全名构造一个PropertyTokenizer. 这时候这个PropertyTokenizer指向第一个属性(最外层属性)
     *
     * @param fullname 属性标记器的全限定名. person.birthdate.year就是一个全名
     */

    public PropertyTokenizer(String fullname) {
        //person[0].birthdate.year
        //找到第一个.的下标
        int index = fullname.indexOf('.');
        if (index > -1) {//fullname中存在.
            name = fullname.substring(0, index);
            children = fullname.substring(index + 1);
        } else {
            //找不到.的话，取全部部分
            name = fullname;
            children = null;
        }
        indexedName = name;
        //把中括号里的数字给解析出来
        index = name.indexOf('[');
        if (index > -1) {//name中包含字符[
            //substring方法前包后不包
            this.index = name.substring(index + 1, name.length() - 1);
            name = name.substring(0, index);
        }
    }

    /**
     * @return name字段
     */
    public String getName() {
        return name;
    }

    public String getIndex() {
        return index;
    }

    public String getIndexedName() {
        return indexedName;
    }

    public String getChildren() {
        return children;
    }

    /**
     * @return 是否此属性嵌套有其他属性
     */
    @Override
    public boolean hasNext() {
        return children != null;
    }

    /**
     * 通过儿子来new一个实例.
     * 通过不断调用next, 可以把PropertyTokenizer向后移动
     */
    @Override
    public PropertyTokenizer next() {
        return new PropertyTokenizer(children);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
    }

    /**
     * 我迭代我自己, 哈哈!!<br/>
     * PropertyTokenizer不是用内部类实现Iterator, 而是自己实现了Iterator. 所以iterator()方法返回自己.
     *
     * @return 这个对象自己
     */
    @Override
    public Iterator<PropertyTokenizer> iterator() {
        return this;
    }
}
