package org.nezxenka.auth.session;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.nezxenka.auth.account.Account;

@Getter
public final class PlayerSession {

    private final UUID uuid;
    private final String name;
    private final String ip;
    private volatile Account account;
    private volatile AuthState state;
    private volatile long stateChangedAt;
    private volatile long lastActivity;
    private volatile boolean authenticatedOnce;

    @Setter
    private volatile boolean bypass;

    @Getter(AccessLevel.NONE)
    private final AtomicInteger failedAttempts = new AtomicInteger();

    @Getter(AccessLevel.NONE)
    private final AtomicBoolean busy = new AtomicBoolean();

    public PlayerSession(UUID uuid, String name, String ip, Account account) {
        long now = System.currentTimeMillis();
        this.uuid = uuid;
        this.name = name;
        this.ip = ip;
        this.account = account;
        this.state = AuthState.LOADING;
        this.stateChangedAt = now;
        this.lastActivity = now;
    }

    public boolean isAuthenticated() {
        return state == AuthState.AUTHENTICATED;
    }

    public boolean isRegistered() {
        return account != null;
    }

    public synchronized void setAccount(Account account) {
        this.account = account;
    }

    public synchronized void updateAccount(UnaryOperator<Account> updater) {
        if (account != null) {
            account = updater.apply(account);
        }
    }

    public void transition(AuthState next) {
        long now = System.currentTimeMillis();
        state = next;
        stateChangedAt = now;
        lastActivity = now;
        if (next == AuthState.AUTHENTICATED) {
            authenticatedOnce = true;
        }
    }

    public void touch() {
        lastActivity = System.currentTimeMillis();
    }

    public long millisInState(long now) {
        return now - stateChangedAt;
    }

    public int registerFailedAttempt() {
        return failedAttempts.incrementAndGet();
    }

    public void resetFailedAttempts() {
        failedAttempts.set(0);
    }

    public boolean tryAcquire() {
        return busy.compareAndSet(false, true);
    }

    public void release() {
        busy.set(false);
    }
}
