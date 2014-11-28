/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.bean;

import ca.weblite.codename1.bean.BeanClass.Property;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author shannah
 */
public class BeanObject implements Map<String,Object> {

    private BeanClass klass;
    private Object bean;
    private HashMap<String,Object> overflow;
    
    
    public BeanObject(BeanClass klass, Object bean){
        this.klass = klass;
        this.bean = bean;
    }
    
    public int size() {
        return klass.properties.size() + (overflow != null ? overflow.size():0);
    }

    public boolean isEmpty() {
        return klass.properties.isEmpty() && (overflow == null || overflow.isEmpty());
    }

    public boolean containsKey(Object key) {
        return overflow != null && overflow.containsKey(key) || klass.properties.containsKey(key);
    }

    public boolean containsValue(Object value) {
        if ( value == null ){
            return false;
        }
        for ( BeanClass.Property e : klass.properties.values() ){
            if ( value.equals(e.get(bean))){
                return true;
            }
        }
        return overflow != null && overflow.containsValue(value);
    }

    public Object get(Object key) {
        if ( klass.properties.containsKey((String)key)) {
            return klass.properties.get(key).get(bean);
        } else if ( overflow != null ){
            return overflow.get(key);
        } else {
            return null;
        }
    }

    public Object put(String key, Object value) {
        if ( klass.properties.containsKey(key)){
            if ( klass.properties.get(key).isWritable()){
                Object old = klass.properties.get(key).get(bean);
                klass.properties.get(key).set(bean, value);
                return old;
            } else {
                throw new RuntimeException("Attempt to write property "+key+" in bean but the property is not writable.");
            }
        } else {
            if ( overflow == null ){
                overflow = new HashMap();
                
            }
            Object old = overflow.get(key);
            overflow.put(key, value);
            return old;
        }
    }

    public Object remove(Object key) {
        if (klass.properties.containsKey((String)key)){
            
            Property prop = klass.properties.get((String)key);
            if ( prop.isWritable() ){
                Object old = prop.get(bean);
                prop.set(bean, null);
                return old;
            } else {
                throw new RuntimeException("Failed to remove key "+key+" from bean because that property is not writable.");
            }
        } else {
            if ( overflow == null){
                overflow = new HashMap();
            }
            Object old = overflow.get(key);
            overflow.remove(key);
            return old;
        }
    }

    public void putAll(Map m) {
        for ( Object o : m.entrySet()){
            Map.Entry e = (Map.Entry)o;
            put((String)e.getKey(), e.getValue());
        }
    }

    public void clear() {
        for ( Object o : klass.properties.values() ){
            BeanClass.Property p = (BeanClass.Property)o;
            if ( p.isWritable() ){
                p.set(bean, null);
            }
        }
        if ( overflow != null ){
            overflow.clear();
        }
    }

    public Set<String> keySet() {
        Set<String> out = new HashSet<String>();
        out.addAll(klass.properties.keySet());
        if ( overflow != null ){
            out.addAll(overflow.keySet());
        }
        return out;
    }

    public Collection values() {
        List out = new ArrayList();
        for ( String k : keySet() ){
            Object o = get(k);
            if ( o != null ){
                out.add(o);
            }
        }
        return out;
    }

    public Set entrySet() {
        Set out = new HashSet();
        for (String key : keySet() ){
            out.add(new Entry(key));
        }
        return out;
    }

    

    
    private class Entry implements Map.Entry{
        
        String key;
        
        Entry(String key){
            this.key = key;
        }
        
        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return get(key);
        }

        public Object setValue(Object value) {
            return put(key, value);
        }
        
    }
    
    
}
