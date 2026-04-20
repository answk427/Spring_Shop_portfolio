package work.trade.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        System.out.println("CORS Origin: " + allowedOrigins);  // ← 이거 추가

        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)  // ← 환경변수에서 가져옴
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
