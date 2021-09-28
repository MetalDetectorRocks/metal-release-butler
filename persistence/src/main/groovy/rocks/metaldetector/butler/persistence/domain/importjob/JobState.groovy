package rocks.metaldetector.butler.persistence.domain.importjob

enum JobState {

  RUNNING("Running"),
  SUCCESSFUL("Successful"),
  ERROR("Error")

  String displayName

  JobState(String displayName) {
    this.displayName = displayName
  }
}
