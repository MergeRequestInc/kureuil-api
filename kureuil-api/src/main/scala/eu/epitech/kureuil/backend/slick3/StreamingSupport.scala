package eu.epitech.kureuil
package backend
package slick3

import akka.stream.ActorMaterializer
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import cats.instances.future._
import cats.syntax.functor._
import scala.concurrent.Future
import shapeless.Coproduct
import shapeless.ops.coproduct.Inject
//
import poly.Builder

trait StreamingSupport extends DbContext { self: Tables =>

  implicit val materializer: Materializer = ActorMaterializer()

  import profile.api._
  import slick.jdbc.ResultSetConcurrency
  import slick.jdbc.ResultSetType

  override def close(): Future[Unit] = {
    system.terminate().andThen { case _ => super.close() }.void
  }

  private def decorateAction[R, A]( action: DBIOAction[R, Streaming[A], DML] ): DBIOAction[R, Streaming[A], DML] =
    action
      .withStatementParameters(
        rsType = ResultSetType.ForwardOnly,
        rsConcurrency = ResultSetConcurrency.ReadOnly,
        fetchSize = 50
      )
      .transactionally

  def foldStream[A, B <: Coproduct, C]( action: DBIOAction[_, Streaming[A], DML], builder: Builder[B, C] )(
      implicit inj: Inject[B, A]
  ): DmlIO[builder.type] = {

    DmlIO.from(
      Source
        .fromPublisher( db.stream( decorateAction( action ) ) )
        .runFold[builder.type]( builder )( _.append( _ ) )
    )

  }
}
