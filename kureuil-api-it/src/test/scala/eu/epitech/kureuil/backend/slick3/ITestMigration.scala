package eu.epitech.kureuil
package backend
package slick3

import com.typesafe.config.Config
import org.flywaydb.core.Flyway

object ITestMigration extends Migration {

  override def flyway( config: Config ): Flyway = {
    val f = super.flyway( config )

    f.setCleanOnValidationError( true )
    f.setIgnoreFutureMigrations( false )

    f
  }

}
