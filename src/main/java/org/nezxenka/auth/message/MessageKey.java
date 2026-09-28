package org.nezxenka.auth.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageKey {
    LOGIN_SUCCESS("login_success"),
    REGISTER_SUCCESS("register_success"),
    ALREADY_LOGGED_IN("already_logged_in"),
    NOT_LOGGED_IN("not_logged_in"),
    NOT_REGISTERED("not_registered"),
    ALREADY_REGISTERED("already_registered"),
    ALREADY_ONLINE("already_online"),
    WRONG_PASSWORD("wrong_password"),
    PASSWORD_MISMATCH("password_mismatch"),
    PASSWORD_TOO_SHORT("password_too_short"),
    PASSWORD_TOO_LONG("password_too_long"),
    SESSION_EXPIRED("session_expired"),
    ADMIN_RELOAD("admin_reload"),
    ADMIN_RELOAD_FAILED("admin_reload_failed"),
    ADMIN_HELP_HEADER("admin_help_header"),
    ADMIN_HELP_RELOAD("admin_help_reload"),
    TIMEOUT_REACHED("timeout_reached"),
    MAX_ATTEMPTS("max_attempts"),
    FORCE_CHANGED("force_changed"),
    SUCCESSFULLY_CHANGED("successfully_changed"),
    USAGE_CHANGE("usage_change"),
    USAGE_ADMIN_CHANGE("usage_admin_change"),
    PLAYER_NOT_FOUND("player_not_found"),
    NO_PERMISSION("no_permission"),
    ONLY_PLAYERS("only_players"),
    PROCESSING("processing"),
    DATA_LOADING("data_loading"),
    DATABASE_ERROR("database_error"),
    REMINDER_LOGIN("reminder_login"),
    REMINDER_REGISTER("reminder_register"),
    TWO_FACTOR_WAITING("2fa_waiting"),
    TWO_FACTOR_TIMEOUT("2fa_timeout"),
    TELEGRAM_LINK_USAGE("telegram_link_usage"),
    TELEGRAM_LINK_HINT("telegram_link_hint"),
    TELEGRAM_LINKED("telegram_linked"),
    TELEGRAM_LINKED_TWO_FACTOR("telegram_linked_2fa"),
    TELEGRAM_ALREADY_LINKED("telegram_already_linked"),
    TELEGRAM_INVALID_CODE("telegram_invalid_code"),
    TELEGRAM_NO_PENDING("telegram_no_pending"),
    TELEGRAM_ID_TAKEN("telegram_id_taken"),
    TELEGRAM_DISABLED("telegram_disabled");

    private final String path;
}
