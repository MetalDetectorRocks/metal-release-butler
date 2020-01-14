package com.metalr2.butler.model

import groovy.transform.Canonical

import java.time.LocalDate

@Canonical
class TimeRange {

  LocalDate from
  LocalDate to

  private TimeRange(LocalDate from, LocalDate to) {
    this.from = from
    this.to = to
  }

  static of(LocalDate from, LocalDate to) {
    return new TimeRange(from, to)
  }

  @Override
  String toString() {
    return from.toString() + " to " + to.toString()
  }

}
