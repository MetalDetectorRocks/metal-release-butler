package rocks.metaldetector.butler.service.release

import org.springframework.stereotype.Component
import rocks.metaldetector.butler.model.release.ReleaseEntity
import rocks.metaldetector.butler.web.dto.ReleaseDto

@Component
class ReleaseTransformer {

  ReleaseDto transform(ReleaseEntity releaseEntity) {
    return ReleaseDto.builder()
        .id(releaseEntity.id)
        .artist(releaseEntity.artist)
        .additionalArtists(releaseEntity.additionalArtists)
        .albumTitle(releaseEntity.albumTitle)
        .releaseDate(releaseEntity.releaseDate)
        .announcementDate(releaseEntity.createdDateTime?.toLocalDate())
        .estimatedReleaseDate(releaseEntity.estimatedReleaseDate)
        .genre(releaseEntity.genre)
        .type(releaseEntity.type)
        .releaseDetailsUrl(releaseEntity.releaseDetailsUrl)
        .artistDetailsUrl(releaseEntity.artistDetailsUrl)
        .source(releaseEntity.source)
        .state(releaseEntity.state)
        .coverUrl(releaseEntity.coverUrl)
        .reissue(releaseEntity.reissue)
        .build()
  }
}
