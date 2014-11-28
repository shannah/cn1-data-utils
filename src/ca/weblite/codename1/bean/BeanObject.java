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

import ca.weblite.codename1.bean.BeanClass.Property;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wraps an object to provide reflection-like capabilities, and tread the wrapped
 * object as a regular Map.  It depends on a BeanClass which should have been 
 * implemented with the appropriate property descriptors for the wrapped object.
 * @author shannah
 */
public class BeanObject implements Map<String,Object> {

    
    /**
     * The bean class that contains information about the properties in the bean.
     */
    private final BeanClass klass;
    
    /**
     * An object that is wrapped by this bean.
     */
    private final Object bean;
    
    /**
     * An overflow map to store values that are set which don't correspond with
     * a property in the wrapped bean.
     */
    private HashMap<String,Object> overflow;
    
    
    /**
     * Creates a new BeanObject for the specified bean class, to wrap the specified
     * object.
     * @param klass The BeanClass that serves as the prototype.
     * @param bean The object to wrap.
     */
    public BeanObject(BeanClass klass, Object bean){
        this.klass = klass;
        this.bean = bean;
    }
    
    /**
     * Returns the BeanClass for this object.
     * @return 
     */
    public BeanClass getBeanClass(){
        return klass;
    }
    
    /**
     * Returns the wrapped object.
     * @return 
     */
    public Object unwrap(){
        return bean;
    }
    
    /**
     * Returns the sum of the number of properties in the wrapped object and
     * the number of entries in the overflow map.
     * @return 
     */
    public int size() {
        return klass.properties.size() + (overflow != null ? overflow.size():0);
    }

    /**
     * Returns true if the wrapped object contains no properties and the overflow
     * map is empty.
     * @return 
     */
    public boolean isEmpty() {
        return klass.properties.isEmpty() && (overflow == null || overflow.isEmpty());
    }

    
    /**
     * Checks to see if this map contains the specified key.  This will return true
     * if the wrapped object includes a property with the name of the key, or if 
     * the internal overflow map contains a key.
     * @param key
     * @return 
     */
    public boolean containsKey(Object key) {
        return overflow != null && overflow.containsKey(key) || klass.properties.containsKey(key);
    }

    /**
     * Checks to see if the specified value is contained in any of the properties
     * of the wrapped object.
     * @param value
     * @return True if the value is contained in the wrapped object, or in the 
     * overflow map.
     */
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

    /**
     * Gets the value of a specified property.
     * @param key The name of the property to get.
     * @return The value of the specified property.
     */
    public Object get(Object key) {
        if ( klass.properties.containsKey((String)key)) {
            return klass.properties.get(key).get(bean);
        } else if ( overflow != null ){
            return overflow.get(key);
        } else {
            return null;
        }
    }

    /**
     * Sets a value in the bean object.  If this key corresponds to a property
     * in the wrapped object, then the object's property will be updated directly.
     * If the key corresponds to a property that is not writable, then this will
     * throw a runtime exception.  If the key doesn't correspond to any property,
     * it will be stored in the internal overflow map.
     * @param key The name of the property to set.
     * @param value The value to set.
     * @return The old value
     */
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

    /**
     * Removes the specified key from the object.  Effectively, this will just
     * set the value to null, if it is a property in the wrapped object. If the 
     * property is not in the wrapped object, but instead is stored in the internal
     * overflow map, then that entry will be removed.
     * @param key The key to remove
     * @return The removed object.
     */
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

    /**
     * Puts all properties from the provided map into the current object.  This
     * includes properties that may not be writable so be careful. If you try
     * to write a property that is not writable, a RuntimeException will be thrown.
     * @param m 
     * @see #putAllWritable
     */
    public void putAll(Map m) {
        for ( Object o : m.entrySet()){
            Map.Entry e = (Map.Entry)o;
            put((String)e.getKey(), e.getValue());
        }
    }
    
    /**
     * Puts all properties of the given BeanObject into the current BeanObject.
     * Only those properties that are readable in the source, and writable in the 
     * target will be copied.
     * @param m 
     */
    public void putAll(BeanObject m) {
        for ( Object o : m.readableEntries()){
            Map.Entry e = (Map.Entry)o;
            if ( isWritable((String)e.getKey())){
                put((String)e.getKey(), e.getValue());
            }
            
        }
    }
    
    /**
     * Checks if the specified property is readable.
     * @param key THe name of the property
     * @return True if the property is readable.
     */
    public boolean isReadable(String key){
        return klass.properties.containsKey(key) && klass.properties.get(key).isReadable();
    }
    
    
    /**
     * Checks if the specified property is writable.
     * @param key The name of the property.
     * @return True if the property is writable.
     */
    public boolean isWritable(String key){
        return klass.properties.containsKey(key) && klass.properties.get(key).isWritable();
    }
    
