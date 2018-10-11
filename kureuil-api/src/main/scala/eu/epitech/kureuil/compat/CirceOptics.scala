package eu.epitech.kureuil.compat

import cats.Applicative
import cats.instances.vector._
import cats.syntax.functor._
import cats.syntax.traverse._
import io.circe.Json
import monocle.Prism
import monocle.Traversal
import monocle.function.Plated

object CirceOptics {
  final lazy val jsonString: Prism[Json, String] = Prism[Json, String]( _.asString )( Json.fromString )

  implicit final lazy val jsonPlated: Plated[Json] = new Plated[Json] {
    val plate: Traversal[Json, Json] = new Traversal[Json, Json] {
      def modifyF[F[_]]( f: Json => F[Json] )( a: Json )( implicit F: Applicative[F] ): F[Json] = {
        a.fold(
          F.pure( a ),
          b => F.pure( Json.fromBoolean( b ) ),
          n => F.pure( Json.fromJsonNumber( n ) ),
          s => F.pure( Json.fromString( s ) ),
          _.traverse( f ).map( Json.fromValues ),
          _.traverse( f ).map( Json.fromJsonObject )
        )
      }
    }
  }
}
