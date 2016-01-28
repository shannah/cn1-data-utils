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

import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.NetworkManager;
import com.codename1.l10n.DateFormat;
import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.processing.Result;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shannah
 */
public abstract class DataMapper {
    
    private static Map<String,DataMapper> globalContext;
    
    public static DataMapper getGlobal(String name) {
        if (globalContext != null) {
            return globalContext.get(name);
        }
        return null;
    }
    
    public static void addGlobal(String name, DataMapper mapper) {
        if (globalContext == null) {
            globalContext = new HashMap<String,DataMapper>();
        }
        
        if (!globalContext.containsKey(name)) {
            if (mapper.getContext() == null) {
                mapper.setContext(new HashMap<Class,DataMapper>());
                mapper.getContext().put(mapper.selfClass, mapper);
            }
            globalContext.put(name, mapper);
        } else {
            Map<Class,DataMapper> ctx = globalContext.get(name).getContext();
            ctx.put(mapper.selfClass, mapper);
            mapper.setContext(ctx);
        }
    }
    
    public static void clearGlobal(String name) {
        if (globalContext != null) {
            globalContext.remove(name);
        }
    }

    /**
     * @return the outputDateFormat
     */
    public DateFormat getOutputDateFormat() {
        return outputDateFormat;
    }

    /**
     * @param outputDateFormat the outputDateFormat to set
     */
    public void setOutputDateFormat(DateFormat outputDateFormat) {
        this.outputDateFormat = outputDateFormat;
    }

    public static interface Decorator {
        public void decorate(DataMapper dataMapper);
    }
    
    /**
     * @return the writeKeyConversion
     */
    public KeyConversion getWriteKeyConversion() {
        return writeKeyConversion;
    }

    /**
     * @param writeKeyConversion the writeKeyConversion to set
     */
    public void setWriteKeyConversion(KeyConversion writeKeyConversion) {
        this.writeKeyConversion = writeKeyConversion;
    }

    /**
     * @return the readKeyConversions
     */
    public List<KeyConversion> getReadKeyConversions() {
        return readKeyConversions;
    }

    /**
     * @param readKeyConversions the readKeyConversions to set
     */
    public void setReadKeyConversions(List<KeyConversion> readKeyConversions) {
        this.readKeyConversions = readKeyConversions;
    }
    
    public  static interface KeyConversion {
        public String convertKey(String key);
        
    }
    
    private static class CamelToSnakeConversion implements KeyConversion {

        public String convertKey(String key) {
            int len = key.length();
            char[] out = new char[len*2];
            char[] in = key.toCharArray();
            int i = 0;
            int j = 0;
            while ( i < len ){
                char c = in[i];
                if ( c>='A' && c<='Z'){ // caps
                    out[j++] = '_';
                    out[j++] = (char)(c+32);
                } else {
                    out[j++] = c;
                }
                i++;
            }
            return new String(out, 0, j);    
        }
        
    }
    
    private static class SnakeToCamelConversion implements KeyConversion {

        public String convertKey(String key) {
            int len = key.length();
            char[] out = new char[len];
            char[] in = key.toCharArray();
            int i = 0;
            int j = 0;
            boolean underscore=false;
            while ( i < len ){
                char c = in[i];
                if ( c == '_'){ // caps
                    underscore = true;
                } else {
                    if ( underscore ){
                        underscore = false;
                        if ( c >= 'a' && c <= 'z' ){
                            out[j++] = (char)(c-32);
                        } else {
                            out[j++] = c;
                        }
                    } else {
                        out[j++] = c;
                    }
                }
                i++;
            }
            return new String(out, 0, j);    
        }
        
    }
    
    private static class NoConversion implements KeyConversion {

        public String convertKey(String key) {
            return key;
        }
        
    }
    
    public static KeyConversion CONVERSION_CAMEL_TO_SNAKE = new CamelToSnakeConversion();
    public static KeyConversion CONVERSION_SNAKE_TO_CAMEL = new SnakeToCamelConversion();
    public static KeyConversion CONVERSION_NONE = new NoConversion();
    
