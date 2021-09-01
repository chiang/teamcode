package io.teamcode.service;

import io.teamcode.TeamcodeException;
import io.teamcode.common.TeamcodeConstants;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.NotificationEmails;
import io.teamcode.domain.entity.User;
import io.teamcode.domain.entity.UserEmail;
import io.teamcode.repository.UserEmailRepository;
import io.teamcode.repository.UserRepository;
import io.teamcode.web.ui.view.ChangePasswordForm;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 사용자가 자신의 정보를 관리할 수 있는 기능을 제공하는 서비스.
 */
@Service
@Transactional(readOnly = true)
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserEmailRepository userEmailRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    public User get() {

        return userService.getCurrentUser();
    }

    //TODO policy!
    @Transactional
    public void changePassword(final ChangePasswordForm changePasswordForm) {
        User currentUser;
        if (StringUtils.hasText(changePasswordForm.getUserName())) {
            currentUser = userService.get(changePasswordForm.getUserName());
        }
        else {
            currentUser = userService.getCurrentUser();
        }

        if (!passwordEncoder.matches(changePasswordForm.getCurrentPassword(), currentUser.getPassword()))
            throw new InvalidCurrentPasswordException();

        currentUser.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
        currentUser.setUpdatedAt(new Date());
        currentUser.setUpdatedBy(currentUser);
        currentUser.setLastPasswordModifiedAt(new Date());

        userRepository.save(currentUser);
        logger.info("사용자 '{}'이(가) 요청한 비밀번호 변경 요청을 처리했습니다.", currentUser.getName());
    }

    /**
     *
     * @param user
     * @param multipartFile 이 값은 <code>null</code> 을 허용합니다. <code>null</code> 인 경우는 이미지를 변경하지 않은 경우입니다.
     * @return
     */
    @Transactional
    public User update(User user, MultipartFile multipartFile) {
        Assert.notNull(user);

        User currentUser = userService.getCurrentUser();

        currentUser.setEmail(user.getEmail());
        currentUser.setFullName(user.getFullName());
        currentUser.setOrganization(user.getOrganization());
        currentUser.setBio(user.getBio());
        currentUser.setUpdatedAt(new Date());
        currentUser.setUpdatedBy(userService.getCurrentUser());

        User updatedUser = userRepository.save(currentUser);

        if (multipartFile != null && !multipartFile.isEmpty()) {
            logger.debug("사용자 아바타 파일을 전달받았습니다. 파일 저장을 시작합니다...");
            if (multipartFile.getSize() > TeamcodeConstants.MAX_UPLOAD_AVATAR_SIZE) {
                throw new MaxUploadSizeExceededException(TeamcodeConstants.MAX_UPLOAD_AVATAR_SIZE);
            }

            String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
            File avatarsDir = new File(tcConfig.getAttachmentsDir(), "users");
            File currentAvatarDir = new File(avatarsDir, String.valueOf(updatedUser.getId()));

            if (!currentAvatarDir.exists()) {
                if (!currentAvatarDir.mkdirs())
                    throw new TeamcodeException(String.format("디렉터리를 만들 수 없습니다! %s", currentAvatarDir.getAbsolutePath()));
            }

            File avatarFile = new File(currentAvatarDir, String.format("avatar.%s", extension));
            try {
                multipartFile.transferTo(avatarFile);

                updatedUser.setAvatarPath(String.format("users/%s/%s", updatedUser.getId(), avatarFile.getName()));
                userRepository.save(updatedUser);
            } catch (IOException e) {
                logger.error("사용자 아바타 파일을 저장하던 중 오류가 발생했습니다.", e);

                throw new TeamcodeException("사용자 아바타 파일을 저장하던 중 오류가 발생했습니다.", e);
            }
        }

        logger.info("사용자 '{}' 의 정보를 업데이트했습니다. 세션 정보도 함께 업데이트합니다.", updatedUser.getName());

        User sessionUser = userService.getSessionUser();
        sessionUser.setAvatarPath(updatedUser.getAvatarPath());
        sessionUser.setFullName(updatedUser.getFullName());

        return updatedUser;
    }

    public List<UserEmail> getEmails() {

        return userEmailRepository.findByUser(userService.getCurrentUser());
    }

    @Transactional
    public void addEmail(String email) {
        if(userEmailRepository.countByEmail(email) > 0)
            throw new EmailAlreadyExistException();

        User currentUser = userService.getCurrentUser();
        if (email.equals(currentUser.getEmail()))
            throw new EmailAlreadyExistException();

        UserEmail userEmail = new UserEmail();
        userEmail.setUser(currentUser);
        userEmail.setEmail(email);
        userEmail.setCreatedAt(new Date());

        userEmailRepository.save(userEmail);
    }

    @Transactional
    public void removeEmail(final Long emailId) {
        UserEmail userEmail = userEmailRepository.findOne(emailId);
        if (userEmail == null) {
            logger.warn("존재하지 않는 사용자 이메일 정보에 대한 삭제 요청입니다. 요청을 무시합니다.");
            return;
        }

        User currentUser = userService.getCurrentUser();
        if (currentUser.getId().longValue() == userEmail.getUser().getId().longValue()) {
            userEmailRepository.delete(userEmail);
            if (StringUtils.hasText(currentUser.getNotificationEmail())) {
                if (currentUser.getNotificationEmail().equals(userEmail.getEmail())) {
                    currentUser.setNotificationEmail(currentUser.getEmail());
                    userRepository.save(currentUser);
                    userService.getSessionUser().setNotificationEmail(currentUser.getNotificationEmail());
                    logger.info("삭제할 사용자 이메일이 알림 메일로 설정되어 있습니다. 기본 이메일을 알림 메일로 변경했습니다.");
                }
            }

            logger.info("사용자 '{}' 의 이메일 정보 '{}' 를 삭제했습니다.", userEmail.getUser().getName(), userEmail.getEmail());
        }
        else {
            //TODO custom error
            throw new TeamcodeException("현재 로그인한 사용자가 아닌 사용자가 정보 변경을 시도했습니다.");
        }
    }

    public NotificationEmails getNotificationEmails() {
        List<String> emails = new ArrayList<>();
        User currentUser = userService.getCurrentUser();
        emails.add(currentUser.getEmail());

        List<UserEmail> userEmails = userEmailRepository.findByUser(currentUser);
        userEmails.stream().forEach(u -> emails.add(u.getEmail()));

        NotificationEmails notificationEmailsViewModel = new NotificationEmails();
        notificationEmailsViewModel.setCurrentNotificationEmail(currentUser.getNotificationEmail());
        notificationEmailsViewModel.setUserEmails(emails);

        return notificationEmailsViewModel;
    }

    @Transactional
    public void patch(User user) {
        User currentUser = userService.getCurrentUser();

        user.setId(currentUser.getId());
        User patchedUser = userService.patch(user);
        logger.info("사용자 '{}' 의 설정을 업데이트했습니다.", currentUser.getName());

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        attr.setAttribute("currentUser", patchedUser, ServletRequestAttributes.SCOPE_SESSION);
    }

}
