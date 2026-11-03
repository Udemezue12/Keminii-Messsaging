 package com.astrotech.chat.exception_handlers;

 import org.springframework.http.converter.HttpMessageNotReadableException;
 import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
 import org.springframework.messaging.simp.annotation.SendToUser;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.HttpMediaTypeNotAcceptableException;
 import org.springframework.web.bind.MethodArgumentNotValidException;
 import org.springframework.web.bind.annotation.ControllerAdvice;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.NoHandlerFoundException;

 import java.nio.file.AccessDeniedException;
 import java.util.List;
 import java.util.Map;
 import java.util.Objects;

 @ControllerAdvice(annotations = Controller.class)
 public class MvcExceptionHandler {

//     @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
//     public ModelAndView handleBadRequest(Exception ex) {
//         ModelAndView mav = new ModelAndView();
//         mav.addObject("errorMessage", "The request was invalid or could not be understood by the server.");
//         mav.setViewName("error/400");
//         return mav;
//     }

     // 401 Unauthorized (e.g., InsufficientAuthenticationException)
 //    @ExceptionHandler(InsufficientAuthenticationException.class)
 //    public ModelAndView handleUnauthorized(InsufficientAuthenticationException ex) {
 //        ModelAndView mav = new ModelAndView();
 //        mav.addObject("errorMessage", "You are not authorized to view this resource. Please log in.");
 //        mav.setViewName("error/401");
 //        return mav;
 //    }

     // 403 Forbidden (e.g., AccessDeniedException)
//     @ExceptionHandler(AccessDeniedException.class)
//     public ModelAndView handleForbidden(AccessDeniedException ex) {
//         ModelAndView mav = new ModelAndView();
//         mav.addObject("errorMessage", "You do not have permission to access this page.");
//         mav.setViewName("error/403");
//         return mav;
//     }
//
//
//     @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
//     public ModelAndView handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
//         ModelAndView mav = new ModelAndView();
//         mav.addObject("errorMessage", "The requested format is not supported by the server.");
//         mav.setViewName("error/406");
//         return mav;
//     }
//     @ExceptionHandler(NoHandlerFoundException.class)
//     public ModelAndView handle404(NoHandlerFoundException ex) {
//         ModelAndView modelAndView = new ModelAndView();
//         modelAndView.addObject("errorMessage", "The page you are looking for does not exist.");
//         modelAndView.setViewName("error/404");
//         return modelAndView;
//     }
//
//     @ExceptionHandler(Exception.class)
//     public ModelAndView handleMvcException(Exception ex) {
//         ModelAndView modelAndView = new ModelAndView();
//         modelAndView.addObject("errorMessage", ex.getMessage());
//         modelAndView.setViewName("error/generic-error");
//         return modelAndView;
//     }
     @MessageExceptionHandler(MethodArgumentNotValidException.class)
     @SendToUser("/queue/errors")
     public Map<String, Object> handleValidationException(MethodArgumentNotValidException ex) {
         var errors = ex.getBindingResult()
                 .getFieldErrors()
                 .stream()
                 .map(FieldError::getDefaultMessage)
                 .filter(Objects::nonNull)
                 .toList();


         if (errors.isEmpty()) {
             errors = ex.getBindingResult()
                     .getGlobalErrors()
                     .stream()
                     .map(ObjectError::getDefaultMessage)
                     .filter(Objects::nonNull)
                     .toList();
         }

         return Map.of(
                 "status", "error",
                 "messages", errors.isEmpty() ? List.of("Invalid input data.") : errors
         );
     }

 }

