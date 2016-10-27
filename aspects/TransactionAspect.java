package com.spfsolutions.ioms.aspects;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

import com.j256.ormlite.misc.TransactionManager;
import com.spfsolutions.ioms.exceptions.CustomException;

@Aspect
public class TransactionAspect {
    
    public TransactionAspect(){}
    
    private Object returnedObject;
    
    @Autowired
    TransactionManager transactionManager;    
    
    @Around("@annotation(com.spfsolutions.ioms.annotations.Transactional)")
    public Object triggerTransactionManager(final ProceedingJoinPoint pjp) throws Throwable
    {
        
        try
        {
            
            transactionManager.callInTransaction(new Callable<Void>() {
                public Void call() throws Exception {
                    
                        try {
                            returnedObject = pjp.proceed();
                        } catch (Throwable e) {
                            
                            if (e.getClass() == SQLException.class)
                            {
                                throw new SQLException(e);
                            }
                            
                            throw new Exception(e);
                        }
                            
                        return null;
                                        
            }
            });
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();  
            if(e.getCause() != null && e.getCause().getCause() != null){
                throw e.getCause().getCause();
            }
            else{
            throw new CustomException(e.getMessage(), e);}
        }
        return returnedObject;
    }
}
