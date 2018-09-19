
// must be at least 2.11 to use hmt_textmodel
scalaVersion := "2.11.2"

resolvers += Resolver.jcenterRepo
resolvers += "beta" at "http://beta.hpcc.uh.edu/nexus/content/repositories/releases"
resolvers += Resolver.bintrayRepo("neelsmith", "maven")
libraryDependencies ++=   Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "edu.holycross.shot" %% "seqcomp" % "1.0.0",
    "edu.holycross.shot.cite" %% "xcite" % "3.6.0",
    "edu.holycross.shot" %% "cex" % "6.2.1",
    "edu.holycross.shot" %% "citerelations" % "2.3.0",
    "edu.holycross.shot" %% "ohco2" % "10.9.0",
    "edu.holycross.shot" %% "scm" % "6.1.1",
    "edu.holycross.shot" %% "citeobj" % "7.1.1",
    "co.theasi" %% "plotly" % "0.2.0",
	 "io.spray" %%  "spray-json" % "1.3.3"
)
