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

    // remove duplicates
    releaseEntities.unique { release1, release2 ->
      release1.artist <=> release2.artist ?: release1.albumTitle <=> release2.albumTitle ?: release1.releaseDate <=> release2.releaseDate
    }

    releaseRepository.saveAll(releaseEntities)
  }
}
