package eu.epitech.kureuil
package backend
package slick3

import cats.syntax.either._
import com.typesafe.config.Config
import javax.sql.DataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.postgresql.ds.common.BaseDataSource

import scala.collection.JavaConverters._

trait Migration {

  protected def flyway( config: Config ): Flyway = {
    val f = new Flyway()

    val ds: DataSource = getDataSource( config )

    f.setDataSource( ds )
    f.setValidateOnMigrate( true )
    f.setLocations( "classpath:db.migration" )

    f.setBaselineOnMigrate( config.getBoolean( "flyway.baseline-on-migrate" ) )
    f.setBaselineVersion(
      MigrationVersion.fromVersion( config.getString( "flyway.baseline-version" ) )
    )

    f
  }

  def flywaySupport( key: String ): Boolean = {
    key match {
      case "databaseName" | "serverName" | "portNumber" => false
      case _                                            => true
    }
  }

  private def getDataSource( config: Config ): DataSource = {
    val dbConfig = config.getConfig( "db.properties" )
    val ds = Class
      .forName( config.getString( "db.dataSourceClass" ) )
      .newInstance()
      .asInstanceOf[BaseDataSource with DataSource]

    def setConfigProperty( k: String ): Unit = k match {
      case "databaseName" => ds.setDatabaseName( dbConfig.getString( k ) )
      case "serverName"   => ds.setServerName( dbConfig.getString( k ) )
      case "portNumber"   => ds.setPortNumber( dbConfig.getInt( k ) )
      case _              => ds.setProperty( k, dbConfig.getValue( k ).unwrapped().toString )
    }

    dbConfig.entrySet().asScala.foreach( e => setConfigProperty( e.getKey ) )

    ds
  }

  def prepare( config: Config, repair: Boolean, path: String = "" ): Either[Throwable, Int] = {
    val resolvedConfig = if (path.isEmpty) config else config.getConfig( path )

    for {
      _          <- if (repair) Either.catchNonFatal( flyway( resolvedConfig ).repair() ) else Right( () )
      migrations <- Either.catchNonFatal( flyway( resolvedConfig ).migrate() )
    } yield migrations

  }
}

object Migration extends Migration
