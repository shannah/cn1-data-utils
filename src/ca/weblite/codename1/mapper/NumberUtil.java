/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.codename1.mapper;

import com.codename1.l10n.DateFormat;
import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author shannah
 */
public class NumberUtil {
    
    public static boolean isNumber(Object o){
        
        if ( o == null ){
            return false;
        }
        
        Class cls = o.getClass();
        return cls == Integer.class || 
                cls == Double.class || 
                cls == Float.class ||
                cls == Long.class ||
                cls == Byte.class ||
                cls == Short.class;
    }
    
    public static Date dateValue(Object o, DateFormat[] formats){
        if ( o instanceof Date ){
            return (Date)o;
        } else if ( NumberUtil.isNumber(o)){
            return new Date(NumberUtil.longValue(o));
        } else if ( o instanceof String ){
            int len = formats.length;
            for ( int i=0; i<len; i++){
                try {
                    //System.out.println("Tring string format "+formats[i]+" on date "+o);
                    return formats[i].parse((String)o);
                } catch (ParseException ex) {
                    
                }
            }
            
            throw new RuntimeException("Failed to parse string date format "+o+".  Could not find appropriate format parser.");
            
        }
        throw new RuntimeException("Failed to get date for field "+o+" because value was wrong type.  Expected number, string, or Date, but found "+o.getClass());
        
    }
    
    public static Date dateValue(Object o, List<DateFormat> formats){
        if ( o instanceof Date ){
            return (Date)o;
        } else if ( NumberUtil.isNumber(o)){
            return new Date(NumberUtil.longValue(o));
        } else if ( o instanceof String ){
            for ( DateFormat fmt : formats ){
                try {
                    //System.out.println("Tring string format "+((SimpleDateFormat)fmt).toPattern()+" on date "+o);
                    return fmt.parse((String)o);
                } catch (ParseException ex) {
                    //System.out.println("Faield to parse using format patter: "+ex.getMessage());
                }
            }
            
            throw new RuntimeException("Failed to parse string date format "+o+".  Could not find appropriate format parser.");
            
        }
        throw new RuntimeException("Failed to get date for field "+o+" because value was wrong type.  Expected number, string, or Date, but found "+o.getClass());
        
    }
    
    public static int intValue(Object o){
        if ( o == null ){
            return 0;
        }
        Class c = o.getClass();
        if ( c == Integer.class ){
            return ((Integer)o);
        } else if ( c == Double.class){
            return ((Double)o).intValue();
        } else if ( c == Short.class ){
            return ((Short)o);
        } else if ( c == Float.class ){
            return ((Float)o).intValue();
        } else if ( c == Long.class){
            return (int)((Long)o).longValue();
        } else {
            return Integer.parseInt(""+o);
        }
    }
    
    public static byte byteValue(Object o){
        if ( o == null ){
            return 0;
        }
        if ( o instanceof Byte){
            return ((Byte)o);
        }
        return Integer.valueOf(""+o).byteValue();
    }
    
    
    
    public static short shortValue(Object o){
        if ( o == null ){
            return 0;
        }
        if ( o instanceof Integer ){
            return (short)((Integer)o).intValue();
        } else if ( o instanceof Double ){
            return (short)((Double)o).intValue();
        } else if ( o instanceof Short ){
            return ((Short)o);
        } else if ( o instanceof Float ){
            return (short)((Float)o).intValue();
        } else if ( o instanceof Long ){
            return (short)((Long)o).longValue();
        } else {
            return Short.parseShort(""+o);
        }
    }
    
    public static Integer boxedIntValue(Object o){
        if ( o == null ){
            return 0;
        }
        if ( o instanceof Integer ){
            return (Integer)o;
        } else if ( o instanceof Double ){
            return ((Double)o).intValue();
        } else if ( o instanceof Short ){
            return (int) ((Short)o);
        } else if ( o instanceof Float ){
            return ((Float)o).intValue();
        } else if ( o instanceof Long ){
            return (int)((Long)o).longValue();
        } else {
            return Integer.parseInt(""+o);
        }
    }
    
