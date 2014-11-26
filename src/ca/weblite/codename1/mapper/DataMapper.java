/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.mapper;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.NetworkManager;
import com.codename1.l10n.DateFormat;
import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
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
    private Map<Class,DataMapper> context;
    private Map<String,FieldMapper> fieldMappers;
    private ObjectFactory factory;
    private List<DateFormat> dateFormats = new ArrayList<DateFormat>();
    private DateFormat outputDateFormat;
    private Class selfClass = null;
    private boolean outputJSONReady = true;
    
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
        this.dateFormats.add(new SimpleDateFormatExt("yyyy-MM-dd'T'HH:mm:ssXXX"));
        this.dateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"));
        this.dateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        this.dateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        this.dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
        this.dateFormats.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        this.dateFormats.add(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z"));
        this.dateFormats.add(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z"));
        this.dateFormats.add(new SimpleDateFormat("MM/dd/yyyy"));
        this.outputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        this.init();
    }
    
    protected void init(){
        
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
    public boolean exists(Map map, String key){
        if ( fieldMappers.containsKey(key)){
            return fieldMappers.get(key).valueExists(map, key);
        } else {
            return map.containsKey(key);
        }
    }
    
    public Object get(Map map, String key){
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
       
        } else {
            throw new RuntimeException("Failed to get key "+key+" because it was not a registered object type");
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
        } else if ( Date.class.isAssignableFrom(cls)){
            return outputDateFormat.format((Date)item);
        } else if ( !isSilentWriteMap() ){
            throw new RuntimeException("Failed to jsonify value "+item+" because its class is not an appropriate type to be serialized.");
        } else {
            return null;
        }
    }
    public void set(Map map, String key, Object value){
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
        return (String)get(map,key);
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
    
    public <T> T readJSONFromURL(String url, Class<T> klass) throws IOException {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(url);
        req.setPost(false);
        req.setHttpMethod("GET");
        return readJSONFromConnection(req, klass);
    }
    
    public <T> T readJSONFromConnection(ConnectionRequest req, Class<T> klass) throws IOException {
        NetworkManager.getInstance().addToQueueAndWait(req);
        return readJSON(new ByteArrayInputStream(req.getResponseData()), klass);
        
    }
    
    public <T> T readJSON(InputStream is, String charset, Class<T> klass) throws IOException {
        JSONParser parser = new JSONParser();
        Map m = parser.parseJSON(new InputStreamReader(is, charset));
        return readMap(m, klass);
    }
    
    public <T> T readJSON(InputStream is,  Class<T> klass) throws IOException {
        return readJSON(is, "UTF-8", klass);
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
    
}
