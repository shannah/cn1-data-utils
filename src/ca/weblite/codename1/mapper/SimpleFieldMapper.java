/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.mapper;

import com.codename1.processing.Result;
import java.util.Map;

/**
 *
 * @author shannah
 */
public class SimpleFieldMapper implements FieldMapper {
    
    private String path;
    private boolean useResult = false;
    
    public SimpleFieldMapper(String path){
        this.path = path;
        if ( path.indexOf('/') >= 0 || path.indexOf('[') >= 0){
            useResult = true;
        }
    }
    
    public Object getValue(Map map, String fieldName) {
        if ( useResult ){
            return Result.fromContent(map).get(path);
        } else {
            return map.get(path);
        }
    }

    public void putValue(Map map, String fieldName, Object value) {
        map.put(path, value);
    }

    public boolean valueExists(Map map, String fieldName) {
        if ( useResult ){
            return Result.fromContent(map).get(path) != null;
        } else {
            return map.containsKey(path);
        }
    }
    
}
