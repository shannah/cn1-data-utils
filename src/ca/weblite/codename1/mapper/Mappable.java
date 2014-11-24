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
