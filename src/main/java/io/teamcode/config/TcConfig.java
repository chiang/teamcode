package io.teamcode.config;

import io.teamcode.TeamcodeException;
import io.teamcode.common.converter.KnownFile;
import io.teamcode.common.io.KnownFilesResolveException;
import io.teamcode.common.io.KnownFilesResolver;
import io.teamcode.service.TeamcodeExampleService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by chiang on 2017. 2. 1..
 */
//TODO getter only
@Data
@Configuration
public class TcConfig {

    private static final Logger logger = LoggerFactory.getLogger(TcConfig.class);

    @Value("${teamcode.home}")
    private String home;

    //TODO 설정이 없을 때?
    @Value("${teamcode.artifacts-dir}")
    private String artifactsPath;

    @Value("${teamcode.external-url}")
    private String externalUrl;

    @Value("${subversion.version}")
    private String subversionVersion;

    @Value("${subversion.realm}")
    private String subversionRealm;

    /**
     * 팀코드 서버를 실행하면서 자동으로 저장소 파일 권한을 동기화하는데 테스트 환경에서는 이렇게 하면 안 되므로 이렇게 기본 값을
     * <code>true</code> 로 설정하고 테스트 환경에서는 false 로 설정합니다. 동기화 시 파일에 대한 소유권을 변경하는 작업이 있어서
     * 이와 같이 처리합니다.
     *
     */
    @Value("${teamcode.sync.permission:true}")
    private boolean syncPermission;

    @Value("${teamcode.security.ip-white-list:}")
    private String[] ipWhitelist;

    private File homeDir;

    /**
     * 그냥 homeDir 은 Docker Container 내부의 Teamcode Home Directory 이고 이 디레터리는 Host Machine 상의 Directory 입니다.
     * 이렇게 구분하는 것은 Runner 에서 Subversion Checkout 할 때 참조를 Host Machine 상에서 하기 때문입니다.
     *
     */
    private File hostHomeDir;

    private File dataDir;

    private File hostDataDir;

    private File tempDir;

    /**
     * CI 관련 로그 등이 위치한 디렉터리
     *
     */
    private File ciDir;

    private File artifactsDir;

    private File pipelinesDir;

    /**
     * 빌드 관련 로그가 위치한 디렉터리. CI 파이프라인이 실행하던 중에 발생하는 모든 로그가 여기에 쌓인다.
     *
     */
    private File logsDir;

    private File configDir;

    private File repositoryRootDir;

    private File repositoryHostRootDir;

    private File archivedRepositoryRootDir;

    private File attachmentsDir;

    private File usersAttachmentsDir;

    private File passwordFile;

    private File authz;

    @Value("${teamcode.attachments.downloads.max-files}")
    private Integer attachmentMaxFiles;

    /**
     * Servlet Connector Protocol. 'HTTP/1.1' or 'AJP/1.3'
     *
     */
    @Value("${container.protocol}")
    private String protocol;

    @Value("${container.port}")
    private Integer serverPort;

    @Value("${container.session-timeout}")
    private Integer sessionTimeout;

