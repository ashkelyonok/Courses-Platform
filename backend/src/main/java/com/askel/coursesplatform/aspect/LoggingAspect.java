package com.askel.coursesplatform.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("within(com.askel.coursesplatform.service..*) || within(com.askel.coursesplatform.controller..*)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (logger.isDebugEnabled()) {
            String argsString = safeArgsToString(joinPoint.getArgs());
            logger.debug("Entering: {} with arguments = {}",
                    joinPoint.getSignature().toShortString(), argsString);
        }
        Object result = joinPoint.proceed();
        if (logger.isDebugEnabled()) {
            String resultString = safeObjectToString(result);
            logger.debug("Exiting: {} with result = {}",
                    joinPoint.getSignature().toShortString(), resultString);
        }
        return result;
    }

    @AfterThrowing(pointcut = "within(com.askel.coursesplatform.service..*) || within(com.askel.coursesplatform.controller..*)", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodSignature = joinPoint.getSignature().toShortString();
        String argsString = safeArgsToString(joinPoint.getArgs());
        logger.error("Exception in {} with arguments {}: {}",
                methodSignature, argsString, ex.getMessage(), ex);
    }

    private String safeArgsToString(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            sb.append(safeObjectToString(args[i]));
            if (i < args.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String safeObjectToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getSimpleName() + "@" + System.identityHashCode(obj);
    }
}