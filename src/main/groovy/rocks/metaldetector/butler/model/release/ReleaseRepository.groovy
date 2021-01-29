package rocks.metaldetector.butler.model.release

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

import java.time.LocalDate

@Repository
interface ReleaseRepository extends JpaRepository<ReleaseEntity, Long> {

  @Query("select r from releases r where r.releaseDate >= :date and r.state = 'OK' and (:query is null or (lower(r.artist) like lower(concat('%', :query, '%')) or lower(r.albumTitle) like lower(concat('%', :query, '%'))))")
  Page<ReleaseEntity> findAllReleasesFrom(@Param("date") LocalDate date, @Param("query") String query, Pageable pageable)

  @Query("select r from releases r where r.releaseDate between :from and :to and r.state = 'OK' and (:query is null or (lower(r.artist) like lower(concat('%', :query, '%')) or lower(r.albumTitle) like lower(concat('%', :query, '%'))))")
  Page<ReleaseEntity> findAllReleasesBetween(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("query") String query, Pageable pageable)

  @Query("select r from releases r where r.releaseDate >= :date and r.state = 'OK' and r.artist in :artistNames and (:query is null or (lower(r.artist) like lower(concat('%', :query, '%')) or lower(r.albumTitle) like lower(concat('%', :query, '%'))))")
  Page<ReleaseEntity> findAlleReleasesFromWithArtists(@Param("date") LocalDate date, @Param("artistNames") Iterable<String> artistNames, @Param("query") String query, Pageable pageable)

  @Query("select r from releases r where r.releaseDate between :from and :to and r.state = 'OK' and r.artist in :artistNames and (:query is null or (lower(r.artist) like lower(concat('%', :query, '%')) or lower(r.albumTitle) like lower(concat('%', :query, '%'))))")
  Page<ReleaseEntity> findAllReleasesBetweenWithArtists(@Param("artistNames") Iterable<String> artistNames, @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("query") String query, Pageable pageable)

  List<ReleaseEntity> findAllByReleaseDateAfter(LocalDate date, Sort sort)

  List<ReleaseEntity> findAllByReleaseDateBetween(LocalDate from, LocalDate to, Sort sort)

  List<ReleaseEntity> findAllByReleaseDateAfterAndArtistIn(LocalDate date, Iterable<String> artistNames, Sort sort)

  List<ReleaseEntity> findAllByArtistInAndReleaseDateBetween(Iterable<String> artistNames, LocalDate from, LocalDate to, Sort sort)

  Optional<ReleaseEntity> findById(long id)

  boolean existsByArtistIgnoreCaseAndAlbumTitleIgnoreCaseAndReleaseDate(String artist, String albumTitle, LocalDate releaseDate)

  void deleteByReleaseDetailsUrl(String releaseDetailsUrl)
}
