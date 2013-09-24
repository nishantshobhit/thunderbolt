package to.talk.thunderbolt;

public class CreateProfileRequest {

    int id;
    float version;
    Name name;
    Boolean verify;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public float getVersion() {
        return version;
    }
    public void setVersion(float version) {
        this.version = version;
    }
    public Name getName() {
        return name;
    }
    public void setName(Name name) {
        this.name = name;
    }
    public Boolean getVerify() {
        return verify;
    }
    public void setVerify(Boolean verify) {
        this.verify = verify;
    }
}
