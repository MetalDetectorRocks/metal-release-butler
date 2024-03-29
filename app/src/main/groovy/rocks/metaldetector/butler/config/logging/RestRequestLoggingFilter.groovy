package rocks.metaldetector.butler.config.logging

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.filter.CommonsRequestLoggingFilter

class RestRequestLoggingFilter extends CommonsRequestLoggingFilter {

  RestRequestLoggingFilter() {
    super.setIncludeQueryString(true)
    super.setIncludePayload(true)
    super.setIncludeClientInfo(true)
    super.setIncludeHeaders(false)
    super.setMaxPayloadLength(10000)
  }

  @Override
  protected boolean shouldLog(HttpServletRequest request) {
    return request.getRequestURI().toLowerCase().startsWith("/rest")
  }

  @Override
  protected void beforeRequest(HttpServletRequest request, String message) {
  }

  @Override
  protected void afterRequest(HttpServletRequest request, String message) {
    logger.info(message)
  }
}
