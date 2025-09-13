package co.com.crediya.reportes.model.exceptions;

public class EmailTemplateException extends RuntimeException {
    
    public EmailTemplateException(String message) {
        super(message);
    }
    
    public EmailTemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}
