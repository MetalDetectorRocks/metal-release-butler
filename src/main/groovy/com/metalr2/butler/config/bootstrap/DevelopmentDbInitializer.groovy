package com.metalr2.butler.config.bootstrap

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
    // ToDo DanielW: implement
  }

}
