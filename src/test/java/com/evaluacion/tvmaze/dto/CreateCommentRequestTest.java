package com.evaluacion.tvmaze.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateCommentRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3, 5})
    void acceptsRatingsFromZeroToFive(int rating) {
        assertThat(validator.validate(new CreateCommentRequest("Buena serie", rating))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 6})
    void rejectsRatingsOutOfRange(int rating) {
        assertThat(invalidFields(new CreateCommentRequest("Buena serie", rating))).containsExactly("rating");
    }

    @Test
    void rejectsMissingRating() {
        assertThat(invalidFields(new CreateCommentRequest("Buena serie", null))).containsExactly("rating");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlankComment(String comment) {
        assertThat(invalidFields(new CreateCommentRequest(comment, 4))).containsExactly("comment");
    }

    @Test
    void acceptsCommentOfExactlyMaxLength() {
        assertThat(validator.validate(new CreateCommentRequest("a".repeat(500), 4))).isEmpty();
    }

    @Test
    void rejectsCommentLongerThanMaxLength() {
        assertThat(invalidFields(new CreateCommentRequest("a".repeat(501), 4))).containsExactly("comment");
    }

    private Set<String> invalidFields(CreateCommentRequest request) {
        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);
        return Set.copyOf(violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .toList());
    }
}
