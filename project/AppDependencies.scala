import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.13.0"
  

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"        %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"        %% "play-frontend-hmrc-play-30" % "12.6.0",
    "uk.gov.hmrc.mongo"  %% "hmrc-mongo-play-30"         % "2.6.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"    %% "bootstrap-test-play-30"     % bootstrapVersion    % Test,
    "org.jsoup"      %  "jsoup"                      % "1.20.1"            % Test
  )

  val it: Seq[Nothing] = Seq.empty
}
