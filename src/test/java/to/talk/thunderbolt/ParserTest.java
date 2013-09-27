package to.talk.thunderbolt;

import java.io.InputStream;

import org.junit.Assert;

import org.junit.Test;

import to.talk.thunderbolt.Name.Title;

public class ParserTest {

    private Parser parser = new Parser();
    
    @Test
    public void test_1() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("to/talk/thunderbolt/CreateProfileRequest.xml");
        CreateProfileRequest req = 
                parser.parse(stream, CreateProfileRequest.class);
        Assert.assertEquals(1000, req.getId());
        Assert.assertEquals(2.0f, req.getVersion(), 0f);
        Assert.assertEquals("utf-8", req.getName().getCharset());
        Assert.assertEquals(Title.MR, req.getName().getTitle());
        Assert.assertEquals("Nishant", req.getName().getFirstName());
        Assert.assertEquals("S", req.getName().getLastName());
    }
    
}
