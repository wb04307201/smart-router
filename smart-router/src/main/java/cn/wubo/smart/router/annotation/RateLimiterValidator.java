package cn.wubo.smart.router.annotation;


import cn.wubo.smart.router.SmartRouterProperties;
import cn.wubo.smart.router.utils.ValidationUtils;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = RateLimiterValidator.Validator.class)
@Documented
public @interface RateLimiterValidator {
    String message() default "Invalid Rate Limiting properties";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<RateLimiterValidator, SmartRouterProperties.RateLimiter> {
        @Override
        public boolean isValid(SmartRouterProperties.RateLimiter rateLimiter, ConstraintValidatorContext context) {
            if (rateLimiter == null) {
                return true; // Let other validators handle null
            }

            String rateLimitingType = rateLimiter.getRateLimitingType();
            Map<String, Object> attributes = rateLimiter.getAttributes();

            return switch (rateLimitingType) {
                case "redis" -> validateRedisProperties(attributes, context);
                case "redis-cluster" -> validateRedisClusterProperties(attributes, context);
                case "redis-sentinel" -> validateRedisSentinelProperties(attributes, context);
                case "standalone" -> validateStandaloneProperties(attributes, context);
                default -> true; // Unknown lock type, let other validators handle
            };
        }

        private boolean validateRedisProperties(Map<String, Object> attributes, ConstraintValidatorContext context) {
            if (ValidationUtils.validStringAttribute(attributes, "address")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Redis address is required")
                        .addPropertyNode("attributes[address]").addConstraintViolation();
                return false;
            }

            if (ValidationUtils.validStringAttribute(attributes, "password")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Redis password is required")
                        .addPropertyNode("attributes[password]").addConstraintViolation();
                return false;
            }

            if (ValidationUtils.validIntegerTypeAndRangeAttribute(attributes, "database", integer -> IntStream.rangeClosed(0, 15).noneMatch(v -> v == integer))) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Redis database type is Integer and value between 0~15")
                        .addPropertyNode("attributes[database]").addConstraintViolation();
                return false;
            }

            return true;
        }

        private boolean validateRedisClusterProperties(Map<String, Object> attributes, ConstraintValidatorContext context) {
            if (ValidationUtils.validListStringAttribute(attributes, "nodes")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Redis cluster nodes are required")
                        .addPropertyNode("attributes[nodes]").addConstraintViolation();
                return false;
            }

            if (ValidationUtils.validStringAttribute(attributes, "password")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Redis cluster password is required")
                        .addPropertyNode("attributes[password]").addConstraintViolation();
                return false;
            }

            return true;
        }

        private boolean validateRedisSentinelProperties(Map<String, Object> attributes, ConstraintValidatorContext context) {
            if (ValidationUtils.validListStringAttribute(attributes, "nodes")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Redis sentinel nodes are required")
                        .addPropertyNode("attributes[nodes]").addConstraintViolation();
                return false;
            }

            if (ValidationUtils.validStringAttribute(attributes, "password")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Redis sentinel password is required")
                        .addPropertyNode("attributes[password]").addConstraintViolation();
                return false;
            }

            if (ValidationUtils.validStringAttribute(attributes, "masterName")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Redis sentinel masterName is required")
                        .addPropertyNode("attributes[masterName]").addConstraintViolation();
                return false;
            }

            return true;
        }

        private boolean validateStandaloneProperties(Map<String, Object> attributes, ConstraintValidatorContext context) {
            // Standalone lock doesn't require specific attributes
            return true;
        }
    }
}
