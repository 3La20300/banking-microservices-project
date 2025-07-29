package com.banking.transactionservice.aspect;

import com.banking.transactionservice.service.KafkaLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger= LoggerFactory.getLogger(LoggingAspect.class);
    @Autowired
    private KafkaLoggingService kafkaLoggingService;

    @Around("execution(* com.banking.transactionservice.controller.*.*(..))")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable{

        //Get HTTP request details
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes == null) {
            return joinPoint.proceed();
        }
        //Set Request Attributes
        HttpServletRequest request= attributes.getRequest(); //get or post?
        String endpoint= request.getRequestURI();
        String method= request.getMethod();

        //log request
        Object[] args = joinPoint.getArgs();
        Object requestBody= args.length > 0 ? args[0] : null;

        kafkaLoggingService.logRequest(endpoint, method, requestBody);

        try{
            Object result = joinPoint.proceed();

            int statusCode=200;
            Object responseBody =result;

            if(result instanceof ResponseEntity<?>)
            {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
                statusCode=responseEntity.getStatusCodeValue();
                responseBody=responseEntity.getBody();
            }
            kafkaLoggingService.logResponse(endpoint, method, responseBody, statusCode);
            return result;

        } catch (Exception e) {
            kafkaLoggingService.logResponse(endpoint, method, "Error"+ e.getMessage(), 500);
            throw e ; //tells app to stop as there is exception
        }

    }
}
