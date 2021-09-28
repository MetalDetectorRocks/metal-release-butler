package rocks.metaldetector.butler.testutil

import org.springframework.context.support.StaticApplicationContext
import org.springframework.web.accept.ContentNegotiationManager
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
import rocks.metaldetector.butler.web.rest.RestExceptionHandler

trait WithExceptionResolver {

  HandlerExceptionResolver exceptionResolver() {
    def applicationContext = new StaticApplicationContext()
    applicationContext.registerSingleton("exceptionHandler", RestExceptionHandler)

    def webMvcConfigurationSupport = new WebMvcConfigurationSupport()
    webMvcConfigurationSupport.setApplicationContext(applicationContext)

    return webMvcConfigurationSupport.handlerExceptionResolver(new ContentNegotiationManager())
  }
}