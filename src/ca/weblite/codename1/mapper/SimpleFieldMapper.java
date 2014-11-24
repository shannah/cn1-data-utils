/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.mapper;

import java.util.Map;

/**
 *
 * @author shannah
 */
public class SimpleFieldMapper implements FieldMapper {
    
    private String path;
    
    public SimpleFieldMapper(String path){
        this.path = path;
    }
    
    public Object getValue(Map map, String fieldName) {
        return map.get(path);
    }

    public void putValue(Map map, String fieldName, Object value) {
        map.put(path, value);
    }

    public boolean valueExists(Map map, String fieldName) {
        return map.containsKey(path);
    }
    
}