    public static Byte boxedByteValue(Object o){
        if ( o == null ){
            return 0;
        }
        if ( o instanceof Byte ){
            return (Byte)o;
        } else if ( o instanceof Integer ){
            return ((Integer)o).byteValue();
        } else if ( o instanceof Double ){
            return ((Double)o).byteValue();
        } else if ( o instanceof Short ){
            return (byte) ((Short)o).shortValue();
        } else if ( o instanceof Float ){
            return ((Float)o).byteValue();
        } else if ( o instanceof Long ){
            return new Integer((int)((Long)o).longValue()).byteValue();
        } else {
            return (byte)Integer.parseInt(""+o);
        }
    }
    
    public static Character boxedCharacterValue(Object o){
        if ( o == null ){
            return 0;
        }
        if ( o instanceof Character ){
            return (Character)o;
        } else {
            String str = ""+o;
            if ( str.length() > 0 ){
                return (""+o).charAt(0);
            } else {
                return '\0';
            }
        } 
         
    }
    
    public static Short boxedShortValue(Object o){
        if ( o == null ){
            return 0;
        }
        if ( o instanceof Integer ){
            return (short)((Integer)o).intValue();
        } else if ( o instanceof Double ){
            return (short)((Double)o).intValue();
        } else if ( o instanceof Short ){
            return (Short)o;
        } else if ( o instanceof Float ){
            return (short)((Float)o).intValue();
        } else if ( o instanceof Long ){
            return (short)((Long)o).longValue();
        } else {
            return Short.parseShort(""+o);
        }
    }
    
    public static long longValue(Object o){
        if ( o == null ){
            return 0l;
        }
        if ( o instanceof Integer ){
            return ((Integer)o).longValue();
        } else if ( o instanceof Double ){
            return ((Double)o).longValue();
        } else if ( o instanceof Short ){
            return ((Short)o);
        } else if ( o instanceof Float ){
            return ((Float)o).longValue();
        } else if ( o instanceof Long ){
            return ((Long)o).longValue();
        } else {
            return Long.parseLong(""+o);
        }
    }
    
    public static Long boxedLongValue(Object o){
        if ( o == null ){
            return 0l;
        }
        if ( o instanceof Integer ){
            return ((Integer)o).longValue();
        } else if ( o instanceof Double ){
            return ((Double)o).longValue();
        } else if ( o instanceof Short ){
            return (long) ((Short)o);
        } else if ( o instanceof Float ){
            return ((Float)o).longValue();
        } else if ( o instanceof Long ){
            return ((Long)o);
        } else {
            return Long.parseLong(""+o);
        }
    }
    
    public static double doubleValue(Object o){
        if ( o == null ){
            return 0.0;
        }
        if ( o instanceof Integer ){
            return ((Integer)o).doubleValue();
        } else if ( o instanceof Double ){
            return ((Double)o);
        } else if ( o instanceof Short ){
            return ((Short)o);
        } else if ( o instanceof Float ){
            return ((Float)o).doubleValue();
        } else if ( o instanceof Long ){
            return ((Long)o).doubleValue();
        } else {
            return Double.parseDouble(""+o);
        }
    }
    
    
    public static float floatValue(Object o){
        if ( o == null ){
            return 0f;
        }
        if ( o instanceof Integer ){
            return ((Integer)o).floatValue();
        } else if ( o instanceof Double ){
            return ((Double)o).floatValue();
        } else if ( o instanceof Short ){
            return (float)((Short)o).shortValue();
        } else if ( o instanceof Float ){
            return (Float)o;
        } else if ( o instanceof Long ){
            return ((Long)o).floatValue();
        } else {
            return Float.parseFloat(""+o);
        }
    }
    
    public static Double boxedDoubleValue(Object o){
        if ( o == null ){
            return 0.0;
        }
        if ( o instanceof Integer ){
            return ((Integer)o).doubleValue();
        } else if ( o instanceof Double ){
            return ((Double)o);
        } else if ( o instanceof Short ){
            return new Double(((Short)o));
        } else if ( o instanceof Float ){
            return ((Float)o).doubleValue();
        } else if ( o instanceof Long ){
            return ((Long)o).doubleValue();
        } else {
            return Double.parseDouble(""+o);
        }
    }
    
