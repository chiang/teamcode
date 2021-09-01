package io.teamcode.domain.entity;

/**
 * Object 삭제 시 Undo 기능을 구현하는 경우 사용하는 것.
 */
public enum EntityState {

    CREATED, MODIFIED, DELETED

}
