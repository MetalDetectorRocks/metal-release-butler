package rocks.metaldetector.butler.config.web;

class ResourceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  ResourceNotFoundException(String message) {
    super(message);
  }
}
