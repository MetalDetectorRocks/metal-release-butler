package rocks.metaldetector.butler.persistence.domain.release

interface ReleasePerMonth {

  Integer getReleaseYear()
  Integer getReleaseMonth()
  Integer getReleases()
}
