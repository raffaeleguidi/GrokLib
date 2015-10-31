lazy val root = (project in file(".")).
  settings(
    name := "GrokLib",
    version := "1.0",
    scalaVersion := "2.11.4",
    libraryDependencies ++= Seq(
      "org.jruby.joni" % "joni" % "2.1.8",
      "com.github.scopt" %% "scopt" % "3.3.0"
    ),
    resolvers += Resolver.sonatypeRepo("public")
  )
