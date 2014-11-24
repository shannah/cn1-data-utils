/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.mapper;

/**
 *
 * @author shannah
 */
public interface ObjectFactory {
    public <T> T createObject(Class<T> klass);
}
