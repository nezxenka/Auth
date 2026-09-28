package org.nezxenka.auth.telegram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TelegramText {
    START("start"),
    LINK_USAGE("link_usage"),
    LINK_PLAYER_NOT_FOUND("link_player_not_found"),
    LINK_ALREADY_LINKED("link_already_linked"),
    LINK_TELEGRAM_ALREADY_LINKED("link_telegram_already_linked"),
    LINK_CODE("link_code"),
    LINK_SUCCESS("link_success"),
    UNLINK_NOT_LINKED("unlink_not_linked"),
    UNLINK_SUCCESS("unlink_success"),
    INFO_MESSAGE("info_message"),
    INFO_ONLINE("info_online"),
    INFO_OFFLINE("info_offline"),
    INFO_NOT_LINKED("info_not_linked"),
    TWO_FACTOR_REQUEST("2fa_request"),
    TWO_FACTOR_APPROVED("2fa_approved"),
    TWO_FACTOR_DECLINED("2fa_declined"),
    TWO_FACTOR_KICK("2fa_kick"),
    ERROR("error"),
    NOTIFICATION_JOIN("notifications.join"),
    NOTIFICATION_LEAVE("notifications.leave"),
    BUTTON_APPROVE("buttons.approve"),
    BUTTON_DECLINE("buttons.decline"),
    BUTTON_UNLINK("buttons.unlink"),
    BUTTON_INFO("buttons.info"),
    BUTTON_INSTRUCTION("buttons.instruction");

    private final String key;
}
