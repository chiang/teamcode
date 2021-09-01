package io.teamcode.common.io;

import io.teamcode.TeamCodeServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.Collections;
import java.util.Set;

import static java.nio.file.FileVisitResult.*;

/**
 * 팀코드 서버에서 관리하는 파일들에 대한 권한 관리 기능 모음.
 *
 * Created by chiang on 2017. 4. 5..
 */
public abstract class TeamcodePermissionHelper {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TeamCodeServer.class);

    private static final String TEAMCODE_USER_NAME = "teamcode";


    public static final void setDefaultOwner(final File file) throws IOException {
        UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
        UserPrincipal tcUserPrincipal = lookupService.lookupPrincipalByName(TEAMCODE_USER_NAME);

        Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setOwner(tcUserPrincipal);
    }

    public static final void validateRepositoryDirectoryThenSetOwner(final File repositoryDir) throws IOException {
        UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
        UserPrincipal tcUserPrincipal = lookupService.lookupPrincipalByName(TEAMCODE_USER_NAME);

        PosixFileAttributeView fileAttributeView = Files.getFileAttributeView(repositoryDir.toPath(), PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        //FIXME 루트만 체크하면 되는가?
        if (!fileAttributeView.getOwner().getName().equals(tcUserPrincipal.getName())) {
        //if (!fileAttributeView.getOwner().equals(tcUserPrincipal)) {
            logger.warn("저장소 '{}' 의 소유자 권한이 올바르게 설정되어 있지 않습니다. 권한을 변경합니다...");

            TreeVisitor visitor = new TreeVisitor(tcUserPrincipal);
            Set<FileVisitOption> opts = Collections.emptySet();
            int maxDepth = Integer.MAX_VALUE;
            Files.walkFileTree(repositoryDir.toPath(), opts, maxDepth, visitor);
        }
    }

    static class TreeVisitor implements FileVisitor<Path> {
        private final UserPrincipal tcUserPrincipal;

        TreeVisitor(UserPrincipal tcUserPrincipal) {
            this.tcUserPrincipal = tcUserPrincipal;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            chmod(dir, tcUserPrincipal);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            chmod(file, tcUserPrincipal);
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            if (exc != null)
                System.err.println("WARNING: " + exc);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            System.err.println("WARNING: " + exc);
            return CONTINUE;
        }
    }

    /**
     * Changes the permissions of the file using the given Changer.
     */
    static void chmod(Path file, UserPrincipal tcUserPrincipal) {
        PosixFileAttributeView fileAttributeView = Files.getFileAttributeView(file, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        try {
            fileAttributeView.setOwner(tcUserPrincipal);
        } catch (IOException e) {
            logger.error("파일 '{}' 의 소유자를 변경할 수 없습니다.", file.toFile().getName(), e);
        }
    }

}
