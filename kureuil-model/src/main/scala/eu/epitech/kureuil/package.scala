package eu.epitech

import mouse.option._
import scala.concurrent.Future

package object kureuil {
  import scala.collection.{immutable => sci}

  type Seq[+A] = sci.Seq[A]
  val Seq: sci.Seq.type = sci.Seq

  type IndexedSeq[+A] = sci.IndexedSeq[A]
  val IndexedSeq: sci.IndexedSeq.type = sci.IndexedSeq

  type Set[A] = sci.Set[A]
  val Set: sci.Set.type = sci.Set

  implicit class ToOptionFutureOps[A]( val self: Option[A] ) extends AnyVal {

    def toFuture( err: => Throwable ): Future[A] =
      self.cata( Future.successful, Future.failed( err ) )

  }

}
