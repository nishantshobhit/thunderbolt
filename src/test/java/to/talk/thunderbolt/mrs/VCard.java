package to.talk.thunderbolt.mrs;

public class VCard {
    
    String FN;
    TEL TEL;
    PHOTO PHOTO;
    String UID;
    
    public String getFN() {
        return FN;
    }
    public void setFN(String fN) {
        FN = fN;
    }
    public TEL getTEL() {
        return TEL;
    }
    public void setTEL(TEL tEL) {
        TEL = tEL;
    }
    public PHOTO getPHOTO() {
        return PHOTO;
    }
    public void setPHOTO(PHOTO pHOTO) {
        PHOTO = pHOTO;
    }
    public String getUID() {
        return UID;
    }
    public void setUID(String uID) {
        UID = uID;
    }
}