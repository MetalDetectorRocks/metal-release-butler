package rocks.metaldetector.butler.supplier.metalarchives

import java.util.concurrent.TimeUnit

trait MetalArchivesReleaseImportUtils {

  void throttle() {
    TimeUnit.SECONDS.sleep(5)
  }
}
