package to.talk.thunderbolt;

import java.io.InputStream;
import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

    private StreamingParser parser = new StreamingParser();
    
    protected InputStream getResource(int id) {
        return getClass().getClassLoader().getResourceAsStream("test" + id + ".xml");
    }
    
    // ignore properties if getter/setters are not available
    @Test
    public void test_0() {
        Name name = parser.parse(getResource(0), Name.class);
        Assert.assertNull(name.getTitle());
        Assert.assertNull(name.getFirstName());
        Assert.assertNull(name.getLastName());
        Assert.assertNull(name.getCharset());
    }
    
    // simple types without attributes or nesting
    @Test
    public void test_1() {
        Name name = parser.parse(getResource(1), Name.class);
        Assert.assertEquals(Name.Title.MR, name.getTitle());
        Assert.assertEquals("Unit", name.getFirstName());
        Assert.assertEquals("Test", name.getLastName());
        Assert.assertEquals("utf-8", name.getCharset());
    }
    
    // attributes on the top level element 
    @Test
    public void test_2() {
        Name name = parser.parse(getResource(2), Name.class);
        Assert.assertEquals(Name.Title.MR, name.getTitle());
        Assert.assertEquals("Unit", name.getFirstName());
        Assert.assertEquals("Test", name.getLastName());
        Assert.assertEquals("utf-8", name.getCharset());
    }
    
    // attributes on a simple element which is defined as a primitive/string/enum
    @Test
    public void test_3() {
        Name name = parser.parse(getResource(3), Name.class);
        Assert.assertEquals("Unit", name.getFirstName());
        Assert.assertNull(name.getLastName());
    }
    
    // attributes on a simple element which is defined as a class other than strings/enum
    @Test
    public void test_4() {
        Name name = parser.parse(getResource(4), Name.class);
        Assert.assertTrue(name.getPassword().isEncrypt());
        Assert.assertEquals("foobar", name.getPassword().getPassword());
    }
    
    // complex type with all features
    @Test
    public void test_5() {
        CreateProfileRequest req = parser.parse(getResource(5), CreateProfileRequest.class);
        Assert.assertEquals(2.0f, req.getVersion(), 0);
        Assert.assertEquals(1000, req.getId());
        Assert.assertEquals("Unit", req.getName().getFirstName());
        Assert.assertEquals("Test", req.getName().getLastName());
        Assert.assertEquals(Name.Title.MS, req.getName().getTitle());
        Assert.assertEquals("utf-8", req.getName().getCharset());
        Assert.assertFalse(req.getName().getPassword().isEncrypt());
        Assert.assertEquals("foobar", req.getName().getPassword().getPassword());
    }
}
