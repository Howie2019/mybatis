package org.apache.ibatis.submitted.custom_collection_handling;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.Wrapper;

import java.util.List;

public class CustomWrapper implements Wrapper {

    private CustomCollection collection;

    public CustomWrapper(CustomCollection collection) {
        this.collection = collection;
    }

    @Override
    public Object get(PropertyTokenizer propertyTokenizer) {
        return null;
    }

    @Override
    public void set(PropertyTokenizer propertyTokenizer, Object value) {

    }

    @Override
    public String findProperty(String name, boolean useCamelCaseMapping) {
        return null;
    }

    @Override
    public String[] getGetterNames() {
        return new String[0];
    }

    @Override
    public String[] getSetterNames() {
        return new String[0];
    }

    @Override
    public Class<?> getSetterType(String name) {
        return null;
    }

    @Override
    public Class<?> getGetterType(String name) {
        return null;
    }

    @Override
    public boolean hasSetter(String name) {
        return false;
    }

    @Override
    public boolean hasGetter(String name) {
        return false;
    }

    @Override
    public MetaObject instantiatePropertyValue(String name, PropertyTokenizer propertyTokenizer, ObjectFactory objectFactory) {
        return null;
    }

    public boolean isCollection() {
        return true;
    }

    public void add(Object element) {
        ((CustomCollection<Object>) collection).add(element);
    }

    public <E> void addAll(List<E> element) {
        ((CustomCollection<Object>) collection).addAll(element);
    }

}
