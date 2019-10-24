package com.metalr2.butler.config.bootstrap

import com.metalr2.butler.model.release.ReleaseEntity
import com.metalr2.butler.model.release.ReleaseEntityRecordState
import com.metalr2.butler.model.release.ReleaseSource
import com.metalr2.butler.model.release.ReleaseType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import java.time.LocalDate
import java.time.ZoneId

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
  @Transactional
  void run(ApplicationArguments args) throws Exception {
    ReleaseEntity alcest = ReleaseEntity.builder()
            .artist("Alcest")
            .albumTitle("Spiritual Instinct")
            .releaseDate(LocalDate.of(2019, 11, 25).atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime())
            .genre("Black Metal (early), Post-Metal/Shoegaze (later)")
            .type(ReleaseType.FULL_LENGTH)
            .source(ReleaseSource.METAL_ARCHIVES)
            .state(ReleaseEntityRecordState.DEMO)
            .build()

    ReleaseEntity cradleOfFilth = ReleaseEntity.builder()
            .artist("Cradle of Filth")
            .albumTitle("Cruelty and the Beast: Re-Mistressed")
            .releaseDate(LocalDate.of(2019, 11, 1).atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime())
            .genre("Death Metal (early), Symphonic Black Metal (mid), Extreme Gothic Metal (later)")
            .type(ReleaseType.FULL_LENGTH)
            .source(ReleaseSource.METAL_ARCHIVES)
            .state(ReleaseEntityRecordState.DEMO)
            .build()

    ReleaseEntity caronte = ReleaseEntity.builder()
            .artist("Caronte")
            .albumTitle("Wolves of Thelema")
            .estimatedReleaseDate("Winter")
            .genre("Death Metal")
            .type(ReleaseType.FULL_LENGTH)
            .source(ReleaseSource.METAL_HAMMER_DE)
            .state(ReleaseEntityRecordState.DEMO)
            .build()

    ReleaseEntity voland = ReleaseEntity.builder()
            .artist("平衡世界的意志")
            .albumTitle("Bоланд")
            .releaseDate(LocalDate.of(2019, 11, 23).atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime())
            .genre("Symphonic/Folk Black Metal")
            .type(ReleaseType.COMPILATION)
            .source(ReleaseSource.METAL_ARCHIVES)
            .state(ReleaseEntityRecordState.DEMO)
            .build()

    ReleaseEntity grond = ReleaseEntity.builder()
            .artist("Grond")
            .albumTitle("Endless Spiral of Terror")
            .releaseDate(LocalDate.of(2019, 11, 30).atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime())
            .genre("Death Metal")
            .type(ReleaseType.SPLIT)
            .source(ReleaseSource.METAL_ARCHIVES)
            .additionalArtists("Graceless")
            .state(ReleaseEntityRecordState.DEMO)
            .build()

    ReleaseEntity graceless = ReleaseEntity.builder()
            .artist("Graceless")
            .albumTitle("Endless Spiral of Terror")
            .releaseDate(LocalDate.of(2019, 11, 30).atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime())
            .genre("Death Metal")
            .type(ReleaseType.SPLIT)
            .source(ReleaseSource.METAL_ARCHIVES)
            .additionalArtists("Grond")
            .state(ReleaseEntityRecordState.DEMO)
            .build()

    entityManager.persist(alcest)
    entityManager.persist(cradleOfFilth)
    entityManager.persist(caronte)
    entityManager.persist(voland)
    entityManager.persist(grond)
    entityManager.persist(graceless)
  }

}
