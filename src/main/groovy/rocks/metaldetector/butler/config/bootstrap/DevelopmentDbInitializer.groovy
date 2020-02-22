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

import static rocks.metaldetector.butler.model.release.ReleaseEntityRecordState.DEMO
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_ARCHIVES
import static rocks.metaldetector.butler.model.release.ReleaseSource.METAL_HAMMER_DE
import static rocks.metaldetector.butler.model.release.ReleaseType.COMPILATION
import static rocks.metaldetector.butler.model.release.ReleaseType.FULL_LENGTH
import static rocks.metaldetector.butler.model.release.ReleaseType.SPLIT

@Component
@Profile("dev")
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
    addExampleReleases()
  }

  private void addExampleReleases() {
    ReleaseEntity alcest = ReleaseEntity.builder()
            .artist("Alcest")
            .albumTitle("Spiritual Instinct")
            .releaseDate(LocalDate.of(2019, 11, 25))
            .genre("Black Metal (early), Post-Metal/Shoegaze (later)")
            .type(FULL_LENGTH)
            .source(METAL_ARCHIVES)
            .state(DEMO)
            .build()

    ReleaseEntity cradleOfFilth = ReleaseEntity.builder()
            .artist("Cradle of Filth")
            .albumTitle("Cruelty and the Beast: Re-Mistressed")
            .releaseDate(LocalDate.of(2019, 11, 1))
            .genre("Death Metal (early), Symphonic Black Metal (mid), Extreme Gothic Metal (later)")
            .type(FULL_LENGTH)
            .source(METAL_ARCHIVES)
            .state(DEMO)
            .build()

    ReleaseEntity caronte = ReleaseEntity.builder()
            .artist("Caronte")
            .albumTitle("Wolves of Thelema")
            .estimatedReleaseDate("Winter")
            .genre("Death Metal")
            .type(FULL_LENGTH)
            .source(METAL_HAMMER_DE)
            .state(DEMO)
            .build()

    ReleaseEntity voland = ReleaseEntity.builder()
            .artist("平衡世界的意志")
            .albumTitle("Bоланд")
            .releaseDate(LocalDate.of(2019, 11, 23))
            .genre("Symphonic/Folk Black Metal")
            .type(COMPILATION)
            .source(METAL_ARCHIVES)
            .state(DEMO)
            .build()

    ReleaseEntity grond = ReleaseEntity.builder()
            .artist("Grond")
            .albumTitle("Endless Spiral of Terror")
            .releaseDate(LocalDate.of(2019, 11, 30))
            .genre("Death Metal")
            .type(SPLIT)
            .source(METAL_ARCHIVES)
            .additionalArtists("Graceless")
            .state(DEMO)
            .build()

    ReleaseEntity graceless = ReleaseEntity.builder()
            .artist("Graceless")
            .albumTitle("Endless Spiral of Terror")
            .releaseDate(LocalDate.of(2019, 11, 30))
            .genre("Death Metal")
            .type(SPLIT)
            .source(METAL_ARCHIVES)
            .additionalArtists("Grond")
            .state(DEMO)
            .build()

    entityManager.persist(alcest)
    entityManager.persist(cradleOfFilth)
    entityManager.persist(caronte)
    entityManager.persist(voland)
    entityManager.persist(grond)
    entityManager.persist(graceless)
  }
}
