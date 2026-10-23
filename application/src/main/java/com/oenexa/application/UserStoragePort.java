package com.oenexa.application;

/**
 * Outbound Port for User persistence.
 * Follows Hexagonal Architecture: core application logic is completely decoupled from JPA.
 * Canonical port definition is located in {@link com.oenexa.application.port.UserStoragePort}.
 */
public interface UserStoragePort extends com.oenexa.application.port.UserStoragePort {
}
