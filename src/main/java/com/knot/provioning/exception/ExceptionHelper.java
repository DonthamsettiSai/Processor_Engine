package com.knot.provioning.exception;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.knot.provisioning.dto.Response;
import com.knot.provisioning.dto.ResponseMode;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.utill.ProvisionException;

/**
 * @author Sai D
 */
@RestControllerAdvice
public class ExceptionHelper {

	private static final Logger logger = LogManager.getLogger(ExceptionHelper.class);

	CacheService cache;

	Response<Object> response = null;

	@ExceptionHandler(value = RuntimeException.class)
	public ResponseEntity<Response<Object>> reponseMyException(ProvisionException e) {
		String message = "";
		if (e.getMessage().contains("ProvisionException:")) {
			message = e.getMessage().substring(e.getMessage().lastIndexOf("ProvisionException:"));
		} else {
			message = e.getMessage();
		}
		response = new Response<>(ErrorCodes.EXCEPTION, message);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler(value = { org.springframework.dao.InvalidDataAccessResourceUsageException.class })
	public ResponseEntity<Response<Object>> sqlGrammarException(Exception ex) {
		response = new Response<>(ErrorCodes.INVALID_RES_USG_EXC,
				cache.getErrorDescription(ErrorCodes.INVALID_RES_USG_EXC) + " :: " + ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler(value = { NullPointerException.class })
	public ResponseEntity<Response<Object>> nullPointerException(Exception ex) {
		response = new Response<>(ErrorCodes.NULL_POINTER_EXCEPTION,
				cache.getErrorDescription(ErrorCodes.NULL_POINTER_EXCEPTION) + " :: " + ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler(value = { NumberFormatException.class })
	public ResponseEntity<Response<Object>> numberFormatException(Exception ex) {
		response = new Response<>(ErrorCodes.NUM_FOR_EXCEPTION,
				cache.getErrorDescription(ErrorCodes.NUM_FOR_EXCEPTION) + " :: " + ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler(value = { ArrayIndexOutOfBoundsException.class })
	public ResponseEntity<Response<Object>> arrayIndexOutOfBoundsException(Exception ex) {
		response = new Response<>(ErrorCodes.ARRAY_INDEX_EXC,
				cache.getErrorDescription(ErrorCodes.ARRAY_INDEX_EXC) + " :: " + ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler(value = { DataIntegrityViolationException.class })
	public ResponseEntity<Response<Object>> dataIntegrityViolationException(Exception ex) {
		response = new Response<>(ErrorCodes.DATA_INTE_VIO_EXC,
				cache.getErrorDescription(ErrorCodes.DATA_INTE_VIO_EXC) + " :: " + ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler(value = { UnexpectedRollbackException.class })
	public ResponseEntity<Response<Object>> dataPersistenceException(Exception ex) {
		response = new Response<>(ErrorCodes.EXCEPTION, ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler(value = { Exception.class })
	public ResponseEntity<Response<Object>> handleException(Exception ex) {
		logger.info("The Start ExceptionHelper :: ExceptionHelper", ex);
		response = new Response<>(ErrorCodes.EXCEPTION, ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler(value = { MethodArgumentNotValidException.class })
	public ResponseEntity<Response<Object>> handleValidationException(MethodArgumentNotValidException ex) {
		logger.info("The Start ExceptionHelper :: ExceptionHelper", ex);
		String defaultMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
		response = new Response<>(ErrorCodes.METHOD_ARUGUMENT_EXCEPTION, defaultMessage);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler(value = { ProvisioningException.class })
	public ResponseEntity<Object> reponseMyException(ProvisioningException ex) {
		logger.info("The Start ExceptionHelper :: ExceptionHelper", ex);
		ResponseMode responseMode = new ResponseMode();
		responseMode.setStatusCode(ErrorCodes.CUSTOM_EXCPTION);
		responseMode.setStatusMessage(ex.getMessage());
		return new ResponseEntity<>(responseMode, HttpStatus.OK);
	}

	@ExceptionHandler(value = { SQLException.class })
	public ResponseEntity<Object> responseSqlException(SQLException ex) {
		logger.error("SQL Exception occurred in ExceptionHelper", ex);
		ResponseMode responseMode = new ResponseMode();
		responseMode.setStatusCode(ErrorCodes.CUSTOM_EXCPTION);
		responseMode.setStatusMessage(ex.getMessage());
		return new ResponseEntity<>(responseMode, HttpStatus.OK);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Object> responseConstraintViolationException(
	        ConstraintViolationException ex) {

	    logger.error("Hibernate ConstraintViolationException occurred", ex);

	    ResponseMode responseMode = new ResponseMode();
	    responseMode.setStatusCode(ErrorCodes.CUSTOM_EXCPTION);
	    responseMode.setStatusMessage("Database constraint violation");

	    return new ResponseEntity<>(responseMode, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	

}
