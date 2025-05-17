package com.linyajin.mikufans.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class AppConfig {
    @Value("${web.jwt.secret:}")
    private String WebJwtSecret;

    @Value("${web.jwt.expiresIn:}")
    private Integer WebJwtExpiresIn;

    @Value("${admin.jwt.secret:}")
    private String AdminJwtSecret;

    @Value("${admin.jwt.expiresIn:}")
    private Integer AdminJwtExpiresIn;

    @Value("${admin.account:}")
    private String AdminAccount;

    @Value("${admin.password:}")
    private String AdminPassword;

    @Value("${project.folder:}")
    private String ProjectFolder;

    @Value("${es.host.port:127.0.0.1:9200}")
    private String esHostPort;

    @Value("${es.index.video.name:mikufans_video}")
    private String esIndexVideoName;
}
