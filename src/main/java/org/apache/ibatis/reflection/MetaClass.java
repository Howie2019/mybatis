/*
 *    Copyright 2009-2011 the original author or authors.
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

import org.apache.ibatis.reflection.invoker.GetFieldInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * MetaClass实际上是对Reflector和ProeprtyTokenizer的一种结合，是我们可以用复杂的属性表达式来获取类型的描述。
 * <p>
 * 类是用来描述对象的, 而MetaClass是用来描述某个类或者某个属性的. <br/>
 * 换言之MetaClass就是Class的元信息<br/>
 * 此类用于反射<br/>
 * 持有一个Reflector, 方法基本都是再次委派给这个Reflector
 */
public class MetaClass {
    /**
     * 方法基本都是再次委派给这个Reflector
     */
    private Reflector reflector;

    private MetaClass(Class<?> type) {
        this.reflector = Reflector.forClass(type);
    }

    /**
     * @return 这个类对应的MetaClass
     * @see MetaClass#metaClassForProperty(java.lang.String)
     */
    public static MetaClass forClass(Class<?> type) {
        return new MetaClass(type);
    }

    public static boolean isClassCacheEnabled() {
        return Reflector.isClassCacheEnabled();
    }

    public static void setClassCacheEnabled(boolean classCacheEnabled) {
        Reflector.setClassCacheEnabled(classCacheEnabled);
    }

    /**
     * 得到这个属性对应的MetaClass
     *
     * @param propertyName 属性名
     * @return 这个属性对应的MetaClass
     */
    public MetaClass metaClassForProperty(String propertyName) {
        Class<?> propType = reflector.getGetterType(propertyName);
        return MetaClass.forClass(propType);
    }

    /**
     * 传进来的name可能是大小写错乱的, 将其转换为正确名字
     */
    public String findProperty(String propertyName) {
        StringBuilder stringBuilder = buildProperty(propertyName, new StringBuilder());
        return stringBuilder.length() > 0 ? stringBuilder.toString() : null;
    }

    /**
     * 传进来的name可能是大小写错乱的, 将其转换为正确名字
     */
    public String findProperty(String propertyName, boolean useCamelCaseMapping) {
        if (useCamelCaseMapping) {
            //去除"_", app_belong->appbelong
            propertyName = propertyName.replace("_", "");
        }
        return findProperty(propertyName);
    }

    public String[] getGetterNames() {
        return reflector.getGetablePropertyNames();
    }

    public String[] getSetterNames() {
        return reflector.getSetablePropertyNames();
    }

    public Class<?> getSetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaClass metaProp = metaClassForProperty(prop.getName());
            return metaProp.getSetterType(prop.getChildren());
        } else {
            return reflector.getSetterType(prop.getName());
        }
    }

    public Class<?> getGetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaClass metaProp = metaClassForProperty(prop);
            return metaProp.getGetterType(prop.getChildren());
        }
        // issue #506. Resolve the type inside a Collection Object
        return getGetterType(prop);
    }

    private MetaClass metaClassForProperty(PropertyTokenizer prop) {
        Class<?> propType = getGetterType(prop);
        return MetaClass.forClass(propType);
    }

    private Class<?> getGetterType(PropertyTokenizer prop) {
        Class<?> type = reflector.getGetterType(prop.getName());
        if (prop.getIndex() != null && Collection.class.isAssignableFrom(type)) {
            Type returnType = getGenericGetterType(prop.getName());
            if (returnType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnType = actualTypeArguments[0];
                    if (returnType instanceof Class) {
                        type = (Class<?>) returnType;
                    } else if (returnType instanceof ParameterizedType) {
                        type = (Class<?>) ((ParameterizedType) returnType).getRawType();
                    }
                }
            }
        }
        return type;
    }

    private Type getGenericGetterType(String propertyName) {
        try {
            Invoker invoker = reflector.getGetInvoker(propertyName);
            if (invoker instanceof MethodInvoker) {
                Field _method = MethodInvoker.class.getDeclaredField("method");
                _method.setAccessible(true);
                Method method = (Method) _method.get(invoker);
                return method.getGenericReturnType();
            } else if (invoker instanceof GetFieldInvoker) {
                Field _field = GetFieldInvoker.class.getDeclaredField("field");
                _field.setAccessible(true);
                Field field = (Field) _field.get(invoker);
                return field.getGenericType();
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    /**
     * @param propertyName 属性名
     * @return 此属性是否有setter
     */
    public boolean hasSetter(String propertyName) {
        PropertyTokenizer propertyTokenizer = new PropertyTokenizer(propertyName);
        if (propertyTokenizer.hasNext()) {
            if (reflector.hasSetter(propertyTokenizer.getName())) {
                MetaClass metaProp = metaClassForProperty(propertyTokenizer.getName());
                return metaProp.hasSetter(propertyTokenizer.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasSetter(propertyTokenizer.getName());
        }
    }

    public boolean hasGetter(String propertyName) {
        PropertyTokenizer prop = new PropertyTokenizer(propertyName);
        if (prop.hasNext()) {
            if (reflector.hasGetter(prop.getName())) {
                MetaClass metaProp = metaClassForProperty(prop);
                return metaProp.hasGetter(prop.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasGetter(prop.getName());
        }
    }

    public Invoker getGetInvoker(String name) {
        return reflector.getGetInvoker(name);
    }

    public Invoker getSetInvoker(String name) {
        return reflector.getSetInvoker(name);
    }

    /**
     * 传进来的name可能是大小写错乱的, 将其转换为正确名字
     *
     * @param name    属性全名(person.birthdate.year就是一个全名), 这个字符串是大小写无关的, 因为这个方法就是解决大小写问题
     * @param builder 被用来存属性
     * @return 返回传入的builder
     */
    private StringBuilder buildProperty(String name, StringBuilder builder) {
        //先根据属性全名得到一个属性标记器, propertyTokenizer指向第一个属性
        PropertyTokenizer propertyTokenizer = new PropertyTokenizer(name);
        if (propertyTokenizer.hasNext()) {
            //propertyTokenizer.getName()可能是大小写错乱的
            //propertyName为实际属性名
            String propertyName = reflector.findPropertyName(propertyTokenizer.getName());
            if (propertyName != null) {
                builder.append(propertyName);
                builder.append(".");
                MetaClass metaProp = metaClassForProperty(propertyName);
                metaProp.buildProperty(propertyTokenizer.getChildren(), builder);
            }
        } else {
            String propertyName = reflector.findPropertyName(name);
            if (propertyName != null) {
                builder.append(propertyName);
            }
        }
        return builder;
    }

    public boolean hasDefaultConstructor() {
        return reflector.hasDefaultConstructor();
    }

}
