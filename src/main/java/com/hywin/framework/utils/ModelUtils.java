package com.hywin.framework.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hywin.framework.entity.BaseEntity;
import com.hywin.framework.mybatis.ColumnTarget;
import com.hywin.framework.mybatis.Property;

/**
 * Created by wuyouyang on 2017/4/27.
 */
public class ModelUtils
{
    public static Map<String, Property> getProperties(BaseEntity object, ColumnTarget columnTarget) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        Class modelClass = object.getClass();
        Map<String, Property> properties = getProperties(modelClass, columnTarget);
        Map results = new HashMap(properties.size());
        for (Entry<String, Property> propertyEntry : properties.entrySet()) {
            Property property = propertyEntry.getValue();
            if (((columnTarget == ColumnTarget.INSERT) || (columnTarget == ColumnTarget.UPDATE) || (columnTarget == ColumnTarget.WHERE)) &&
                    (property.isNullValue(object)))
            {
                continue;
            }

            results.put(propertyEntry.getKey(), property);
        }

        return results;
    }

    public static Map<String, Property> getProperties(Class<?> modelClass, ColumnTarget columnTarget)
    {
        PropertyDescriptor[] propDescriptors = ReflectUtils.getPropertyDescriptors(modelClass);
        Map properties = new HashMap(propDescriptors.length);
        for (PropertyDescriptor propertyDescriptor : propDescriptors) {
            Property property = new Property(modelClass, propertyDescriptor);
            if (property.isTransient()) {
                continue;
            }
            if (property.isUnableForColumnTarget(columnTarget)) {
                continue;
            }
            if (((columnTarget == ColumnTarget.INSERT) || (columnTarget == ColumnTarget.UPDATE) || (columnTarget == ColumnTarget.WHERE)) &&
                    (property.isId()))
            {
                continue;
            }
            if ((columnTarget == ColumnTarget.ORDER) &&
                    (!property.isOrderColumn()))
            {
                continue;
            }

            properties.put(property.getName(), property);
        }
        return properties;
    }
}
