package to.talk.thunderbolt.mrs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import to.talk.thunderbolt.StreamingParser;

public class MrsTest {

    private StreamingParser parser = new StreamingParser();
    private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder builder;
    
    @BeforeClass
    public static void setup() throws Exception {
        builder = factory.newDocumentBuilder();
    }
    
    protected InputStream getResource(int id) {
        return getClass().getClassLoader().getResourceAsStream("mrs/test" + id + ".xml");
    }
    
    @Test
    public void test_1() {
        parser.parse(getResource(1), GetContactsIq.class);
    }
    
    @Test
    public void test_2() {
        parser.parse(getResource(2), MetaContactIq.class);
    }
    
    @Test
    public void test_3_large() throws Exception {
        System.out.println("STAX");
        System.out.println("---------");
        String file = FileUtils.readFileToString(new File("src/test/resources/mrs/test3.xml"));
        
        for (int i=0; i<2000; i++) {
            InputStream is = new ByteArrayInputStream(file.getBytes());
            parser.parse(is, AddContactsIq.class);
        }
        double avg = 0.0;
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for (int i=1; i<=1000; i++) {
            InputStream is = new ByteArrayInputStream(file.getBytes());
            long start = System.nanoTime();
            parser.parse(is, AddContactsIq.class);
            long end = System.nanoTime();
            avg = (avg*(i-1) + (end-start)) / i;
            min = ((end-start) <= min) ? end-start : min;
            max = ((end-start) > max) ? end-start : max;
        }
        // micros
        System.out.println("Min:" + min/1000.0);
        System.out.println("Max:" + max/1000.0);
        System.out.println("Average:" + avg/1000.0);
        System.out.println("---------");
    }
    
    @Test
    public void test4_large() throws Exception {
        System.out.println("DOM");
        System.out.println("---------");
        String file = FileUtils.readFileToString(new File("src/test/resources/mrs/test3.xml"));
        for (int i=0; i<2000; i++) {
            InputStream is = new ByteArrayInputStream(file.getBytes());
            builder.parse(is);
        }
        
        double avg = 0.0;
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for (int i=1; i<=1000; i++) {
            InputStream is = new ByteArrayInputStream(file.getBytes());
            long start = System.nanoTime();
            Document doc = builder.parse(is);
            builder.reset();
            doc.getElementsByTagName("vCard");
            long end = System.nanoTime();
            avg = (avg*(i-1) + (end-start)) / i;
            min = ((end-start) <= min) ? end-start : min;
            max = ((end-start) > max) ? end-start : max;
        }
        System.out.println("Min:" + min/1000.0);
        System.out.println("Max:" + max/1000.0);
        System.out.println("Average:" + avg/1000.0);
        System.out.println("---------");
    }
}