    @PostConstruct
    public void init() {
        String[] homeMappings = this.home.split(":");
        if (homeMappings.length == 1) {
            homeDir = new File(home);
            hostHomeDir = new File(home);
        }
        else {
            logger.debug("TeamCode Home Directory 설정이 Host Mapping 을 포함하고 있습니다. Host 정보는 '{}' 입니다.", homeMappings[0]);
            homeDir = new File(homeMappings[1]);
            hostHomeDir = new File(homeMappings[0]);
        }
        if (!homeDir.exists()) {
            throw new IllegalArgumentException(String.format("Teamcode 홈 디렉터리를 찾을 수 없습니다. 현재 설정된 디렉터리는 '%s' 입니다.", homeDir.getAbsolutePath()));
        }

        dataDir = new File(homeDir, "data");
        if (!dataDir.exists()) {
            if (!dataDir.mkdir()) {
                throw new TeamcodeException(String.format("팀코드 서버에서 관리하는 데이터를 저장하는 data 디렉터리를 만들 수 없습니다. 위치는 '%s' 입니다.", dataDir.getAbsolutePath()));
            }
        }
        hostDataDir = new File(hostHomeDir, "data");

        tempDir = new File(homeDir, "temp");
        if (!tempDir.exists()) {
            if (!tempDir.mkdir()) {
                throw new TeamcodeException(String.format("팀코드 서버에서 관리하는 임시 데이터를 저장하는 temp 디렉터리를 만들 수 없습니다. 위치는 '%s' 입니다.", tempDir.getAbsolutePath()));
            }
        }

        ciDir = new File(dataDir, "ci");
        if (!ciDir.exists()) {
            if (!ciDir.mkdir()) {
                throw new TeamcodeException(String.format("팀코드 서버에서 관리하는 데이터를 저장하는 data/ci 디렉터리를 만들 수 없습니다. 위치는 '%s' 입니다.", ciDir.getAbsolutePath()));
            }
        }

        artifactsDir = new File(this.artifactsPath);
        if (!artifactsDir.exists()) {
            if (!artifactsDir.mkdir()) {
                throw new TeamcodeException(String.format("팀코드 서버에서 관리하는 데이터를 저장하는 Artifacts 디렉터리를 만들 수 없습니다. 위치는 '%s' 입니다.", artifactsDir.getAbsolutePath()));
            }
        }

        pipelinesDir = new File(ciDir, "pipelines");
        if (!pipelinesDir.exists()) {
            if (!pipelinesDir.mkdir()) {
                throw new TeamcodeException(String.format("팀코드 서버에서 관리하는 데이터를 저장하는 pipelines 디렉터리를 만들 수 없습니다. 위치는 '%s' 입니다.", pipelinesDir.getAbsolutePath()));
            }
        }

        logsDir = new File(ciDir, "logs");

        if (!logsDir.exists()) {
            if (!logsDir.mkdir()) {
                throw new TeamcodeException(String.format("팀코드 서버에서 관리하는 데이터를 저장하는 data/ci/logs 디렉터리를 만들 수 없습니다. 위치는 '%s' 입니다.", logsDir.getAbsolutePath()));
            }
        }

        configDir = new File(homeDir, "config");
        if (!configDir.exists()) {
            throw new TeamcodeException(String.format("설정 파일 디렉터리를 찾을 수 없습니다. 위치는 '%s' 입니다.", configDir.getAbsolutePath()));
        }

        repositoryRootDir = new File(dataDir, "repositories");
        if (!repositoryRootDir.exists()) {
            if (!repositoryRootDir.mkdir()) {
                throw new TeamcodeException(String.format("서브버전 저장소 루트 디렉터리를 만들 수 없습니다. 위치는 '%s' 입니다.", repositoryRootDir.getAbsolutePath()));
            }
        }
        repositoryHostRootDir = new File(hostDataDir, "repositories");//위에서 만들면 자동으로 Host Machine 에 생성됨.

        archivedRepositoryRootDir = new File(dataDir, "archived-repositories");
        if (!archivedRepositoryRootDir.exists()) {
            if (!archivedRepositoryRootDir.mkdir()) {
                throw new TeamcodeException(String.format("서브버전 아카이브 저장소 루트 디렉터리를 만들 수 없습니다. 위치는 '%s' 입니다.", archivedRepositoryRootDir.getAbsolutePath()));
            }
        }

        attachmentsDir = new File(dataDir, "attachments");
        if (!attachmentsDir.exists()) {
            if (!attachmentsDir.mkdir()) {
                throw new TeamcodeException(String.format("업로드 데이터를 저장하는 attachments 디렉터리를 만들 수 없습니다. 위치는 '%s' 입니다.", attachmentsDir.getAbsolutePath()));
            }
        }

        File projectsDir = new File(attachmentsDir, "projects");
        if (!projectsDir.exists()) {
            if (!projectsDir.mkdir()) {
                throw new TeamcodeException(String.format("업로드 데이터를 저장하는 attachments/projects 디렉터리를 만들 수 없습니다. 위치는 '%s' 입니다.", projectsDir.getAbsolutePath()));
            }
        }

        usersAttachmentsDir = new File(attachmentsDir, "users");
        if (!usersAttachmentsDir.exists()) {
            if (!usersAttachmentsDir.mkdir()) {
                throw new TeamcodeException(String.format("사용자 첨부 파일을 저장하는 attachments/users 디렉터리를 만들 수 없습니다. 위치는 '%s' 입니다.", usersAttachmentsDir.getAbsolutePath()));
            }
        }

        File httpdConfigDir = new File(configDir, "httpd");
        if (!httpdConfigDir.exists()) {
            throw new TeamcodeException(String.format("HTTPD 설정 디렉터리를 찾을 수 없습니다. 위치는 '%s' 입니다.", httpdConfigDir.getAbsolutePath()));
        }

        passwordFile = new File(httpdConfigDir, "users.conf");
        if (!passwordFile.exists()) {
            throw new TeamcodeException(String.format("서브버전 사용자 설정 파일을 찾을 수 없습니다. 위치는 '%s' 입니다.", passwordFile.getAbsolutePath()));
        }

        authz = new File(httpdConfigDir, "authz.conf");
        if (!authz.exists()) {
            throw new TeamcodeException(String.format("서브버전 접근 제어 설정 파일을 찾을 수 없습니다. 위치는 '%s' 입니다.", authz.getAbsolutePath()));
        }
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();

        factory.setProtocol(protocol);
        factory.setPort(serverPort);
        //factory.setContextPath(context);

        if (protocol.equals("AJP/1.3")) {
            logger.info("서버가 HTTPD 와 연동하도록 설정이 되었습니다. 팀코드에 접속 시 참고하시기 바랍니다.");
        }

        if (sessionTimeout != null) {
            if (sessionTimeout.intValue() > 10) {
                //TODO Check max timeout...
                factory.setSessionTimeout(sessionTimeout.intValue(), TimeUnit.MINUTES);
            }
            else {
                //TODO logging
                factory.setSessionTimeout(30, TimeUnit.MINUTES);
            }
        }
        else {
            factory.setSessionTimeout(30, TimeUnit.MINUTES);
        }

        //FIXME runtime error factory.setUriEncoding("UTF-8");

        return factory;
    }

    @Bean
    public KnownFilesResolver knownFilesResolver() {
        KnownFilesResolver knownFilesResolver = new KnownFilesResolver();
        try {
            knownFilesResolver.read(KnownFilesResolver.class.getResourceAsStream("/io/teamcode/known-file-names.xml"));

            return knownFilesResolver;
        } catch (IOException | KnownFilesResolveException e) {
            throw new TeamcodeException("기본 설정 파일을 읽던 중 오류가 발생했습니다.", e);
        }
    }

    public String getRepositoryRootPath() {

        return this.repositoryRootDir.getAbsolutePath();
    }

    public String getRepositoryHostRootPath() {

        return this.repositoryHostRootDir.getAbsolutePath();
    }

}
