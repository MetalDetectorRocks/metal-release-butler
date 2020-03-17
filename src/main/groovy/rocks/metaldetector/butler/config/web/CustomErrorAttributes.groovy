package rocks.metaldetector.butler.config.web

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.context.request.WebRequest

@Component
class CustomErrorAttributes extends DefaultErrorAttributes {

  @Override
  Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
    def errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace)
    errorAttributes.remove('exception')
    errorAttributes.remove('path')

    return errorAttributes
  }
}
