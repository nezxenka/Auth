package org.nezxenka.auth.telegram.api.model;

import lombok.Getter;

@Getter
public final class Message {

    private long messageId;
    private User from;
    private Chat chat;
    private String text;
}
