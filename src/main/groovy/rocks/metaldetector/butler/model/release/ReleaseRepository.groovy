package rocks.metaldetector.butler.model.release

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import java.time.LocalDate

@Repository
interface ReleaseRepository extends JpaRepository<ReleaseEntity, Long> {

  Page<ReleaseEntity> findAllByReleaseDateAfter(LocalDate date, Pageable pageable)

  Page<ReleaseEntity> findAllByReleaseDateBetween(LocalDate from, LocalDate to, Pageable pageable)

  Page<ReleaseEntity> findAllByReleaseDateAfterAndArtistIn(LocalDate date, Iterable<String> artistNames, Pageable pageable)

  Page<ReleaseEntity> findAllByArtistInAndReleaseDateBetween(Iterable<String> artistNames, LocalDate from, LocalDate to, Pageable pageable)

  List<ReleaseEntity> findAllByReleaseDateAfter(LocalDate date, Sort sort)

  List<ReleaseEntity> findAllByReleaseDateBetween(LocalDate from, LocalDate to, Sort sort)

  List<ReleaseEntity> findAllByReleaseDateAfterAndArtistIn(LocalDate date, Iterable<String> artistNames, Sort sort)

  List<ReleaseEntity> findAllByArtistInAndReleaseDateBetween(Iterable<String> artistNames, LocalDate from, LocalDate to, Sort sort)

  boolean existsByArtistAndAlbumTitleAndReleaseDate(String artist, String albumTitle, LocalDate releaseDate)
}
