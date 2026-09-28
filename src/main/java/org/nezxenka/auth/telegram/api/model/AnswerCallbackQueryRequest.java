package org.nezxenka.auth.telegram.api.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class AnswerCallbackQueryRequest {

    String callbackQueryId;
}