    public static Float boxedFloatValue(Object o){
        if ( o == null ){
            return 0f;
        }
        if ( o instanceof Integer ){
            return ((Integer)o).floatValue();
        } else if ( o instanceof Double ){
            return ((Double)o).floatValue();
        } else if ( o instanceof Short ){
            return new Float(((Short)o));
        } else if ( o instanceof Float ){
            return (Float)o;
        } else if ( o instanceof Long ){
            return ((Long)o).floatValue();
        } else {
            return Float.parseFloat(""+o);
        }
    }
    
    public static boolean booleanValue(Object o){
        if ( o == null ){
            return false;
        }
        if ( o instanceof Boolean ){
            return (Boolean)o;
        } else if ( o instanceof Integer ){
            int i = (Integer)o;
            return i != 0;
        } else if ( o instanceof Double ){
            double d = (Double)o;
            return d != 0.0;
        } else if ( o instanceof Short ){
            short s = (Short)o;
            return s != 0;
        } else if ( o instanceof Float ){
            float f = (Float)o;
            return f != 0f;
        } else if ( o instanceof Long ){
            long l = (Long)o;
            return l != 0l;
        } else {
            return Boolean.parseBoolean(""+o);
        }
    }
    
    public static Boolean boxedBooleanValue(Object o){
        if ( o == null ){
            return false;
        }
        if ( o instanceof Boolean){
            return (Boolean)o;
        } else if ( o instanceof Integer ){
            int i = (Integer)o;
            return i != 0;
        } else if ( o instanceof Double ){
            double d = (Double)o;
            return d != 0.0;
        } else if ( o instanceof Short ){
            short s = (Short)o;
            return s != 0;
        } else if ( o instanceof Float ){
            float f = (Float)o;
            return f != 0f;
        } else if ( o instanceof Long ){
            long l = (Long)o;
            return l != 0l;
        } else {
            return Boolean.parseBoolean(""+o);
        }
    }
    
    public static char charValue(Object o){
        if ( o == null ){
            return 0;
        } else if ( o instanceof Character ){
            return (Character)o;
        } else if ( o instanceof String){
            String s = (String)o;
            if ( s.length() == 0){
                return 0;
            } else {
                return s.charAt(0);
            }
        } else {
            return charValue(""+o);
        }
    }
    
    public static Character boxedCharValue(Object o){
        if ( o == null ){
            return 0;
        } else if ( o instanceof Character ){
            return (Character)o;
        } else if ( o instanceof String){
            String s = (String)o;
            if ( s.length() == 0){
                return 0;
            } else {
                return s.charAt(0);
            }
        } else {
            return charValue(""+o);
        }
    }
    
    public static double[] toDoubleArray(Object[] ints){
        double[] out = new double[ints.length];
        int i=0;
        for ( Object o : ints ){
            if ( o == null ){
                out[i++] = 0;
            } else {
                out[i++] = doubleValue(o);
            }
        }
        return out;
    }
    
    public static float[] toFloatArray(Object[] ints){
        float[] out = new float[ints.length];
        int i=0;
        for ( Object o : ints ){
            if ( o == null ){
                out[i++] = 0;
            } else {
                out[i++] = floatValue(o);
            }
        }
        return out;
    }
    
    
    public static int[] toIntArray(Object[] ints){
        int[] out = new int[ints.length];
        int i=0;
        for ( Object o : ints ){
            if ( o == null ){
                out[i++] = 0;
            } else {
                out[i++] = intValue(o);
            }
        }
        return out;
    }
    
    public static byte[] toByteArray(Object[] ints){
        byte[] out = new byte[ints.length];
        int i=0;
        for ( Object o : ints ){
            if ( o == null ){
                out[i++] = 0;
            } else {
                out[i++] = byteValue(o);
            }
        }
        return out;
    }
    
    public static char[] toCharArray(Object[] ints){
        char[] out = new char[ints.length];
        int i=0;
        for ( Object o : ints ){
            if ( o == null ){
                out[i++] = 0;
            } else {
                out[i++] = charValue(o);
            }
        }
        return out;
    }
    
    public static boolean[] toBooleanArray(Object[] ints){
        boolean[] out = new boolean[ints.length];
        int i=0;
        for ( Object o : ints ){
            if ( o == null ){
                out[i++] = false;
            } else {
                out[i++] = booleanValue(o);
            }
        }
        return out;
    }
    
    
}
