ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "tracker_v2"
  )

libraryDependencies ++= Seq(
  "ch.qos.logback"                % "logback-classic"                % "1.2.6",
  "org.slf4j"                     % "slf4j-api"                      % "1.7.32",
  "org.http4s"                    %% "http4s-blaze-server"           % "0.23.6",
  "org.http4s"                    %% "http4s-dsl"                    % "0.23.6",
  "org.http4s"                    %% "http4s-circe"                  % "0.23.6",
  "io.circe"                      %% "circe-generic"                 % "0.14.3",
  "org.postgresql"                % "postgresql"                     % "42.3.1",
  "org.tpolecat"                  %% "doobie-core"                   % "1.0.0-M5",
  "org.tpolecat"                  %% "doobie-postgres"               % "1.0.0-M5",
  "org.typelevel"                 %% "cats-core"                     % "2.6.1",
  "org.typelevel"                 %% "cats-effect"                   % "3.2.9",
  "org.typelevel"                 %% "cats-effect-testing-scalatest" % "1.4.0" % Test,
  "org.scalactic"                 %% "scalactic"                     % "3.2.10" % Test,
  "org.scalatest"                 %% "scalatest"                     % "3.2.10" % Test,
  "com.typesafe"                  % "config"                         % "1.4.1",
  "com.softwaremill.sttp.client3" %% "core"                          % "3.3.7",
  "org.typelevel"                 %% "cats-effect-kernel"            % "3.2.9",
  "org.jsoup"                     % "jsoup"                          % "1.15.4",
  "org.typelevel"                 %% "log4cats-slf4j"                % "2.3.1",
  "com.github.pureconfig"         %% "pureconfig"                    % "0.17.0",
  "com.github.pureconfig"         %% "pureconfig-cats-effect"        % "0.17.0",
  "org.flywaydb"                  % "flyway-core"                    % "7.15.0",
)

scalacOptions ++= Seq(
  "-Ymacro-annotations"
)
