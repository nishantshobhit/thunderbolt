package to.talk.thunderbolt;

public class HailStormException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public HailStormException(Exception cause) {
        super(cause);
    }

}
