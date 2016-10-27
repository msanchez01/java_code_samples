package com.spfsolutions.ioms.aspects;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
public class ExceptionFaultAspect {

    private static final Logger log = Logger.getLogger(ExceptionFaultAspect.class);
    ObjectMapper objMapper = new ObjectMapper();
    
    public ExceptionFaultAspect(){}    
   
    @AfterThrowing(pointcut = "execution(* com.spfsolutions.ioms.controllers.*.* (..))", throwing = "e")
    public void logControllerArgumentsOnError(final JoinPoint pjp, Throwable e) throws Throwable
    {
        log.error("Running " + pjp.getSignature().getName() + " with arguments: " + objMapper.writeValueAsString(pjp.getArgs()) + "caused the following exception : " + e.getLocalizedMessage());        
    }
}

