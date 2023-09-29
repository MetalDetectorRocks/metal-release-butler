package rocks.metaldetector.butler.supplier.metalarchives

import static java.util.concurrent.TimeUnit.SECONDS

trait MetalArchivesReleaseImportUtils {

  void throttle(long throttlingInSeconds) {
    SECONDS.sleep(throttlingInSeconds)
  }
}
