package to.talk.thunderbolt.mrs;

import java.io.InputStream;

import org.junit.Test;

import to.talk.thunderbolt.StreamingParser;

public class MrsTest {

    private StreamingParser parser = new StreamingParser();
    
    protected InputStream getResource(int id) {
        return getClass().getClassLoader().getResourceAsStream("mrs/test" + id + ".xml");
    }
    
    @Test
    public void test_1() {
        GetContactsIq iq = parser.parse(getResource(1), GetContactsIq.class);
    }
    
    @Test
    public void test_2() {
        MetaContactIq iq = parser.parse(getResource(2), MetaContactIq.class);
    }
}
