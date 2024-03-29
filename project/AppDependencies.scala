import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  val hmrc = "uk.gov.hmrc"
  val hmrcMongo = s"$hmrc.mongo"
  val mongoVersion = "1.7.0"
  val bootstrapVersion = "8.4.0"
  val playVersion = "play-30"

  val compile: Seq[ModuleID] = Seq(
    ws,
    hmrc      %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    hmrc      %% s"domain-$playVersion"            % s"9.0.0",
    hmrc      %% s"play-hal-$playVersion"          % s"4.0.0",
    hmrc      %% s"crypto-json-$playVersion"       % s"7.6.0",
    hmrcMongo %% s"hmrc-mongo-$playVersion"        % mongoVersion
  )

  def test(scope: String = "test, it"): Seq[ModuleID] = Seq(
    hmrc                   %% s"bootstrap-test-$playVersion" % bootstrapVersion % scope,
    "org.scalatestplus"    %% "mockito-3-4"                  % "3.2.10.0"       % scope,
    "org.scalatestplus"    %% "scalacheck-1-17"              % "3.2.17.0"       % scope,
    "com.vladsch.flexmark" % "flexmark-all"                  % "0.64.8"         % scope,
    "org.scalaj"           %% "scalaj-http"                  % "2.4.2"          % scope,
  )
}
