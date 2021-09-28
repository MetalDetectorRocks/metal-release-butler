package rocks.metaldetector.butler.config.security

enum UserRole {

  ROLE_USER{

    @Override
    String getName() {
      "ROLE_" + displayName.toUpperCase()
    }

    @Override
    String getDisplayName() {
      "User"
    }
  },
  ROLE_ADMINISTRATOR{

    @Override
    String getName() {
      "ROLE_" + displayName.toUpperCase()
    }

    @Override
    String getDisplayName() {
      "Administrator"
    }
  };

  abstract String getName()

  abstract String getDisplayName()
}