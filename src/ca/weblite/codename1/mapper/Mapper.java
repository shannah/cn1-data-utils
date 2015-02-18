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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author shannah
 */
public class Mapper {
    public <T extends Mappable> T readObject(Map map, Class<T> klass){
        try {
            T obj = (T)klass.newInstance();
            if ( obj instanceof Mappable ){
                Mappable m = (Mappable)obj;
                m.readMap(map, this);
            }
            return obj;
        } catch (Throwable t){
            throw new RuntimeException(t.getMessage());
        }
    }
    
    public <T extends Mappable> List<T> readObjects(Collection<Map> maps, Class<T> klass){
        ArrayList<T> out = new ArrayList<T>();
        for ( Map m : maps ){
            out.add(readObject(m, klass));
        }
        return out;
    }
    
    public void writeObject(Mappable o, Map output){
        o.writeMap(output, this);
    }
    
    public void writeObjects(Collection<Mappable> o, Collection<Map> output){
        for ( Mappable m : o ){
            HashMap hm = new HashMap();
            writeObject(m, hm);
            output.add(hm);
        }
    }
    
    public int readInt(Map map, String key){
        return NumberUtil.intValue(map.get(key));
    }
    
    public double readDouble(Map map, String key){
        return NumberUtil.doubleValue(map.get(key));
    }
    
    public long readLong(Map map, String key){
        return NumberUtil.longValue(map.get(key));
    }
    
    public String readString(Map map, String key){
        return (String)map.get(key);
    }
    
    public <T extends Mappable> T readMappable(Map map, String key, Class<T> klass){
        return (T)readObject((Map)map.get(key), klass);
    }
    
    
    
    
    
    
}
