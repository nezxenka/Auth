package org.nezxenka.auth.session;

public enum AuthState {
    LOADING,
    UNREGISTERED,
    UNAUTHENTICATED,
    PENDING_TWO_FACTOR,
    AUTHENTICATED
}
