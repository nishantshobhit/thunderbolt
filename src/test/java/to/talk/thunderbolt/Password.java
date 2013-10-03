package to.talk.thunderbolt;

public class Password {

    boolean encrypt;
    String password;
    
    public Password() {
        
    }
    
    public void setValue(String password) {
        this.password = password;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
