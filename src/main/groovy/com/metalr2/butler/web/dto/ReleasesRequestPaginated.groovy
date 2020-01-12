package com.metalr2.butler.web.dto

import groovy.transform.Canonical

import javax.validation.constraints.Max
import javax.validation.constraints.Min

@Canonical
class ReleasesRequestPaginated extends ReleasesRequest {

  @Min(value = 1L, message = "'page' must be greater than zero!")
  int page

  @Min(value = 1L, message = "'size' must be greater than zero!")
  @Max(value = 50L, message = "'size' must be equal or less than 50!")
  int size

}
