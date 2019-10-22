package com.metalr2.butler.model.release

import org.springframework.data.jpa.repository.JpaRepository

interface ReleaseRepository extends JpaRepository<ReleaseEntity, Long> {

}