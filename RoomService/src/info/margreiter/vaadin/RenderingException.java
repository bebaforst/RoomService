package info.margreiter.vaadin;



public class RenderingException extends Exception {
    public RenderingException(String string) {
        super(string);
    }

    public RenderingException(Throwable e) {
        super(e);
    }

    public RenderingException() {
    }

    public RenderingException(String string, Throwable cause) {
        super(string, cause);
    }
}
