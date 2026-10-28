package io.github.bubalehich.events.interfaces.rest;

import io.github.bubalehich.events.domain.event.exception.EventNotFoundException;
import io.github.bubalehich.events.domain.event.exception.EventRuleViolationException;
import io.github.bubalehich.events.domain.event.exception.InvalidEventDataException;
import io.github.bubalehich.events.domain.event.exception.RegistrationNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ EventNotFoundException.class, RegistrationNotFoundException.class })
	public ProblemDetail handleNotFound(RuntimeException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(EventRuleViolationException.class)
	public ProblemDetail handleRuleViolation(EventRuleViolationException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(InvalidEventDataException.class)
	public ProblemDetail handleInvalidData(InvalidEventDataException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ProblemDetail handleConcurrentModification() {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"Event was modified concurrently, please retry");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<String> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList();
		return validationProblem(exception, errors, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(
			HandlerMethodValidationException exception, HttpHeaders headers, HttpStatusCode status,
			WebRequest request) {
		List<String> errors = exception.getParameterValidationResults().stream()
				.flatMap(result -> result.getResolvableErrors().stream()
						.map(error -> result.getMethodParameter().getParameterName() + ": "
								+ error.getDefaultMessage()))
				.toList();
		return validationProblem(exception, errors, headers, status, request);
	}

	private ResponseEntity<Object> validationProblem(Exception exception, List<String> errors, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "Request validation failed");
		problem.setProperty("errors", errors);
		return handleExceptionInternal(exception, problem, headers, status, request);
	}

}
