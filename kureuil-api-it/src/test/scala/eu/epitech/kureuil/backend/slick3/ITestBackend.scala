package eu.epitech.kureuil
package backend
package slick3

import akka.actor.ActorSystem
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory

import scala.concurrent.Future
import scala.util.Properties
import scala.util.Try
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class ITestBackend( dbCfg: DatabaseConfig[JdbcProfile], system: ActorSystem )
    extends Backend.PostgresBackend( dbCfg )( system )

object ITestBackend {

  val ConfigKey = "kureuil-db"

  def config: Config =
    ConfigFactory
      .load()
      .withStringOverride( s"$ConfigKey.db.properties.host" )
      .withIntOverride( s"$ConfigKey.db.properties.port" )
      .withStringOverride( s"$ConfigKey.db.properties.databaseName" )
      .withStringOverride( s"$ConfigKey.db.properties.user" )
      .withStringOverride( s"$ConfigKey.db.properties.password" )

  def dbConfig( config: Config ): DatabaseConfig[JdbcProfile] =
    DatabaseConfig.forConfig( ConfigKey, config, getClass.getClassLoader )

  def backend: ITestBackend = {
    val numMigrations = ITestMigration.prepare( config, repair = false, ConfigKey ).fold( throw _, identity )
    Loggers.Main.info( s"$numMigrations migration(s) applied" )
    val system = ActorSystem( "kureuil-api-tests" )
    new ITestBackend( dbConfig( config ), system )
  }

  // Reset
  implicit def itBackendTestDatabase(
      implicit del: TestDatabase[Backend]
  ): TestDatabase[ITestBackend] = {
    import slick.dbio.DBIOAction
    import slick.dbio.Effect
    import slick.dbio.NoStream
    import slick.jdbc.SimpleJdbcAction

    def sqlAction( query: String ): DBIOAction[Int, NoStream, Effect.All] =
      SimpleJdbcAction( _.session.withStatement[Int]()( _.executeUpdate( query ) ) )

    new TestDatabase[ITestBackend] {

      override def reset( b: ITestBackend ): Future[Unit] = {
        import b._

        val tables = List(
          b.apiTokens,
          b.users,
          b.channels,
          b.urls,
          b.tags
        )

        val truncateStatements = tables
          .map( _.baseTableRow.tableName )
          .map( t => s"DELETE FROM $t;" )
          .reduce( _ + "\n" + _ )

        val truncateTables = sqlAction(
          s"""|BEGIN;
              |$truncateStatements
              |COMMIT;
              |""".stripMargin
        )

        db.run( truncateTables ).map( _ => () )
      }

      override def addOrUpdateApiToken( b: ITestBackend )( token: ApiToken, secret: String ): Future[Unit] =
        del.addOrUpdateApiToken( b )( token, secret )

      override def asKureuilDatabase( db: ITestBackend ): KureuilDatabase = db
    }
  }

  // Config
  private implicit class ConfigExt( val self: Config ) extends AnyVal {
    def withOverride( path: String, value: Option[Any] ): Config =
      value.fold( self )( ov => self.withValue( path, ConfigValueFactory.fromAnyRef( ov ) ) )

    private def stringEnv( path: String ) = Properties.envOrNone( path )

    private def intEnv( path: String ) =
      Properties.envOrNone( path ).flatMap( e => Try( e.toInt ).toOption )

    def withStringOverride( path: String ): Config =
      withOverride( path, stringEnv( path ) )

    def withIntOverride( path: String ): Config =
      withOverride( path, intEnv( path ) )
  }

}
