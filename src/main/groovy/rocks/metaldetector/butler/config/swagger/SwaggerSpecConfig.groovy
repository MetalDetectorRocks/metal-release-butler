package rocks.metaldetector.butler.config.swagger

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider
import springfox.documentation.swagger.web.SwaggerResource
import springfox.documentation.swagger.web.SwaggerResourcesProvider

@Configuration
class SwaggerSpecConfig {

  @Primary
  @Bean
  SwaggerResourcesProvider swaggerResourcesProvider(InMemorySwaggerResourcesProvider defaultResourcesProvider) {
    return {
      SwaggerResource wsResource = new SwaggerResource()
      wsResource.name = 'Metal Release Butler REST API Doc'
      wsResource.swaggerVersion = '2.0'
      wsResource.location = '/swagger.yml' // need to be a url

      List<SwaggerResource> resources = new ArrayList<>(defaultResourcesProvider.get())
      resources.add(wsResource)
      return resources
    }
  }

}
