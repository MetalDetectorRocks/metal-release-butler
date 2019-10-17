package com.metalr2.butler.config.bootstrap


import com.metalr2.butler.model.release.ReleaseEntity
import com.metalr2.butler.model.release.ReleaseType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

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
            .releaseTime("2019-10-25")
            .genre("Black Metal (early), Post-Metal/Shoegaze (later)")
            .type(ReleaseType.FULL_LENGTH)
            .build()

    ReleaseEntity cradleOfFilth = ReleaseEntity.builder()
            .artist("Cradle of Filth")
            .albumTitle("Cruelty and the Beast: Re-Mistressed")
            .releaseTime("2019-11-01")
            .genre("Death Metal (early), Symphonic Black Metal (mid), Extreme Gothic Metal (later)")
            .type(ReleaseType.FULL_LENGTH)
            .build()

    ReleaseEntity eluveitie = ReleaseEntity.builder()
            .artist("Eluveitie")
            .albumTitle("Live at Masters of Rock")
            .releaseTime("2019-10-25")
            .genre("Folk/Melodic Death Metal, Folk")
            .type(ReleaseType.LIVE_ALBUM)
            .build()

    entityManager.persist(alcest)
    entityManager.persist(cradleOfFilth)
    entityManager.persist(eluveitie)
  }

}
