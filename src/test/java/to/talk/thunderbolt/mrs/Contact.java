package to.talk.thunderbolt.mrs;

public class Contact {
 
    String from;
    String to;
    String subscription;
    VCard vCard;
    
    boolean verifiedMobile;
    
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public String getSubscription() {
        return subscription;
    }
    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }
    public VCard getVCard() {
        return vCard;
    }
    public void setVCard(VCard vCard) {
        this.vCard = vCard;
    }
    public boolean isVerifiedMobile() {
        return verifiedMobile;
    }
    public void setVerifiedMobile(boolean verifiedMobile) {
        this.verifiedMobile = verifiedMobile;
    }
}