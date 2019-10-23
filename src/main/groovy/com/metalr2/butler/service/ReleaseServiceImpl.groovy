package com.metalr2.butler.service

import com.metalr2.butler.model.release.ReleaseEntity
import com.metalr2.butler.model.release.ReleaseRepository
import com.metalr2.butler.service.converter.Converter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.time.LocalDate
import java.time.ZoneId

@Service
class ReleaseServiceImpl implements ReleaseService {

  final Logger log = LoggerFactory.getLogger(ReleaseServiceImpl)
  final ReleaseRepository releaseRepository
  final Converter<String[], List<ReleaseEntity>> converter

  @Autowired
  ReleaseServiceImpl(ReleaseRepository releaseRepository, Converter<String[], List<ReleaseEntity>> converter) {
    this.releaseRepository = releaseRepository
    this.converter = converter
  }

  @Override
  @Transactional
  void saveAll(List<String[]> upcomingReleasesRawData) {
    List<ReleaseEntity> releaseEntities = []
    upcomingReleasesRawData.each { releaseEntities.addAll(converter.convert(it)) }

    // remove duplicates
    releaseEntities.unique { release1, release2 ->
      release1.artist <=> release2.artist ?: release1.albumTitle <=> release2.albumTitle ?: release1.releaseDate <=> release2.releaseDate
    }

    // remove any record that has a release date today or later
    def yesterday = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime()
    int affectedRows = releaseRepository.deleteByReleaseDateIsAfter(yesterday)
    log.info("{} records were deleted", affectedRows)

    releaseRepository.saveAll(releaseEntities)
  }
}
