package com.example.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String key() default ""; // Helper to identify the limit type (e.g. "user" or "global")
    double rate() default 1.0; // Tokens per second
    double capacity() default 1.0; // Max burst
    boolean isUserSpecific() default true; // If true, appends User ID to key
}
