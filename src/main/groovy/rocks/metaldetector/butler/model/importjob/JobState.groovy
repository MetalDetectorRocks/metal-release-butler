package rocks.metaldetector.butler.model.importjob

enum JobState {

  RUNNING("Running"),
  SUCCESSFUL("Successful"),
  ERROR("Error")

  String displayName

  JobState(String displayName) {
    this.displayName = displayName
  }
}
