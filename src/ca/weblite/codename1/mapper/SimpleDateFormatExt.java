/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.codename1.mapper;

import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.util.StringUtil;
import java.util.Date;

/**
 *
 * @author shannah
 */
public class SimpleDateFormatExt extends SimpleDateFormat {

    boolean convertTimezone = false;
    
    public SimpleDateFormatExt(String pattern){
        super();
        this.applyPattern(pattern);
    }
    
    @Override
    public void applyPattern(String pattern) {
        if ( pattern.indexOf("XXX") == pattern.length()-3){
            convertTimezone = true;
            pattern = pattern.substring(0, pattern.length()-3)+"Z";
            System.out.println("New pattern is "+pattern);
        } else {
            convertTimezone = false;
        }
        
        super.applyPattern(pattern); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    @Override
    public Date parse(String source) throws ParseException {
        if ( convertTimezone ){
            System.out.println("Parsing "+source);
            int len = source.length();
            if ( len >= 6 ){
                String base = source.substring(0, len-6);
                String tz = source.substring(len-6);
                tz = StringUtil.replaceAll(tz, ":", "");
                source = base+tz;
            }
        }
        return super.parse(source); //To change body of generated methods, choose Tools | Templates.
    }
    
}
