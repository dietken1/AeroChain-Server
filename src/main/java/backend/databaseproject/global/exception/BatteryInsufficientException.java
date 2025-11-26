package backend.databaseproject.global.exception;

/**
 * 배터리 용량 부족 예외
 */
public class BatteryInsufficientException extends RuntimeException {
    public BatteryInsufficientException(String message) {
        super(message);
    }
}
