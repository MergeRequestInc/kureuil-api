name := "kureuil-build"

resolvers += Classpaths.sbtPluginReleases
resolvers += Classpaths.typesafeReleases
resolvers += Resolver.sonatypeRepo( "releases" )
resolvers += "Flyway-sbt-1.0" at "https://davidmweber.github.io/flyway-sbt.repo"
resolvers += Resolver.url( "sbt-bintray-plugins-releases", url( "http://dl.bintray.com/sbt/sbt-plugin-releases" ) )(
  Resolver.ivyStylePatterns )

addSbtPlugin( "com.lightbend.sbt"     % "sbt-javaagent"                % "0.1.4" )
addSbtPlugin( "org.scoverage"         %% "sbt-scoverage"               % "1.5.1" )
addSbtPlugin( "com.timushev.sbt"      %% "sbt-updates"                 % "0.3.3" )
addSbtPlugin( "io.github.davidmweber" % "flyway-sbt"                   % "5.0.0" )
addSbtPlugin( "com.typesafe.sbt" % "sbt-git"             % "1.0.0" )
addSbtPlugin( "com.eed3si9n"     % "sbt-buildinfo"       % "0.9.0" )
addSbtPlugin( "com.geirsson"     % "sbt-scalafmt"        % "1.6.0-RC1" )
addSbtPlugin( "com.typesafe.sbt" % "sbt-native-packager" % "1.3.4" )


libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact( "jdeb", "jar", "jar" ) )
