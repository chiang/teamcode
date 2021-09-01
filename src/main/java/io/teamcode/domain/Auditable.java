package io.teamcode.domain;

/**
 * Created by chiang on 2017. 1. 25..
 */
public interface Auditable {

    Long getId();

    AuditableType getAuditableType();
}
