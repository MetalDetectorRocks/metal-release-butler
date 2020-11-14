package rocks.metaldetector.butler.config.constants

class Endpoints {

  public static final String RELEASES             = "/rest/v1/releases"
  public static final String IMPORT_JOB           = "/rest/v1/releases/import"
  public static final String COVER_JOB            = "/rest/v1/releases/cover-reload"
  public static final String RELEASES_UNPAGINATED = "/rest/v1/releases/unpaginated"
  public static final String UPDATE_RELEASE       = "/rest/v1/releases/{releaseId}"

  static class AntPattern {

    public static final String REST_ENDPOINTS = "/rest/**"
  }
}
