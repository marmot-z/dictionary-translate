package priv.zxw.dictranslate.exception;

public class IllegalDictionaryUsageException extends RuntimeException {

    public IllegalDictionaryUsageException(String message) {
        super(message);
    }

    public IllegalDictionaryUsageException(String message, Exception e) {
        super(message, e);
    }
}
