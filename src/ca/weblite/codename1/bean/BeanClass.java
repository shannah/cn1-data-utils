/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author shannah
 */
public class BeanClass {
    
    public static interface Property {
        public String getName();
        public Class getType();
        public Object get(Object bean);
        public void set(Object bean, Object value);
        public boolean isReadable();
        public boolean isWritable();
    }
    
    
    private static Map<Class,BeanClass> beanClasses = new HashMap<Class,BeanClass>();
    
    public BeanClass(){
        
    }
    
    public static void register(Class cls, BeanClass bc){
        beanClasses.put(cls, bc);
    }
    
    public static BeanObject wrap(Object bean){
        BeanClass bc = beanClasses.get(bean.getClass());
        if ( bc == null ){
            throw new RuntimeException("There is no registered bean class for "+bean.getClass());
        }
        return bc.createObject(bean);
    }
    
    Map<String,Property> properties = new HashMap<String,Property>();
    public Object get(Object bean, String property){
        return properties.get(property).get(bean);
    }

    public void set(Object bean, String key, Object value){
        properties.get(key).set(bean, value);
    }
    
    public Map<String,Property> getProperties(){
        return Collections.unmodifiableMap(properties);
    }
    
    public BeanObject createObject(Object bean){
        return new BeanObject(this, bean);
    }
    
    protected void addProperty(Property prop){
        properties.put(prop.getName(), prop);
    }
}
