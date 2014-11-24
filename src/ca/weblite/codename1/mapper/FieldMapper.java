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
public interface FieldMapper {
    public Object getValue(Map map, String fieldName);
    public void putValue(Map map, String fieldName, Object value);
    public boolean valueExists(Map map, String fieldName);
}
