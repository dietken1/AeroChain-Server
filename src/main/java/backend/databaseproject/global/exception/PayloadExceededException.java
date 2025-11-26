package backend.databaseproject.global.exception;

/**
 * 드론 적재량 초과 예외
 */
public class PayloadExceededException extends RuntimeException {
    public PayloadExceededException(String message) {
        super(message);
    }
}
