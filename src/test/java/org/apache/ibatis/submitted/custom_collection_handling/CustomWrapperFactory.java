package org.apache.ibatis.submitted.custom_collection_handling;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.Wrapper;
import org.apache.ibatis.reflection.wrapper.WrapperFactory;

public class CustomWrapperFactory implements WrapperFactory {

    public boolean hasWrapperFor(Object object) {
        return object.getClass().equals(CustomCollection.class);
    }

    public Wrapper getWrapperFor(MetaObject metaObject, Object object) {
        return new CustomWrapper((CustomCollection) object);
    }

}
