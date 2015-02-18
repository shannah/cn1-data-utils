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
            
        } else {
            convertTimezone = false;
        }
        
        super.applyPattern(pattern); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    @Override
    public Date parse(String source) throws ParseException {
        if ( convertTimezone ){
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
