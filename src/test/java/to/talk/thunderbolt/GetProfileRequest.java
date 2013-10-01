package to.talk.thunderbolt;
import java.util.ArrayList;
import java.util.List;


public class GetProfileRequest {

    private List<String> ids = new ArrayList<String>();

    public String getId() {
        return null;
    }

    public void setId(String id) {
        ids.add(id);
    }
    
    public List<String> getIds() {
        return ids;
    }
}
