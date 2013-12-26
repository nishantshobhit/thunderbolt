package to.talk.thunderbolt;

public class Iq {

    String id;
    String from;
    String to;
    String type;

    MetaDelete metaDelete;
    MetaUpdate metaUpdate;
    MetaPhoneUpdate metaPhoneUpdate;

    public void setId(String id) {
        this.id = id;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMetaDelete(MetaDelete metaDelete) {
        this.metaDelete = metaDelete;
    }

    public void setMetaPhoneUpdate(MetaPhoneUpdate metaPhoneUpdate) {
        this.metaPhoneUpdate = metaPhoneUpdate;
    }

    public void setMetaUpdate(MetaUpdate metaUpdate) {
        this.metaUpdate = metaUpdate;
    }
}
