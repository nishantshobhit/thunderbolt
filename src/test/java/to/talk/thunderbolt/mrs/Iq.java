package to.talk.thunderbolt.mrs;

public class Iq {
    
    String type;
    String id;
    String to;
    
    GetContacts getContacts;
    MetaContact metaContact;
    
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
    public GetContacts getGetContacts() {
        return getContacts;
    }
    public void setGetContacts(GetContacts getContacts) {
        this.getContacts = getContacts;
    }
    public MetaContact getMetaContact() {
        return metaContact;
    }
    public void setMetaContact(MetaContact metaContact) {
        this.metaContact = metaContact;
    }
    
}