package rocks.metaldetector.butler.supplier.infrastructure

class Endpoints {

  public static final String RELEASES             = "/rest/v1/releases"
  public static final String UPDATE_RELEASE       = "/rest/v1/releases/{releaseId:[0-9]+}"
  public static final String RELEASES_UNPAGINATED = "/rest/v1/releases/unpaginated"
  public static final String COVER_JOB            = "/rest/v1/releases/cover-reload"
  public static final String RELEASE_IMAGES       = "/rest/v1/releases/images"
  public static final String IMPORT_JOB           = "/rest/v1/releases/import"
  public static final String FETCH_IMPORT_JOB     = "/rest/v1/releases/import/{jobId}"
  public static final String STATISTICS           = "/rest/v1/releases/statistics"

  static class AntPattern {

    public static final String ACTUATOR_ENDPOINTS = "/actuator/**"
  }
}
