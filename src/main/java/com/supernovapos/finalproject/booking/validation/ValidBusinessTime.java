package com.supernovapos.finalproject.booking.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BusinessTimeValidator.class)
@Documented
public @interface ValidBusinessTime {
	String message( ) default "Time must be in 30-minute intervals";
	Class<?>[]groups() default{};
	Class<? extends Payload>[] payload() default{};
}
