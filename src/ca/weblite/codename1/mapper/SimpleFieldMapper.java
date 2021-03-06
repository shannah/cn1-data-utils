/*
 *  Copyright 2014 Steve Hannah
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
