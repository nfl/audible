package com.nfl.util.mapper.service;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by chi.kim on 7/21/15.
 */
class TypeSafeCopy {

    @SuppressWarnings("unchecked")
    public void copyProperties(Object dest, Object orig) throws IllegalAccessException, InvocationTargetException {
        PropertyDescriptor[] origDescriptors =
                PropertyUtils.getPropertyDescriptors(orig);
        for (PropertyDescriptor origDescriptor : origDescriptors) {
            String name = origDescriptor.getName();
            if ("class".equals(name)) {
                continue; // No point in trying to set an object's class
            }
            if (PropertyUtils.isReadable(orig, name) &&
                    PropertyUtils.isWriteable(dest, name)) {


                try {
                    Class origPropClass = PropertyUtils.getPropertyType(orig, name);
                    Class destPropClass = PropertyUtils.getPropertyType(dest, name);

                    if (destPropClass.isAssignableFrom(origPropClass)) {
                        Object value =
                                PropertyUtils.getSimpleProperty(orig, name);
                        BeanUtils.copyProperty(dest, name, value);
                    }
                } catch (NoSuchMethodException e) {
                    // Should not happen
                }
            }
        }
    }
}
