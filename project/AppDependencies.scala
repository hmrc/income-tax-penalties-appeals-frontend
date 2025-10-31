import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.3.0"
  

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"        %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"        %% "play-frontend-hmrc-play-30" % "12.19.0",
    "uk.gov.hmrc.mongo"  %% "hmrc-mongo-play-30"         % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"    %% "bootstrap-test-play-30"     % bootstrapVersion    % Test,
    "org.jsoup"      %  "jsoup"                      % "1.21.2"            % Test,
    "org.scalamock"   %% "scalamock"                % "7.5.0"              % Test,
  )

  val it: Seq[Nothing] = Seq.empty
}
