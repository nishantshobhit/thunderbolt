package to.talk.thunderbolt;

public class Password {

    boolean encrypt;
    String password;
    
    public Password(String password) {
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
