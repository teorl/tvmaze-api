package com.evaluacion.tvmaze.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static com.evaluacion.tvmaze.config.MongoUriValidator.MISSING_URI_MESSAGE;
import static com.evaluacion.tvmaze.config.MongoUriValidator.MONGO_URI_PROPERTY;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MongoUriValidatorTest {

    private final MongoUriValidator validator = new MongoUriValidator();

    @Test
    void acceptsEnvironmentWithMongoUri() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("MONGODB_URI", "mongodb+srv://usuario:password@cluster/tvmaze")
                .withProperty(MONGO_URI_PROPERTY, "${MONGODB_URI}");

        assertThatCode(() -> validator.validate(environment)).doesNotThrowAnyException();
    }

    @Test
    void failsWithClearMessageWhenEnvironmentVariableIsMissing() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(MONGO_URI_PROPERTY, "${MONGODB_URI}");

        assertThatThrownBy(() -> validator.validate(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(MISSING_URI_MESSAGE);
    }

    @Test
    void failsWhenEnvironmentVariableIsBlank() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("MONGODB_URI", "  ")
                .withProperty(MONGO_URI_PROPERTY, "${MONGODB_URI}");

        assertThatThrownBy(() -> validator.validate(environment))
                .hasMessage(MISSING_URI_MESSAGE);
    }
}
