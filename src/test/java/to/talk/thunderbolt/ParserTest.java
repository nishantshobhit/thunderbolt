package to.talk.thunderbolt;

import java.io.ByteArrayInputStream;

import org.junit.Assert;

import org.junit.Test;

public class ParserTest {

    private Parser parser = new Parser();
    
    @Test
    public void test_1() {
        String xml = 
                "<create-profile>" +
                  "<id>1000</id>" +
                  "<version>4.9</version>" +
                  "<name>" +
                    "<title>MR</title>" +
                    "<first-name>Nishant</first-name>" +
                    "<last-name>S</last-name>" +
                  "</name>" +
                  "<verify/>" +
                "</create-profile>";
        CreateProfileRequest req = 
                parser.parse(new ByteArrayInputStream(xml.getBytes()), CreateProfileRequest.class);
        Assert.assertEquals(1000, req.getId());
        Assert.assertEquals(4.9f, req.getVersion(), 0f);
        Assert.assertEquals("MR", req.getName().getTitle());
        Assert.assertEquals("Nishant", req.getName().getFirstName());
        Assert.assertEquals("S", req.getName().getLastName());
    }
    
}
