package org.freeasinbeard.util;

import static org.freeasinbeard.util.StringUtil.join;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestStringUtil {
    @Test
    public void testEmptyIterable() {
        assertEquals("", join(new ArrayList<String>(), ","));
    }
    
    @Test
    public void testSingleItemIterable() {
        List<String> list = new ArrayList<String>();
        list.add("foo");
        
        assertEquals("foo", join(list, ","));
    }
    
    @Test
    public void testMultiItemIterable() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        
        assertEquals("1,2,3", join(list, ","));
    }
}
