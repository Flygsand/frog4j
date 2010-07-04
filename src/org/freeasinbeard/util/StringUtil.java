package org.freeasinbeard.util;

import java.util.Iterator;

public class StringUtil {
    
    public static <T> String join(Iterable<T> iterable, String delim) {
        Iterator<T> iter = iterable.iterator();
        
        if (!iter.hasNext())
            return "";
        
        StringBuffer sb = new StringBuffer().append(iter.next());
        while (iter.hasNext()) {
            sb.append(delim).append(iter.next());
        }
        
        return sb.toString();
    }
    
}
