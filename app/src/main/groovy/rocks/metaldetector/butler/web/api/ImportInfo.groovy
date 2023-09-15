package rocks.metaldetector.butler.web.api

import java.time.LocalDateTime

class ImportInfo {

  String source
  int successRate
  LocalDateTime lastImport
  LocalDateTime lastSuccessfulImport
}
