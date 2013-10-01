package to.talk.thunderbolt.mrs;

public class MetaContact {

    String mrVersion;
    String mrId;
    Contact contact;
    
    public String getMrVersion() {
        return mrVersion;
    }
    public void setMrVersion(String mrVersion) {
        this.mrVersion = mrVersion;
    }
    public String getMrId() {
        return mrId;
    }
    public void setMrId(String mrId) {
        this.mrId = mrId;
    }
    public Contact getContact() {
        return contact;
    }
    public void setContact(Contact contact) {
        this.contact = contact;
    }
}
