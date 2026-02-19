package com.example.antishake.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AntiDuplicateSubmit {
    
    /**
     * Timeout for the lock (anti-shake time)
     * Default: 1 second
     */
    long timeout() default 1;
    
    /**
     * Time unit
     * Default: Seconds
     */
    TimeUnit unit() default TimeUnit.SECONDS;
    
    /**
     * Lock Strategy Type
     */
    LockType type() default LockType.REDIS_LUA;

    /**
     * Error message when duplicate submission is detected
     */
    String message() default "Request is too frequent, please try again later!";
    
    enum LockType {
        REDIS_LUA,
        REDISSON,
        LOCAL
    }
}
