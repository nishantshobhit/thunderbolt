package to.talk.thunderbolt.mrs;

public abstract class Iq {
    
    String type;
    String id;
    String to;
    String from;
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public String getFrom() { 
        return this.from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    
}