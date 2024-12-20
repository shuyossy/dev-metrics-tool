// package sak.metricstool.exception;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.ControllerAdvice;
// import org.springframework.web.bind.annotation.ExceptionHandler;

// import java.time.LocalDateTime;

// /**
//  * グローバルな例外ハンドラー。
//  */
// @ControllerAdvice
// public class GlobalExceptionHandler {

//     /**
//      * ResourceNotFoundException を処理します。
//      * @param ex 例外オブジェクト
//      * @return エラーレスポンス
//      */
//     @ExceptionHandler(ResourceNotFoundException.class)
//     public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
//         ErrorResponse error = new ErrorResponse(
//                 HttpStatus.NOT_FOUND.value(),
//                 ex.getMessage(),
//                 LocalDateTime.now()
//         );
//         return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
//     }

//     /**
//      * 全ての例外を処理します。
//      * @param ex 例外オブジェクト
//      * @return エラーレスポンス
//      */
//     @ExceptionHandler(Exception.class)
//     public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
//         ErrorResponse error = new ErrorResponse(
//                 HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                 "Internal Server Error",
//                 LocalDateTime.now()
//         );
//         return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
//     }
// }
