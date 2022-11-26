include(
    ":app", ":persistence",
    ":supplier:infrastructure", ":supplier:metal-archives", ":supplier:time-for-metal"
)

rootProject.name = "metal-release-butler"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
