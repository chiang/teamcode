package io.teamcode.dto;

import io.teamcode.domain.entity.User;
import lombok.Data;

import java.util.List;

/**
 * Created by chiang on 2017. 2. 7..
 */
@Data
public class UserListView {

    private int activeUsersCount;

    private int adminsCount;

    private int blockedUsersCount;

    private String filter;

    private List<User> users;
}
