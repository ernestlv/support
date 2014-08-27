package com.scrippsnetworks.wcm.export.snipage;

import com.scrippsnetworks.wcm.export.snipage.xml.bind.PROP;
import com.scrippsnetworks.wcm.export.snipage.xml.bind.RECORD;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueMapToRecordAdapterTest {

    public enum TestTypes {
        STRING(String.class),
        STRINGARRAY(String[].class),
        CALENDAR(Calendar.class),
        INTEGER(Integer.class),
        BOOLEAN(Boolean.class),
        INTEGERARRAY(Integer[].class);

        Class clazz;

        TestTypes(Class clazz) {
            this.clazz = clazz;
        }

        public Class getValueClass() {
            return clazz;
        }
    }

    Map<String, Object> map = null;
    ValueMapToRecordAdapter adapter = new ValueMapToRecordAdapter();
    Calendar singleCalendar = Calendar.getInstance();
    Integer singleInteger = 3;
    Integer[] integerArray = new Integer[] { 1, 2 };
    Boolean bool = true;

    @Before
    public void before() {
        map = new HashMap<String, Object>();
        map.put(TestTypes.STRING.name(), "STRING");
        map.put(TestTypes.STRINGARRAY.name(), new String[] { "StringArrayItem1", "StringArrayItem2" });
        map.put(TestTypes.CALENDAR.name(), singleCalendar);
        map.put(TestTypes.INTEGER.name(), singleInteger);
        map.put(TestTypes.INTEGERARRAY.name(), integerArray);
        map.put(TestTypes.BOOLEAN.name(), bool);
    }

    @Test
    public void testAllPropsAreTypes() {
        ValueMap vMap = new ValueMapDecorator(map);

        RECORD record = adapter.marshal(vMap);
        List<PROP> props = record.getPROP();
        for (PROP prop : props) {
            String name = prop.getNAME();
            TestTypes type = TestTypes.valueOf(name);
            assertNotNull("type exists", type);
            Class clazz = type.getValueClass();
            assertTrue(name + " has a value", prop.getPVAL().size() > 0);
            if (clazz == String[].class || clazz == Integer[].class || clazz == Calendar[].class) {
                assertTrue(name + " has multiple values", prop.getPVAL().size() > 1);
            } else {
                assertEquals("scalar has one value", 1, prop.getPVAL().size());
            }
        }
    }

    @Test
    public void testAllTypesHaveProps() {
        ValueMap vMap = new ValueMapDecorator(map);

        RECORD record = adapter.marshal(vMap);
        List<PROP> props = record.getPROP();
        for (TestTypes type : TestTypes.values()) {
            boolean matched = false;
            for (PROP prop : props) {
                String name = prop.getNAME();
                if (type.name().equals(name)) {
                    matched = true;
                    break;
                }
            }
            assertTrue("type has property in record", matched);
        }
    }

}
