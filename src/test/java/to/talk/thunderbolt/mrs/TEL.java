package to.talk.thunderbolt.mrs;

public class TEL {
    
    boolean CELL;
    String NUMBER;
    String cc;
    
    public boolean isCELL() {
        return CELL;
    }
    public void setCELL(boolean cELL) {
        CELL = cELL;
    }
    public String getNUMBER() {
        return NUMBER;
    }
    public void setNUMBER(String nUMBER) {
        NUMBER = nUMBER;
    }
    public void setCOUNTRYCODE(String cc) {
        this.cc = cc;
    }
    public String getCOUNTRYCODE() {
        return this.cc;
    }
}

