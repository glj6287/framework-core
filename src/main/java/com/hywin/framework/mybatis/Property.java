package com.hywin.framework.mybatis;

import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * Created by wuyouyang on 2017/4/27.
 */
public class Property
{
    private String name;
    private String tableName;
    private Method readMethod;
    private Field field;
    private Column column;

    public Property(Class<?> modelClass, PropertyDescriptor propertyDescriptor)
    {
        this.name = propertyDescriptor.getName();
        this.readMethod = propertyDescriptor.getReadMethod();
        try
        {
            this.field = modelClass.getDeclaredField(propertyDescriptor.getName());
        } catch (NoSuchFieldException e1) {
            this.field = null;
        } catch (SecurityException e1) {
            this.field = null;
        }

        if (isTransient()) {
            return;
        }

        this.column = ((Column)getAnnotation(this.readMethod, Column.class));
        if (this.column == null) {
            this.column = ((Column)getAnnotation(this.field, Column.class));
        }

        if (this.column != null) {
            this.tableName = this.column.table();
        }
        if (this.tableName == null)
            if (modelClass.getAnnotation(Table.class) == null) {
                String className = StringUtils.split(modelClass.getName(), "$")[0];
                try {
                    this.tableName = ((Table)Class.forName(className).getAnnotation(Table.class)).name();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                this.tableName = ((Table)modelClass.getAnnotation(Table.class)).name();
            }
    }

    public boolean isId()
    {
        return (hasAnnotation(this.readMethod, Id.class)) || (hasAnnotation(this.field, Id.class));
    }

    public boolean isOrderColumn() {
        return (hasAnnotation(this.readMethod, OrderColumn.class)) || (hasAnnotation(this.field, OrderColumn.class));
    }

    public boolean isTransient() {
        return (hasAnnotation(this.readMethod, Transient.class)) || (hasAnnotation(this.field, Transient.class));
    }

    public String getColumnName() {
        if (this.column == null)
        {
            return this.name.replaceAll("([A-Z])", "_$0").toUpperCase();
        }
        return this.column.name();
    }

    public String getOrder()
    {
        OrderColumn orderColumn = (OrderColumn)getAnnotation(this.readMethod, OrderColumn.class);
        if (orderColumn == null) {
            orderColumn = (OrderColumn)getAnnotation(this.field, OrderColumn.class);
        }
        return getColumnName() + " " + orderColumn.name();
    }

    public boolean isNullValue(Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return this.readMethod.invoke(object, new Object[0]) == null;
    }

    public boolean isUnableForColumnTarget(ColumnTarget columnTarget) {
        if (this.column == null) {
            return false;
        }

        switch (columnTarget.ordinal()) {
            case 1:
                return !this.column.insertable();
            case 2:
                return !this.column.updatable();
        }

        return false;
    }

    private boolean hasAnnotation(AccessibleObject accessibleObject, Class<? extends Annotation> annotationClass) {
        return getAnnotation(accessibleObject, annotationClass) != null;
    }

    private Annotation getAnnotation(AccessibleObject accessibleObject, Class<? extends Annotation> annotationClass) {
        if (accessibleObject == null) {
            return null;
        }
        return accessibleObject.getAnnotation(annotationClass);
    }

    public String getName()
    {
        return this.name;
    }
}
