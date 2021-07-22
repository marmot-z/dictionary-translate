package priv.zxw.dictranslate.exception;

import org.springframework.beans.BeansException;

public class DictionaryWrapperClassGenerateException extends BeansException {

    public DictionaryWrapperClassGenerateException(String message) {
        super(message);
    }

    public DictionaryWrapperClassGenerateException(String message, Throwable cause) {
        super(message, cause);
    }
}
