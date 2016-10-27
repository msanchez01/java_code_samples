package com.spfsolutions.ioms.aspects;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
public class RequestLoggerAspect {

    private static final Logger log = Logger.getLogger(RequestLoggerAspect.class);
    private Object returnedObject;
    ObjectMapper objMapper = new ObjectMapper();
    
    public RequestLoggerAspect(){}
    
    @Around("@annotation(com.spfsolutions.ioms.annotations.LogRequest)")
    public Object logRequest(final ProceedingJoinPoint pjp) throws Throwable
    {
        log.info("Running " + pjp.getSignature().getName() + " with arguments: " + objMapper.writeValueAsString(pjp.getArgs()));;
        returnedObject = pjp.proceed();
        
        log.info("Processed " + pjp.getSignature().getName() + " successfully. Response: " + objMapper.writeValueAsString(returnedObject));
            
        return returnedObject;
    }
}
