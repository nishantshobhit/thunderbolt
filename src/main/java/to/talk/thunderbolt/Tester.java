package to.talk.thunderbolt;

import java.io.InputStream;

public class Tester {

    public static void main(String[] args) throws Exception {
        InputStream is = Tester.class.getClassLoader().getResourceAsStream("meta_update.xml");
        StreamingParser sp = new StreamingParser();
        Iq iq = sp.parse(is, Iq.class);

    }
}
