package rocks.metaldetector.butler.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfig implements WebMvcConfigurer {

  @Override
  void addResourceHandlers(final ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/resources/**").addResourceLocations("/resources/static/")
  }

  @Bean
  Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
            .enable(false)
            .select()
            .apis(RequestHandlerSelectors.none())
            .paths(PathSelectors.none())
            .build()
  }

}
