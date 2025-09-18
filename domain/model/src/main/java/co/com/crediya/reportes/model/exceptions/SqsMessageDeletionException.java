package co.com.crediya.reportes.model.exceptions;


public class SqsMessageDeletionException extends RuntimeException {
    
    public SqsMessageDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SqsMessageDeletionException(String message) {
        super(message);
    }
}
