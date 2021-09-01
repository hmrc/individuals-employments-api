import play.core.PlayVersion
import sbt.Keys.compile
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.ExternalService
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.ServiceManagerPlugin.Keys.itDependenciesList

val appName = "individuals-employments-api"
val hmrc = "uk.gov.hmrc"

lazy val playSettings: Seq[Setting[_]] = Seq(
  routesImport ++= Seq(
    "uk.gov.hmrc.domain._",
    "uk.gov.hmrc.individualsemploymentsapi.domain._",
    "uk.gov.hmrc.individualsemploymentsapi.Binders._"))

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;" +
      ".*BuildInfo.;uk.gov.hmrc.BuildInfo;.*Routes;.*RoutesPrefix*;" +
      ".*definition*;",
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val appDependencies: Seq[ModuleID] = compile ++ test()
lazy val plugins: Seq[Plugins] = Seq.empty
lazy val externalServices =
  List(ExternalService("AUTH"), ExternalService("INDIVIDUALS_MATCHING_API"), ExternalService("DES"))
def intTestFilter(name: String): Boolean = name startsWith "it"
def unitFilter(name: String): Boolean = name startsWith "unit"
def componentFilter(name: String): Boolean = name startsWith "component"
lazy val ComponentTest = config("component") extend Test

val akka = "com.typesafe.akka"

val akkaVersion = "2.6.15"
val akkaHttpVersion = "10.2.6"

val overrides = Seq(
  akka %% "akka-stream" % akkaVersion,
  akka %% "akka-protobuf" % akkaVersion,
  akka %% "akka-slf4j" % akkaVersion,
  akka %% "akka-actor" % akkaVersion,
  akka %% "akka-actor-typed" % akkaVersion,
  akka %% "akka-serialization-jackson" % akkaVersion,
  akka %% "akka-http-core" % akkaHttpVersion
)

val compile = Seq(
  ws,
  hmrc                %% "bootstrap-backend-play-28" % "5.10.0",
  hmrc                %% "domain"                    % "6.2.0-play-28",
  hmrc                %% "play-hal"                  % "2.1.0-play-27",
  hmrc                %% "play-hmrc-api"             % "6.4.0-play-28",
  hmrc                %% "mongo-caching"             % "7.0.0-play-28",
  hmrc                %% "json-encryption"           % "4.10.0-play-28",
  "com.typesafe.play" %% "play-json-joda"            % "2.9.2"
)

def test(scope: String = "test,it") = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0"             % scope,
  "org.scalatestplus"      %% "mockito-3-4"              % "3.2.1.0"           % scope,
  "com.vladsch.flexmark"   % "flexmark-all"              % "0.35.10"           % scope,
  "org.scalaj"             %% "scalaj-http"              % "2.4.2"             % scope,
  "org.pegdown"            % "pegdown"                   % "1.6.0"             % scope,
  "com.github.tomakehurst" % "wiremock-jre8"             % "2.27.2"            % scope,
  hmrc                     %% "reactivemongo-test"       % "5.0.0-play-28"     % scope,
  hmrc                     %% "service-integration-test" % "1.1.0-play-28"     % scope
)

lazy val microservice =
  Project(appName, file("."))
    .enablePlugins(Seq(
      play.sbt.PlayScala,
      SbtAutoBuildPlugin,
      SbtGitVersioning,
      SbtDistributablesPlugin,
      SbtArtifactory) ++ plugins: _*)
    .settings(playSettings: _*)
    .settings(scalaSettings: _*)
    .settings(scoverageSettings: _*)
    .settings(publishingSettings: _*)
    .settings(scalaVersion := "2.12.11")
    .settings(defaultSettings(): _*)
    .settings(
      libraryDependencies ++= appDependencies,
      testOptions in Test := Seq(Tests.Filter(unitFilter)),
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    )
    .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources")
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(itDependenciesList := externalServices)
    .settings(
      dependencyOverrides ++= overrides,
      Keys.fork in IntegrationTest := false,
      unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "test")).value,
      testOptions in IntegrationTest := Seq(Tests.Filter(intTestFilter)),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
      parallelExecution in IntegrationTest := false
    )
    .configs(ComponentTest)
    .settings(inConfig(ComponentTest)(Defaults.testSettings): _*)
    .settings(
      testOptions in ComponentTest := Seq(Tests.Filter(componentFilter)),
      unmanagedSourceDirectories in ComponentTest := (baseDirectory in ComponentTest)(base => Seq(base / "test")).value,
      testGrouping in ComponentTest := oneForkedJvmPerTest((definedTests in ComponentTest).value),
      parallelExecution in ComponentTest := false
    )
    .settings(resolvers ++= Seq(
      Resolver.jcenterRepo
    ))
    .settings(PlayKeys.playDefaultPort := 9651)
    .settings(majorVersion := 0)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }

lazy val compileAll = taskKey[Unit]("Compiles sources in all configurations.")

compileAll := {
  val a = (compile in Test).value
  val b = (compile in IntegrationTest).value
  ()
}
