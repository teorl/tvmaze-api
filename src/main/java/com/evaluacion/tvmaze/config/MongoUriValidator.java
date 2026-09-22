package com.evaluacion.tvmaze.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Detiene el arranque con un mensaje claro si no hay cadena de conexión a MongoDB.
 * <p>
 * Sin esta validación, el placeholder {@code ${MONGODB_URI}} llega sin resolver al driver y el error es
 * "The connection string is invalid". Se registra en {@code main()}, por lo que solo corre al levantar la
 * aplicación completa y no en las pruebas.
 */
public class MongoUriValidator implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    static final String MONGO_URI_PROPERTY = "spring.data.mongodb.uri";

    static final String MISSING_URI_MESSAGE = "Falta la variable de entorno MONGODB_URI. Defínela con la cadena "
            + "de conexión de MongoDB (formato en .env.example; ver 'Configuración de MongoDB' en el README).";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        validate(event.getEnvironment());
    }

    void validate(Environment environment) {
        if (!StringUtils.hasText(resolveMongoUri(environment))) {
            throw new IllegalStateException(MISSING_URI_MESSAGE);
        }
    }

    /**
     * Devuelve {@code null} si la URI apunta a una variable de entorno que no existe.
     */
    private static String resolveMongoUri(Environment environment) {
        try {
            return environment.getProperty(MONGO_URI_PROPERTY);
        } catch (IllegalArgumentException unresolvablePlaceholder) {
            return null;
        }
    }
}
