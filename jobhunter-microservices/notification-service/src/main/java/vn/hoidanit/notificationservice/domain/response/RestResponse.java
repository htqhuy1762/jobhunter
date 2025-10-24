package vn.hoidanit.notificationservice.domain.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
public class RestResponse<T> {
    private int statusCode;
    private String error;
    private Object message;
    private T data;

    public static <T> ResponseEntity<RestResponse<T>> ok(T data, String message) {
        RestResponse<T> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage(message);
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<RestResponse<T>> created(T data, String message) {
        RestResponse<T> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.CREATED.value());
        response.setMessage(message);
        response.setData(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public static <T> ResponseEntity<RestResponse<T>> error(HttpStatus status, String message) {
        RestResponse<T> response = new RestResponse<>();
        response.setStatusCode(status.value());
        response.setError(status.getReasonPhrase());
        response.setMessage(message);
        return ResponseEntity.status(status).body(response);
    }
}

