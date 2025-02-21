/*
 *    Copyright 2009-2013 the original author or authors.
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
package org.apache.ibatis.type;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.io.Resources;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.*;

/**
 * parameterType和resultType会用到一些javaBean, 不设置别名就必须写javaBean的全路径
 * 本质就是一个Map, 而解析别名本质就是从Map里面取对应的Class对象
 * <p>
 * mybatis自己定义了一系列别名.
 */
public class TypeAliasRegistry {

    private final Map<String, Class<?>> TYPE_ALIASES = new HashMap<String, Class<?>>();

    public TypeAliasRegistry() {
        //构造函数里注册系统内置的类型别名
        registerAlias("string", String.class);

        //mybatis内置的别名
        registerAlias("byte", Byte.class);
        registerAlias("long", Long.class);
        registerAlias("short", Short.class);
        registerAlias("int", Integer.class);
        registerAlias("integer", Integer.class);
        registerAlias("double", Double.class);
        registerAlias("float", Float.class);
        registerAlias("boolean", Boolean.class);

        //基本数组包装类型
        registerAlias("byte[]", Byte[].class);
        registerAlias("long[]", Long[].class);
        registerAlias("short[]", Short[].class);
        registerAlias("int[]", Integer[].class);
        registerAlias("integer[]", Integer[].class);
        registerAlias("double[]", Double[].class);
        registerAlias("float[]", Float[].class);
        registerAlias("boolean[]", Boolean[].class);

        //加个下划线，就变成了基本类型
        registerAlias("_byte", byte.class);
        registerAlias("_long", long.class);
        registerAlias("_short", short.class);
        registerAlias("_int", int.class);
        registerAlias("_integer", int.class);
        registerAlias("_double", double.class);
        registerAlias("_float", float.class);
        registerAlias("_boolean", boolean.class);

        //加个下划线，就变成了基本数组类型
        registerAlias("_byte[]", byte[].class);
        registerAlias("_long[]", long[].class);
        registerAlias("_short[]", short[].class);
        registerAlias("_int[]", int[].class);
        registerAlias("_integer[]", int[].class);
        registerAlias("_double[]", double[].class);
        registerAlias("_float[]", float[].class);
        registerAlias("_boolean[]", boolean[].class);

        //日期数字型
        registerAlias("date", Date.class);
        registerAlias("decimal", BigDecimal.class);
        registerAlias("bigdecimal", BigDecimal.class);
        registerAlias("biginteger", BigInteger.class);
        registerAlias("object", Object.class);

        registerAlias("date[]", Date[].class);
        registerAlias("decimal[]", BigDecimal[].class);
        registerAlias("bigdecimal[]", BigDecimal[].class);
        registerAlias("biginteger[]", BigInteger[].class);
        registerAlias("object[]", Object[].class);

        //集合型
        registerAlias("map", Map.class);
        registerAlias("hashmap", HashMap.class);
        registerAlias("list", List.class);
        registerAlias("arraylist", ArrayList.class);
        registerAlias("collection", Collection.class);
        registerAlias("iterator", Iterator.class);

        //还有个ResultSet型
        registerAlias("ResultSet", ResultSet.class);
    }

    /**
     * 从TYPE_ALIASES这个Map里面取对应的Class对象
     *
     * @param string 别名
     * @param <T>    这是一个泛型方法
     * @return 别名对应的Class对象
     */
    @SuppressWarnings("unchecked")
    // throws class cast exception as well if types cannot be assigned

    public <T> Class<T> resolveAlias(String string) {
        try {
            if (string == null) {
                return null;
            }
            // issue #748
            //先转成小写再解析
            //这里转个小写也有bug？见748号bug(在google code上)
            //https://code.google.com/p/mybatis/issues
            //比如如果本地语言是Turkish，那i转成大写就不是I了，而是另外一个字符（İ）。这样土耳其的机器就用不了mybatis了！这是一个很大的bug，但是基本上每个人都会犯......
            String key = string.toLowerCase(Locale.ENGLISH);
            Class<T> value;
            //原理就很简单了，从HashMap里找对应的键值，找到则返回类型别名对应的Class
            if (TYPE_ALIASES.containsKey(key)) {
                value = (Class<T>) TYPE_ALIASES.get(key);
            } else {
                //找不到，再试着将String直接转成Class(这样怪不得我们也可以直接用java.lang.Integer的方式定义，也可以就int这么定义)
                value = (Class<T>) Resources.classForName(string);
            }
            return value;
        } catch (ClassNotFoundException e) {
            throw new TypeException("Could not resolve type alias '" + string + "'.  Cause: " + e, e);
        }
    }

    public void registerAliases(String packageName) {
        registerAliases(packageName, Object.class);
    }

    //扫描并注册包下所有继承于superType的类型别名
    public void registerAliases(String packageName, Class<?> superType) {
        //TODO ResolverUtil
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();
        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
        Set<Class<? extends Class<?>>> typeSet = resolverUtil.getClasses();
        for (Class<?> type : typeSet) {
            // Ignore inner classes and interfaces (including package-info.java)
            // Skip also inner classes. See issue #6
            if (!type.isAnonymousClass() && !type.isInterface() && !type.isMemberClass()) {
                registerAlias(type);
            }
        }
    }

    //注册类型别名
    public void registerAlias(Class<?> type) {
        //如果没有类型别名，用Class.getSimpleName来注册
        String alias = type.getSimpleName();
        //或者通过Alias注解来注册(Class.getAnnotation)
        Alias aliasAnnotation = type.getAnnotation(Alias.class);
        if (aliasAnnotation != null) {
            alias = aliasAnnotation.value();
        }
        registerAlias(alias, type);
    }

    //注册类型别名
    public void registerAlias(String alias, Class<?> value) {
        if (alias == null) {
            throw new TypeException("The parameter alias cannot be null");
        }
        // issue #748
        String key = alias.toLowerCase(Locale.ENGLISH);
        //如果已经存在key了，且value和之前不一致，报错
        //这里逻辑略显复杂，感觉没必要，一个key对一个value呗，存在key直接报错不就得了
        if (TYPE_ALIASES.containsKey(key) && TYPE_ALIASES.get(key) != null && !TYPE_ALIASES.get(key).equals(value)) {
            throw new TypeException("The alias '" + alias + "' is already mapped to the value '" + TYPE_ALIASES.get(key).getName() + "'.");
        }
        TYPE_ALIASES.put(key, value);
    }

    public void registerAlias(String alias, String value) {
        try {
            registerAlias(alias, Resources.classForName(value));
        } catch (ClassNotFoundException e) {
            throw new TypeException("Error registering type alias " + alias + " for " + value + ". Cause: " + e, e);
        }
    }

    /**
     * @since 3.2.2
     */
    public Map<String, Class<?>> getTypeAliases() {
        return Collections.unmodifiableMap(TYPE_ALIASES);
    }

}
