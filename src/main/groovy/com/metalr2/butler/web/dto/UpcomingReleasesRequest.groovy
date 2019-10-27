package com.metalr2.butler.web.dto

import groovy.transform.Canonical

@Canonical
class UpcomingReleasesRequest {

  Collection<String> artistNames

}
