package rocks.metaldetector.butler.config.bootstrap

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import rocks.metaldetector.butler.model.release.ReleaseEntity

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import java.time.LocalDate

import static rocks.metaldetector.butler.model.release.ReleaseEntityState.DEMO
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE
import static rocks.metaldetector.butler.model.release.ReleaseType.COMPILATION
import static rocks.metaldetector.butler.model.release.ReleaseType.FULL_LENGTH
import static rocks.metaldetector.butler.model.release.ReleaseType.SPLIT

@Component
@Profile("default")
class DevelopmentDbInitializer implements ApplicationRunner {

  @PersistenceContext
  final EntityManager entityManager

  @Autowired
  DevelopmentDbInitializer(EntityManager entityManager) {
    this.entityManager = entityManager
  }

  @Override
  @Transactional(readOnly = false)
  void run(ApplicationArguments args) throws Exception {
    List<ReleaseEntity> currentExistingReleases = entityManager.createQuery("select r from releases r", ReleaseEntity).getResultList()
    if (currentExistingReleases.isEmpty()) {
      addExampleUpcomingReleases()
    }
  }

  private void addExampleUpcomingReleases() {
    ReleaseEntity release1 = ReleaseEntity.builder()
            .artist("Corona")
            .albumTitle("Bringing the Death to you")
            .releaseDate(LocalDate.now().plusDays(33))
            .genre("Black Metal (early), Post-Metal/Shoegaze (later)")
            .type(FULL_LENGTH)
            .source(METAL_HAMMER_DE)
            .state(DEMO)
            .build()

    ReleaseEntity release2 = ReleaseEntity.builder()
            .artist("Outbreak")
            .albumTitle("Disastrous Pandemic")
            .releaseDate(LocalDate.now().plusDays(66))
            .genre("Death Metal (early), Symphonic Black Metal (mid), Extreme Gothic Metal (later)")
            .type(FULL_LENGTH)
            .source(METAL_HAMMER_DE)
            .state(DEMO)
            .build()

    ReleaseEntity release3 = ReleaseEntity.builder()
            .artist("平衡世界的意志")
            .albumTitle("Bоланд")
            .releaseDate(LocalDate.now().plusDays(99))
            .genre("Symphonic/Folk Black Metal")
            .type(COMPILATION)
            .source(METAL_HAMMER_DE)
            .state(DEMO)
            .build()

    ReleaseEntity release4 = ReleaseEntity.builder()
            .artist("Hamster purchaser")
            .albumTitle("Hope you gonna die with a roll of toilet paper in your hand")
            .releaseDate(LocalDate.now().plusDays(40))
            .genre("Death Metal")
            .type(SPLIT)
            .source(METAL_HAMMER_DE)
            .additionalArtists("Stupid people")
            .state(DEMO)
            .build()

    ReleaseEntity release5 = ReleaseEntity.builder()
            .artist("Stupid people")
            .albumTitle("Hope you gonna die with a roll of toilet paper in your hand")
            .releaseDate(LocalDate.now().plusDays(40))
            .genre("Death Metal")
            .type(SPLIT)
            .source(METAL_HAMMER_DE)
            .additionalArtists("Hamster purchaser")
            .state(DEMO)
            .build()

    entityManager.persist(release1)
    entityManager.persist(release2)
    entityManager.persist(release3)
    entityManager.persist(release4)
    entityManager.persist(release5)
  }
}