    /**
     * Returns the type of the specified property.
     * @param key The name of the property.
     * @return The type of the property as defined in the class.
     */
    public Class getType(String key){
        return klass.properties.get(key).getType();
    }
    
    
    /**
     * Sets all properties in the wrapped object which are writable using the 
     * values provided in m.
     * @param m A map with keys/values to set in the wrapped object.
     */
    public void putAllWritable(Map m){
        Set<String> writableKeys = writableKeys();
        for ( Object o : m.entrySet()){
            Map.Entry e = (Map.Entry)o;
            if ( writableKeys.contains((String)e.getKey())){
                put((String)e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Sets all writable properties to null (or translated primitive value), and clears
     * the overflow map.
     */
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

    /**
     * Returns the names of all properties in the wrapped object, and the existing
     * keys in the overflow map.
     * @return 
     */
    public Set<String> keySet() {
        Set<String> out = new HashSet<String>();
        out.addAll(klass.properties.keySet());
        if ( overflow != null ){
            out.addAll(overflow.keySet());
        }
        return out;
    }

    /**
     * Returns all of the non-null values of properties in this object - and
     * the values in the overflow map.
     * @return 
     */
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

    /**
     * Returns all entries in the wrapped object, and in the overflow map.
     * @return 
     */
    public Set<Map.Entry<String,Object>> entrySet() {
        Set out = new HashSet();
        for (String key : keySet() ){
            out.add(new Entry(key));
        }
        return out;
    }

    
    /**
     * Returns all entries for readable properties in the wrapped object.  Does
     * not include any entries from the overflow map.
     * @return 
     */
    public Set<Map.Entry<String,Object>> readableEntries(){
        Set out = new HashSet();
        for (Property p : getProperties().values()){
            if ( p.isReadable()){
                out.add(new Entry(p.getName()));
            }
        }
        return out;
    }
    
    /**
     * Returns all entries for writable properties in the wrapped object.  Does
     * not include any entries from the overflow map.
     * @return 
     */
    public Set<Map.Entry<String,Object>> writableEntries(){
        Set out = new HashSet();
        for (Property p : getProperties().values()){
            if ( p.isWritable()){
                out.add(new Entry(p.getName()));
            }
        }
        return out;
    }
    
    
    /**
     * Returns the names of all properties in the wrapped object that are readable.
     * Does not include any keys from the overflow map.
     * @return 
     */
    public Set<String> readableKeys(){
        Set out = new HashSet<String>();
        for (Property p : getProperties().values()){
            if ( p.isReadable()){
                out.add(p.getName());
            }
        }
        return out;
    }
    
    /**
     * Returns the names of all properties in the wrapped object that are writable.
     * Does not include any keys from the overflow map.
     * @return 
     */
    public Set<String> writableKeys(){
        Set out = new HashSet<String>();
        for (Property p : getProperties().values()){
            if ( p.isWritable()){
                out.add(p.getName());
            }
        }
        return out;
    }
    
    /**
     * Returns values of all non-null readable properties in the wrapped object. Does
     * not include any values from the overflow map.
     * @return 
     */
    public Collection readableValues(){
        Collection out = new ArrayList();
        for (Property p : getProperties().values()){
            if ( p.isReadable()){
                Object val = get(p.getName());
                if ( val != null ){
                    out.add(val);
                }
            }
        }
        return out;
    }
    /**
     * Returns values of all non-null writable properties in the wrapped object. Does
     * not include any values from the overflow map.
     * @return 
     */
    public Collection writableValues(){
        Collection out = new ArrayList();
        for (Property p : getProperties().values()){
            // This only makes sense for values that are both readable and writable
            if ( p.isReadable() && p.isWritable()){
                Object val = get(p.getName());
                if ( val != null ){
                    out.add(val);
                }
            }
        }
        return out;
    }

    /**
     * Returns the properties defined by this schema.
     * @return 
     */
    public Map<String,Property> getProperties(){
        return klass.getProperties();
    }
    
    
    /**
     * Returns all readable properties defined by this schema.
     * @return 
     */
    public Map<String,Property> getReadableProperties(){
        Map<String,Property> out = new HashMap<String,Property>();
        Map<String,Property> props = getProperties();
        for ( String key : props.keySet()){
            Property p = props.get(key);
            if ( p.isReadable()){
                out.put(key, p);
            }
        }
        return out;
    }
    
    /**
     * Returns all writable properties defined by this schema.
     * @return 
     */
    public Map<String,Property> getWritableProperties(){
        Map<String,Property> out = new HashMap<String,Property>();
        Map<String,Property> props = getProperties();
        for ( String key : props.keySet()){
            Property p = props.get(key);
            if ( p.isWritable()){
                out.put(key, p);
            }
        }
        return out;
    }
    
    /**
     * Encapsulates an entry as returned from various entry methods.
     */
    private class Entry implements Map.Entry<String,Object>{
        
        String key;
        
        Entry(String key){
            this.key = key;
        }
        
        public String getKey() {
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
