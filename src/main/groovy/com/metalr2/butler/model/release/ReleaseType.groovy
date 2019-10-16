package com.metalr2.butler.model.release

enum ReleaseType {

  FULL_LENGTH("Full-Length"),
  DEMO("Demo"),
  EP("EP"),
  COMPILATION("Compilation"),
  BOXED_SET("Boxed set"),
  SINGLE("Single"),
  SPLIT("Split"),
  VIDEO("Video"),
  OTHER("Other");

  String typeName

  ReleaseType(String typeName) {
    this.typeName = typeName
  }

}
