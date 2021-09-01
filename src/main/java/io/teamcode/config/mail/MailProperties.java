package io.teamcode.config.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "mail")
@Data
public class MailProperties {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private static final String DEFAULT_FROM_NAME = "팀코드";

    private boolean enabled;

    /**
     * SMTP server host.
     */
    private String host;

    /**
     * SMTP server port.
     */
    private Integer port;

    //http://docs.aws.amazon.com/ko_kr/ses/latest/DeveloperGuide/email-format.html
    private String from;

    private String fromName;

    /**
     * Login user of the SMTP server.
     */
    private String username;

    /**
     * Login password of the SMTP server.
     */
    private String password;

    /**
     * Protocol used by the SMTP server.
     */
    private String protocol = "smtp";

    /**
     * Default MimeMessage encoding.
     */
    private Charset defaultEncoding = DEFAULT_CHARSET;

    /**
     * Additional JavaMail session properties.
     */
    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * Session JNDI name. When set, takes precedence to others mail settings.
     */
    private String jndiName;

    public String getFromName() {
        if (StringUtils.hasText(this.fromName)) {
            return this.fromName;
        }
        else {
            return DEFAULT_FROM_NAME;
        }
    }

}

