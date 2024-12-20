package sak.metricstool.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sak.metricstool.integration.ActualGitLabClient;
import sak.metricstool.integration.GitLabClient;
import sak.metricstool.integration.MockGitLabClient;
import org.springframework.web.client.RestTemplate;

/**
 * GitLabClientの実装をプロファイルに応じて切り替えるコンフィギュレーションクラス。
 */
@Configuration
public class GitLabClientConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "mock")
    public GitLabClient mockGitLabClient() {
        return new MockGitLabClient();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "dev", matchIfMissing = true)
    public GitLabClient actualGitLabClientDev(RestTemplate restTemplate) {
        return new ActualGitLabClient(restTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod")
    public GitLabClient actualGitLabClientProd(RestTemplate restTemplate) {
        return new ActualGitLabClient(restTemplate);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
