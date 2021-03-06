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

import java.util.Map;

/**
 *
 * @author shannah
 */
public interface Mappable {
    /**
     * Reads values from a map
     * @param map 
     */
    public void readMap(Map map, Mapper mapper);
    
    /**
     * Write values to map.
     * @param map 
     */
    public void writeMap(Map map, Mapper mapper);
}
