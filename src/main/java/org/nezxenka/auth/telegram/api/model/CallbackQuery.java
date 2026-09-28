package org.nezxenka.auth.telegram.api.model;

import lombok.Getter;

@Getter
public final class CallbackQuery {

    private String id;
    private User from;
    private Message message;
    private String data;
}
