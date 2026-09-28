package org.nezxenka.auth.telegram.api.model;

import lombok.Getter;

@Getter
public final class Update {

    private long updateId;
    private Message message;
    private CallbackQuery callbackQuery;
}
