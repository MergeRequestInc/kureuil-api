package eu.epitech.kureuil

import java.sql.SQLException

package object backend {
  def sqlExceptionStream( e: Throwable ): Stream[SQLException] = e match {
    case ex: SQLException =>
      val next = ex.getNextException
      if (next == null) Stream()
      else next #:: sqlExceptionStream( next )
    case _ => Stream()
  }
}
