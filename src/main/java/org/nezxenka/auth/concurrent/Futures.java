package org.nezxenka.auth.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Futures {

    public Throwable unwrap(Throwable error) {
        Throwable current = error;
        while (
            (current instanceof CompletionException || current instanceof ExecutionException) &&
            current.getCause() != null
        ) {
            current = current.getCause();
        }
        return current;
    }

    public <T> CompletableFuture<T> logFailure(CompletableFuture<T> future, Logger logger, String action) {
        return future.whenComplete((result, error) -> {
            if (error != null) {
                logger.log(Level.SEVERE, action, unwrap(error));
            }
        });
    }
}
