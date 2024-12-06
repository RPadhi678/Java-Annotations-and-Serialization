package javacson;

public class CsonSerializationError extends RuntimeException {
    public CsonSerializationError(String message) {
        super(message);
    }
}
