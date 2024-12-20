package sak.metricstool.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * リソースが見つからない場合にスローされる例外。
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