    private Map<Class,DataMapper> context;
    private Map<String,FieldMapper> fieldMappers;
    private ObjectFactory factory;
    private List<DateFormat> dateFormats = new ArrayList<DateFormat>();
    private DateFormat outputDateFormat;
    private Class selfClass = null;
    private boolean outputJSONReady = true;
    private KeyConversion writeKeyConversion;
    private List<KeyConversion> readKeyConversions;
    private Map<String,Class> listValueTypes;
    
    // Disabled because at this juncture it doesn't seem necessary to have
    // any map key types since JS only allows string types for key values.
    //private Map<String,Class> mapKeyTypes;
    
    // True if, when writing JSON, fields that can't be converted are omitted
    // with no error.  Set to false if you want an exception to be thrown
    // if a property cannot be "serialized"
    private boolean silentWriteMap = true;
    
    public DataMapper(){
        this(new HashMap<Class,DataMapper>());
    }
    
    public DataMapper(Map<Class,DataMapper> context){
        this.context = context;
        this.fieldMappers = new HashMap<String,FieldMapper>();
        this.dateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        this.dateFormats.add(new SimpleDateFormatExt("yyyy-MM-dd'T'HH:mm:ssXXX"));
        this.dateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"));
        this.dateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        
        this.dateFormats.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        this.dateFormats.add(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z"));
        this.dateFormats.add(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z"));
        this.dateFormats.add(new SimpleDateFormat("MM/dd/yyyy"));
        this.dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
        this.outputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        this.init();
    }
    
    protected void init(){
        
    }
    
    private Map<String,Class> listValueTypes(){
        if (listValueTypes==null){
            listValueTypes = new HashMap<String,Class>();
        }
        return listValueTypes;
    }
    
    public void setListValueType(String key, Class type){
        listValueTypes().put(key, type);
    }
    
    public Map<String,Class> getListValueTypes(){
        return Collections.unmodifiableMap(listValueTypes());
    }
    
    public Class getListValueType(String key){
        return listValueTypes().get(key);
    }
    
    public void unsetListValueType(String key){
        listValueTypes().remove(key);
    }
    
    /*
    
    // Disable this functionality for now as there doesn't seem to be 
    // a need to have custom map types since JSON only allows string keys.
    private Map<String,Class> mapKeyTypes(){
        if (mapKeyTypes==null){
            mapKeyTypes = new HashMap<String,Class>();
        }
        return mapKeyTypes;
    }
    
    public void setMapKeyType(String key, Class type){
        mapKeyTypes().put(key, type);
    }
    
    public Map<String,Class> getMapKeyTypes(){
        return Collections.unmodifiableMap(mapKeyTypes());
    }
    
    public Class getMapKeyType(String key){
        return mapKeyTypes().get(key);
    }
    
    public void unsetMapKeyType(String key){
        mapKeyTypes().remove(key);
    }
    */
    public static void createContext(List<DataMapper> mappers, Decorator decorator){
        Map<Class,DataMapper> context = new HashMap<Class,DataMapper>();
        for ( DataMapper mapper : mappers ){
            context.put(mapper.selfClass, mapper);
            mapper.setContext(context);
            if ( decorator != null ){
                decorator.decorate(mapper);
            }
            
        }
    }
    
    public static void createContext(List<DataMapper> mappers){
        createContext(mappers, null);
    }
    
    public void addDateFormat(DateFormat fmt){
        dateFormats.add(fmt);
    }
    
    public List<DateFormat> getDateFormats(){
        return Collections.unmodifiableList(dateFormats);
    }
    
    public void removeDateFormat(DateFormat fmt){
        dateFormats.remove(fmt);
    }
    
    public void clearDateFormats(){
        dateFormats.clear();
    }
    
    private String _readKey(Map map, String key){
        if ( readKeyConversions != null ){
            for ( KeyConversion conv : readKeyConversions ){
                String k = conv.convertKey(key);
                //System.out.println("Converting "+key+" to "+k);
                if ( fieldMappers.containsKey(k) || map.containsKey(k)){
                    key = k;
                    break;
                }
            }
        }
        return key;
    }
    
    public boolean exists(Map map, String key){
        key = _readKey(map, key);
        if ( fieldMappers.containsKey(key)){
            return fieldMappers.get(key).valueExists(map, key);
        } else {
            return map.containsKey(key);
        }
    }
    
    public Object get(Map map, String key){
        key = _readKey(map, key);
        if ( fieldMappers.containsKey(key)){
            return fieldMappers.get(key).getValue(map, key);
        } else {
            return map.get(key);
        }
    }
    
    public <T> Map<String,T> getMap(Map map, String key, Class<T> cls){
        HashMap<String,T> out = new HashMap<String,T>();
        Object o = get(map, key);
        if ( o instanceof Map ){
            Map src = (Map)o;
            if ( Integer.class.equals(cls)){
                for ( Object tmp : src.entrySet()){
                    Map.Entry entry = (Map.Entry)tmp;
                    out.put(
                            entry.getKey().toString(), 
                            (T)NumberUtil.boxedIntValue(entry.getValue())
                    );
                }
            } else if ( Float.class.equals(cls)){
                for ( Object tmp : src.entrySet()){
                    Map.Entry entry = (Map.Entry)tmp;
                    out.put(
                            entry.getKey().toString(), 
                            (T)NumberUtil.boxedFloatValue(entry.getValue())
                    );
                }
            } else if ( Double.class.equals(cls)){
                for ( Object tmp : src.entrySet()){
                    Map.Entry entry = (Map.Entry)tmp;
                    out.put(
                            entry.getKey().toString(), 
                            (T)NumberUtil.boxedDoubleValue(entry.getValue())
                    );
                }
            } else if ( Short.class.equals(cls)){
                for ( Object tmp : src.entrySet()){
                    Map.Entry entry = (Map.Entry)tmp;
                    out.put(
                            entry.getKey().toString(), 
                            (T)NumberUtil.boxedShortValue(entry.getValue())
                    );
                }
            } else if ( Long.class.equals(cls)){
                for ( Object tmp : src.entrySet()){
                    Map.Entry entry = (Map.Entry)tmp;
                    out.put(
                            entry.getKey().toString(), 
                            (T)NumberUtil.boxedLongValue(entry.getValue())
                    );
                }
            } else if ( Byte.class.equals(cls)){
                for ( Object tmp : src.entrySet()){
                    Map.Entry entry = (Map.Entry)tmp;
                    out.put(
                            entry.getKey().toString(), 
                            (T)NumberUtil.boxedByteValue(entry.getValue())
                    );
                }
            } else if ( Character.class.equals(cls)){
                for ( Object tmp : src.entrySet()){
                    Map.Entry entry = (Map.Entry)tmp;
                    out.put(
                            entry.getKey().toString(), 
                            (T)NumberUtil.boxedCharacterValue(entry.getValue())
                    );
                }
            } else if ( Boolean.class.equals(cls)){
                for ( Object tmp : src.entrySet()){
                    Map.Entry entry = (Map.Entry)tmp;
                    out.put(
                            entry.getKey().toString(), 
                            (T)NumberUtil.boxedBooleanValue(entry.getValue())
                    );
                }
            } else if ( Date.class.equals(cls)){
                for ( Object tmp : src.entrySet()){
                    Map.Entry entry = (Map.Entry)tmp;
                    out.put(
                            entry.getKey().toString(), 
                            (T)NumberUtil.dateValue(entry.getValue(), dateFormats)
                    );
                }
            } else if ( String.class.equals(cls)){
                for ( Object tmp : src.entrySet()){
                    Map.Entry entry = (Map.Entry)tmp;
                    out.put(
                            entry.getKey().toString(), 
                            (T)(""+entry.getValue())
                    );
                }
            } else if ( context.containsKey(cls)){
                DataMapper mapper = context.get(cls);
                for ( Object tmp : src.entrySet()){
                    Map.Entry entry = (Map.Entry)tmp;
                    out.put(
                            entry.getKey().toString(), 
                            (T)mapper.readMap((Map)entry.getValue(), cls)
                    );
                }
                
            }
        }
        return out;
    }
    
    public <T> List<T> getList(Map map, String key, Class<T> cls){
        if (get(map,key)==null) {
            return null;
        }
        ArrayList<T> out = new ArrayList<T>();
        if ( Integer.class.equals(cls)){
            List l = (List)get(map, key);
            for ( Object o : l ){
                out.add((T)NumberUtil.boxedIntValue(o));
            }
            
        } else if (  Double.class.equals(cls)){
            List l = (List)get(map, key);
            for ( Object o : l ){
                out.add((T)NumberUtil.boxedDoubleValue(o));
            }
        } else if (  Long.class.equals(cls)){
            List l = (List)get(map, key);
            for ( Object o : l ){
                out.add((T)NumberUtil.boxedLongValue(o));
            }
        } else if (  Short.class.equals(cls)){
            List l = (List)get(map, key);
            for ( Object o : l ){
                out.add((T)NumberUtil.boxedShortValue(o));
            }
        } else if (  Float.class.equals(cls)){
            List l = (List)get(map, key);
            for ( Object o : l ){
                out.add((T)NumberUtil.boxedFloatValue(o));
            }
        } else if (  Boolean.class.equals(cls)){
            List l = (List)get(map, key);
            for ( Object o : l ){
                out.add((T)NumberUtil.boxedBooleanValue(o));
            }
        } else if (  Character.class.equals(cls)){
            List l = (List)get(map, key);
            for ( Object o : l ){
                out.add((T)NumberUtil.boxedBooleanValue(o));
            }
        } else if (  String.class.equals(cls)){
            List l = (List)get(map, key);
            for ( Object o : l ){
                out.add((T)String.valueOf(o));
            }
        } else if ( Date.class.equals(cls)){
            List l = (List)get(map, key);
            for ( Object o : l ){
                out.add((T)NumberUtil.dateValue(o, dateFormats));
            }
        } else if ( context.containsKey(cls) ){
            DataMapper mapper = context.get(cls);
            List l = (List)get(map, key);
            for ( Object o : l ){
                out.add((T)mapper.readMap((Map)o, cls));
            }
        }
        return out;
    }
    
    public Object get(Map map, String key, Class cls){
        if (  Integer.class.equals(cls)){
            return getInt(map, key);
        } else if (  Double.class.equals(cls)){
            return getDouble(map, key);
        } else if ( Long.class.equals(cls)){
            return getLong(map, key);
        } else if (  Short.class.equals(cls)){
            return getShort(map, key);
        } else if (  Float.class.equals(cls)){
            return getFloat(map, key);
        } else if (  Boolean.class.equals(cls)){
            return getBoolean(map, key);
        } else if (  Character.class.equals(cls)){
            throw new RuntimeException("Char not supported yet");
        } else if ( Byte.class.equals(cls)){
            return getByte(map, key);
        } else if ( String.class.equals(cls)){
            return getString(map, key);
        } else if ( Date.class.equals(cls)){
            return getDate(map, key);
        } else if ( context.containsKey(cls) ){
            return getObject(map, key, cls);
        } else if ( List.class.isAssignableFrom(cls) && listValueTypes != null && listValueTypes.containsKey(key)){
            return getList(map, key, listValueTypes.get(key));
        } else if ( Map.class.isAssignableFrom(cls) && listValueTypes != null && listValueTypes.containsKey(key)){
            return getMap(map, key, listValueTypes.get(key));
        } else {
            throw new RuntimeException("Failed to get key "+key+" for class "+cls+" because it was not a registered object type");
        }
    }
    
    
    public Object jsonify(Object item){
        if ( item == null ){
            return item;
        }
        Class cls = item.getClass();
        if ( NumberUtil.isNumber(item) || cls == String.class || cls == Boolean.class || cls == Character.class){
            return item;
        } else if (Object[].class.isAssignableFrom(cls)){
            if ( Integer[].class == cls || Short[].class == cls || Double[].class == cls || String[].class == cls || Long[].class == cls || Float.class == cls || Byte.class == cls || Character.class == cls || Boolean.class == cls ){
                return Arrays.asList((Object[])item);
            } else {
                List out = new ArrayList(((Object[])item).length);
            
                for ( Object o : ((Object[])item)){
                    out.add(jsonify(o));
                }
                return out;
            }
        } else if ( Collection.class.isAssignableFrom(cls)){
            List out = new ArrayList(((Collection)item).size());
            for ( Object o : (Collection)item){
                out.add(jsonify(o));
            }
            return out;
        } else if ( Map.class.isAssignableFrom(cls)){
            Map<String,Object> out = new HashMap<String,Object>();
            for ( Object key : ((Map)item).keySet()){
                out.put(key.toString(), jsonify(((Map)item).get(key)));
            }
            return out;
        } else if ( context.containsKey(cls)){
            DataMapper mapper = context.get(cls);
            Map m = new HashMap();
            boolean oldJSONReady = mapper.isOutputJSONReady();
            boolean oldSilentWrite = mapper.isSilentWriteMap();
            mapper.setOutputJSONReady(true);
            mapper.setSilentWriteMap(this.isSilentWriteMap());
            mapper.writeMap(m, item);
            mapper.setOutputJSONReady(oldJSONReady);
            mapper.setSilentWriteMap(oldSilentWrite);
            return m;
        } else if (Enum.class.isAssignableFrom(cls)) {
            return ((Enum)item).name();
        } else if ( Date.class.isAssignableFrom(cls)){
            return getOutputDateFormat().format((Date)item);
        } else if ( !isSilentWriteMap() ){
            throw new RuntimeException("Failed to jsonify value "+item+" because its class is not an appropriate type to be serialized.");
        } else {
            return null;
        }
    }
    public void set(Map map, String key, Object value){
        if ( getWriteKeyConversion() != null ){
            key = getWriteKeyConversion().convertKey(key);
        }
        if ( isOutputJSONReady() ){
           value = jsonify(value);
        }
        if ( fieldMappers.containsKey(key)){
            fieldMappers.get(key).putValue(map, key, value);
        } else {
            map.put(key, value);
        }
    }
    
    public boolean getBoolean(Map map, String key){
        return NumberUtil.booleanValue(get(map, key));
    }
    
    public byte getByte(Map map, String key){
        return NumberUtil.byteValue(get(map, key));
    }
    
    public char getChar(Map map, String key){
        return NumberUtil.charValue(get(map, key));
    }
    
    public float getFloat(Map map, String key){
        return (float)getDouble(map, key);
    }
    public int getInt(Map map, String key){
        return NumberUtil.intValue(get(map,key));
    }
    
    public short getShort(Map map, String key){
        return (short)getInt(map, key);
    }
    
    public double getDouble(Map map, String key){
        return NumberUtil.doubleValue(get(map,key));
    }
    
    public long getLong(Map map, String key){
        return NumberUtil.longValue(get(map,key));
    }
    
    public String getString(Map map, String key){
        return (String)""+get(map,key);
    }
    
    public Date getDate(Map map, String key){
        return NumberUtil.dateValue(get(map, key), dateFormats);
    }
    
    public <T> T getObject(Map map, String key, Class<T> klass){
        Object o = get(map, key);
        if ( o instanceof Map){
            return readMap((Map)o, klass);
        } else if ( o != null && klass.isAssignableFrom(o.getClass())){
            return (T)o;
        } else if (Enum.class.isAssignableFrom(klass)) {
            if (o instanceof String) {
                return (T)Enum.valueOf((Class<? extends Enum>)klass, key);
            } else {
                throw new RuntimeException("Illegal value type when reading Enum value for key "+key+" of type "+klass);
            }
        } else {
            throw new RuntimeException("Illegal object type for key "+key+".  Expected "+klass+" but found "+o.getClass());
        }
    }
    
    public <T> List<T> getObjects(Map map, String key, Class<T> klass){
        Object o = get(map, key);
        List<T> out = new ArrayList<T>();
        if ( o instanceof java.util.List ){
            List l = (List)o;
            for ( Object oMap : l ){
                if ( oMap instanceof Map ){
                    out.add(readMap((Map)oMap, klass));
                }
            }
        } else if ( o instanceof Object[] ){
            Object[] l = (Object[])o;
            for ( Object oMap : l ){
                if ( oMap instanceof Map ){
                    out.add(readMap((Map)oMap, klass));
                }
            }
        }
        return out;
    }
    
    public Date[] getDateArray(Map map, String key){
        Object o = get(map, key);
        if ( o instanceof Date[] ){
            return (Date[])o;
        } else if ( o instanceof Collection ){
            Collection c = (Collection)o;
            int size = c.size();
            Date[] out = new Date[size];
            int i=0;
            for ( Object item : c ){
                out[i++] = NumberUtil.dateValue(item, dateFormats);
            }
            return out;
        } else {
            throw new RuntimeException("Illegal type conversion");
        }
    }
    
    public int[] getIntArray(Map map, String key){
        Object o = get(map, key);
        if ( o instanceof int[] ){
            return (int[])o;
        } else if ( o instanceof Collection ){
            Collection c = (Collection)o;
            int size = c.size();
            int[] out = new int[size];
            int i=0;
            for ( Object item : c ){
                out[i++] = NumberUtil.intValue(item);
            }
            return out;
        } else {
            throw new RuntimeException("Illegal type conversion");
        }
    }
    
    public double[] getDoubleArray(Map map, String key){
        Object o = get(map, key);
        if ( o instanceof double[] ){
            return (double[])o;
        } else if ( o instanceof Collection ){
            Collection c = (Collection)o;
            int size = c.size();
            double[] out = new double[size];
            int i=0;
            for ( Object item : c ){
                out[i++] = NumberUtil.doubleValue(item);
            }
            return out;
        } else {
            throw new RuntimeException("Illegal type conversion");
        }
    }
    
    
    
    protected <T> T createObject(Class<T> klass){
        if ( factory == null ){
            try {
                return (T)klass.newInstance();
            } catch (Throwable t ){
                throw new RuntimeException(t.getMessage());
            }
        } else {
            return factory.createObject(klass);
        }
    }
    
    public <T> T readMap(Map map, Class<T> klass){
        DataMapper mapper = context.get(klass);
        if ( mapper == this){
            T obj = createObject(klass);
            //System.out.println("Readng map "+map);
            readMap(map, obj);
            return obj;
        } else if ( mapper != null) {
            return mapper.readMap(map, klass);
        } else {
            return null;
        }
    }
    
    public Map readJSONFromURL(String url) throws IOException {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(url);
        req.setPost(false);
        req.setHttpMethod("GET");
        return readJSONFromConnection(req);
    }
    
    public Map readJSONFromConnection(ConnectionRequest req) throws IOException {
        NetworkManager.getInstance().addToQueueAndWait(req);
        return readJSON(new ByteArrayInputStream(req.getResponseData()), "UTF-8");
    }
    
    public Map readJSON(InputStream is, String charset) throws IOException {
        JSONParser parser = new JSONParser();
        Map m = parser.parseJSON(new InputStreamReader(is, charset));
        return m;
    }
    
    
    
    public <T> T readJSONFromURL(String url, Class<T> klass) throws IOException {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(url);
        req.setPost(false);
        req.setHttpMethod("GET");
        return readJSONFromConnection(req, klass);
    }
    
    public <T> T readJSONFromURL(String url, Class<T> klass, String path) throws IOException {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(url);
        req.setPost(false);
        req.setHttpMethod("GET");
        return readJSONFromConnection(req, klass, path);
    }
    
    public <T> T readJSONFromConnection(ConnectionRequest req, Class<T> klass) throws IOException {
        NetworkManager.getInstance().addToQueueAndWait(req);
        return readJSON(new ByteArrayInputStream(req.getResponseData()), klass);
        
    }
    
    public <T> T readJSONFromConnection(ConnectionRequest req, Class<T> klass, String path) throws IOException {
        NetworkManager.getInstance().addToQueueAndWait(req);
        return readJSON(new ByteArrayInputStream(req.getResponseData()), klass, path);
        
    }
    
    public <T> T readJSON(InputStream is, String charset, Class<T> klass) throws IOException {
        JSONParser parser = new JSONParser();
        Map m = parser.parseJSON(new InputStreamReader(is, charset));
        return readMap(m, klass);
    }
    
    public <T> T readJSON(InputStream is, String charset, Class<T> klass, String path) throws IOException {
        JSONParser parser = new JSONParser();
        Map m = parser.parseJSON(new InputStreamReader(is, charset));
        Result r = Result.fromContent(m);
        m = (Map)r.get(path);
        return readMap(m, klass);
    }
    
    public <T> T readJSON(InputStream is,  Class<T> klass) throws IOException {
        return readJSON(is, "UTF-8", klass);
    }
    
    public <T> T readJSON(InputStream is,  Class<T> klass, String path) throws IOException {
        return readJSON(is, "UTF-8", klass, path);
    }
    
    public <T> T readJSON(String data, Class<T> klass) throws IOException {
        return readJSON(new ByteArrayInputStream(data.getBytes("UTF-8")), klass);
    }
    
    public abstract void writeMap(Map dest, Object src);
    
    public Map writeMap(Object src){
        Map out = new HashMap();
        writeMap(out, src);
        return out;
    }
    
    public abstract void readMap(Map src, Object dest);
    
    
    
    public void setFieldMapper(String field, FieldMapper mapper){
        fieldMappers.put(field, mapper);
    }
    
    public void removeFieldMapper(String field){
        fieldMappers.remove(field);
    }
    
    public void setFieldMapper(String field, String path){
        setFieldMapper(field, new SimpleFieldMapper(path));
    }
    
    public void register(Class klass, DataMapper mapper){
        if ( mapper == this){
            selfClass = klass;
        }
        context.put(klass, mapper);
    }
    
    public void unregister(Class klass){
        context.remove(klass);
    }
    
    public Map<Class,DataMapper> getContext(){
        return context;
    }
    
    public void setContext(Map<Class,DataMapper> context){
        this.context = context;
    }
    
    public void setObjectFactory(ObjectFactory f){
        this.factory = f;
    }
    
    public ObjectFactory getObjectFactory(){
        return factory;
    }
    
    public DataMapper getDataMapperForClass(Class cls){
        return context.get(cls);
    }

    /**
     * @return the outputJSONReady
     */
    public boolean isOutputJSONReady() {
        return outputJSONReady;
    }

    /**
     * @param outputJSONReady the outputJSONReady to set
     */
    public void setOutputJSONReady(boolean outputJSONReady) {
        this.outputJSONReady = outputJSONReady;
    }

    /**
     * @return the silentWriteMap
     */
    public boolean isSilentWriteMap() {
        return silentWriteMap;
    }

    /**
     * @param silentWriteMap the silentWriteMap to set
     */
    public void setSilentWriteMap(boolean silentWriteMap) {
        this.silentWriteMap = silentWriteMap;
    }
    
    /**
     * Reads a list of Maps and produces a list of the converted type
     * @param input A list of maps that need to be converted to objects.
     * @return A list of objects corresponding to the input maps.
     */
    public <T> List<T> parseListOfMaps(List<Map> input, Class<T> outType) {
        List<T> out = new ArrayList<T>(input.size());
        for (Map item : input) {
            out.add(readMap(item, outType));
        }
        return out;
    }
    
    /**
     * Reads a map of maps to produce a map of the converted type.
     * @param input The map of maps that need to be converted to objects.
     * @param outType The map of objects corresponding to the input maps.
     * @return 
     */
    public Map parseMapOfMaps(Map input, Class outType) {
        Map out = new HashMap();
        for (Object key : input.keySet()) {
            out.put(key, readMap((Map)input.get(key), outType));
        }
        return out;
    }
    
}
