package rocks.metaldetector.butler.model.release

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import java.time.LocalDate

@Repository
interface ReleaseRepository extends JpaRepository<ReleaseEntity, Long> {

  Page<ReleaseEntity> findAllByReleaseDateAfterAndState(LocalDate date, ReleaseEntityState state, Pageable pageable)

  Page<ReleaseEntity> findAllByReleaseDateBetweenAndState(LocalDate from, LocalDate to, ReleaseEntityState state, Pageable pageable)

  Page<ReleaseEntity> findAllByReleaseDateAfterAndArtistInAndState(LocalDate date, Iterable<String> artistNames, ReleaseEntityState state, Pageable pageable)

  Page<ReleaseEntity> findAllByArtistInAndReleaseDateBetweenAndState(Iterable<String> artistNames, LocalDate from, LocalDate to, ReleaseEntityState state, Pageable pageable)

  List<ReleaseEntity> findAllByReleaseDateAfter(LocalDate date, Sort sort)

  List<ReleaseEntity> findAllByReleaseDateBetween(LocalDate from, LocalDate to, Sort sort)

  List<ReleaseEntity> findAllByReleaseDateAfterAndArtistIn(LocalDate date, Iterable<String> artistNames, Sort sort)

  List<ReleaseEntity> findAllByArtistInAndReleaseDateBetween(Iterable<String> artistNames, LocalDate from, LocalDate to, Sort sort)

  Optional<ReleaseEntity> findById(long id)

  boolean existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(String artist, String albumTitle, LocalDate releaseDate);
}
