package com.metalr2.butler.service

import com.metalr2.butler.model.release.ReleaseEntity
import com.metalr2.butler.model.release.ReleaseRepository
import com.metalr2.butler.service.converter.Converter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ReleaseServiceImpl implements ReleaseService {

  final ReleaseRepository releaseRepository
  final Converter<String[], List<ReleaseEntity>> converter

  @Autowired
  ReleaseServiceImpl(ReleaseRepository releaseRepository, Converter<String[], List<ReleaseEntity>> converter) {
    this.releaseRepository = releaseRepository
    this.converter = converter
  }

  @Override
  void saveAll(List<String[]> upcomingReleasesRawData) {
    List<ReleaseEntity> releaseEntities = []
    upcomingReleasesRawData.each { releaseEntities.addAll(converter.convert(it)) }

    releaseRepository.saveAll(releaseEntities)
  }
}
