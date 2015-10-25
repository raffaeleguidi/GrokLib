lazy val root = (project in file(".")).
  settings(
    name := "GrokLib",
    version := "1.0",
    scalaVersion := "2.11.4",
    libraryDependencies += "org.jruby.joni" % "joni" % "2.1.8"
  )
