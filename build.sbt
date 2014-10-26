name := "freez"

scalaVersion := "2.11.2"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.chuusai" 	  %% "shapeless" % "2.1.0-SNAPSHOT" changing(),
  "org.scalaz"      %% "scalaz-core"      % "7.1.0-M7",
  "org.scalatest"   %  "scalatest_2.11"   % "2.1.3"             % "test",
  "org.scala-lang"  %  "scala-reflect"    % scalaVersion.value  % "provided",
  "org.scala-lang"  %  "scala-compiler"   % scalaVersion.value  % "test",
  "nl.grons"        %% "metrics-scala"    % "3.2.1_a2.3"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xlog-implicits")

fork in test := true


