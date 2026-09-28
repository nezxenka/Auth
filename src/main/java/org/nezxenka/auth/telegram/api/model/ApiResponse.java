package org.nezxenka.auth.telegram.api.model;

import lombok.Getter;

@Getter
public final class ApiResponse<T> {

    private boolean ok;
    private T result;
    private String description;
}
