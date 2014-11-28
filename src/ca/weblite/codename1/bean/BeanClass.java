/*
 Copyright 2014 Steve Hannah

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
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
public abstract class BeanClass {
    
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
        return wrap(bean, bean.getClass());
    }
    
    public static BeanObject wrap(Object bean, Class cls){
        BeanClass bc = beanClasses.get(cls);
        if ( bc == null ){
            throw new RuntimeException("There is no registered bean class for "+cls);
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
    
    private BeanObject createObject(Object bean){
        return new BeanObject(this, bean);
    }
    
    protected void addProperty(Property prop){
        properties.put(prop.getName(), prop);
    }
    
    public abstract Class getWrappedClass();
}
