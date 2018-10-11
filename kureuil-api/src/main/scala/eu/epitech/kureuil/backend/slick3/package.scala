package eu.epitech.kureuil
package backend

import java.sql.SQLException
import mouse.option._
import slick.dbio.DBIOAction
import slick.dbio.Effect
import slick.dbio.NoStream

package object slick3 {

  type DML      = Effect.Read with Effect.Write with Effect.Transactional
  type DmlIO[A] = DBIOAction[A, NoStream, DML]
  val DmlIO = slick.dbio.DBIOAction

  implicit class ToOptionDmlIOOps[A]( val self: Option[A] ) extends AnyVal {

    def toDmlIO( err: => Throwable ): DmlIO[A] =
      self.cata( DmlIO.successful, DmlIO.failed( err ) )

  }

  object IntegrityConstraintViolation {

    private val integrityConstraintViolation = """23\d\d\d""".r

    private def isIntegrityConstraintViolation: String => Boolean = {
      case integrityConstraintViolation() => true
      case _                              => false
    }

    def unapply( t: Throwable ): Option[SQLException] = t match {
      case e: SQLException if isIntegrityConstraintViolation( e.getSQLState ) => Some( e )
      case _                                                                  => None
    }
  }

}
