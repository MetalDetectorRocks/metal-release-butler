package rocks.metaldetector.butler.model.release

enum ReleaseType {

  FULL_LENGTH("Full Length"),
  DEMO("Demo"),
  EP("EP"),
  COMPILATION("Compilation"),
  BOXED_SET("Boxed Set"),
  SINGLE("Single"),
  SPLIT("Split"),
  LIVE_ALBUM("Live Album"),
  VIDEO("Video"),
  OTHER("Other");

  static final MAPPINGS = ['Full-length': FULL_LENGTH, 'Demo': DEMO, 'EP': EP, 'Compilation': COMPILATION, 'Boxed set': BOXED_SET,
                           'Single': SINGLE, 'Split': SPLIT, 'Live album': LIVE_ALBUM, 'Video': VIDEO]
  String typeName

  ReleaseType(String typeName) {
    this.typeName = typeName
  }

  static ReleaseType convertFrom(String value) {
    return MAPPINGS[value] ?: OTHER
  }

}
