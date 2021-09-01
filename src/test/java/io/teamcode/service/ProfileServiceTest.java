package io.teamcode.service;

import io.teamcode.domain.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by chiang on 2017. 2. 1..
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@Transactional
public class ProfileServiceTest {

    @Autowired
    ProfileService profileService;

    @Autowired
    UserService userService;

    @WithUserDetails("admin")
    @Test(expected = InsufficientPrivilegeException.class)
    public void updateError() {
        User anotherUser = userService.get("chiang");
        anotherUser.setFullName("Superhero");

        profileService.update(anotherUser, null);
    }

    @WithUserDetails("chiang")
    public void update() {
        User user = userService.get("chiang");
        user.setFullName("Superhero");

        User updatedUser = profileService.update(user, null);
        Assert.assertEquals(user.getFullName(), updatedUser.getFullName());
    }

    //TODO
    @WithUserDetails("chiang")
    @Test
    public void changePasswordWithShortLength() {
        profileService.changePassword(null);
    }
}
