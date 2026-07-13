package com.astrotech.chat.responseBuilder;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

public class ApiResponseBuilder {
    private ApiResponseBuilder() {}

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data){
        var response = new ApiResponse<T>(
                true, message, data
        );
        return ResponseEntity.ok(response);
    }
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, String path, T data , Object Id, UriComponentsBuilder uriComponentsBuilder){
        var response = new ApiResponse<T>(
                true, message, data
        );
        var uri = uriComponentsBuilder.path(path).buildAndExpand(Id).encode().toUri();
        return ResponseEntity.created(uri).body(response);
    }
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(
            String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }

    public static <T> ResponseEntity<ApiResponse<T>> deletedResponse(
            String message) {
        return error(message, HttpStatus.OK);
    }



    public static <T> ResponseEntity<ApiResponse<T>> error(
            String message,

            HttpStatus status) {

        ApiResponse<T> response = new ApiResponse<>(
                false,
                message,
                null);

        return ResponseEntity.status(status)
                .body(response);
    }
    public static <T> ResponseEntity<ApiResponse<T>> forbidden(
            String message) {
        return error(message, HttpStatus.FORBIDDEN);
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(
            String message) {
        return error(message, HttpStatus.NOT_FOUND);
    }

    public static <T> ResponseEntity<ApiResponse<T>> conflict(
            String message) {
        return error(message, HttpStatus.CONFLICT);
    }

    public static <T> ResponseEntity<ApiResponse<T>> unAuthorized() {
        return error("Not Authorized", HttpStatus.UNAUTHORIZED);
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(
            String message) {
        return error(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
