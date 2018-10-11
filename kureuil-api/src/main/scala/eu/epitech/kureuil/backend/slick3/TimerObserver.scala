package eu.epitech.kureuil
package backend
package slick3

import org.lyranthe.prometheus.client.LabelledHistogram
import org.lyranthe.prometheus.client.Timer
import scala.concurrent.Future

trait TimerObserver extends DbContext { self: Tables =>
  def observeDbTime[T]( histogram: LabelledHistogram, runnable: DmlIO[T] ): Future[T] = {
    val timer = Timer()
    val run   = db.run( runnable )
    run.andThen { case _ => histogram.observeDuration( timer ) }
    run
  }
}
