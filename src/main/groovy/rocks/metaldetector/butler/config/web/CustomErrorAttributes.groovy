package rocks.metaldetector.butler.config.web

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.context.request.WebRequest

@Component
class CustomErrorAttributes extends DefaultErrorAttributes {

  @Override
  Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions errorAttributeOptions) {
    def errorAttributes = super.getErrorAttributes(webRequest, errorAttributeOptions)
    errorAttributes.remove('exception')
    errorAttributes.remove('path')

    return errorAttributes
  }
}
