package eu.epitech.kureuil

import cats.data.NonEmptyList
import cats.data.NonEmptyVector
import scala.collection.immutable.VectorBuilder

object multimap {
  implicit class ToMultimapOps[A]( val self: Traversable[A] ) extends AnyVal {

    private def groupBy[T, U, C]( single: U => C, append: U => C => C )( implicit ev: A <:< ( T, U ) ): Map[T, C] =
      self.foldRight( Map[T, C]() ) { ( x, m ) =>
        val ( t, u ) = ev( x )
        m + (t -> m.get( t ).fold( single( u ) )( append( u )( _ ) ))
      }

    def toMultimapNel[T, U]( implicit ev: A <:< ( T, U ) ): Map[T, NonEmptyList[U]] =
      groupBy[T, U, NonEmptyList[U]]( u => NonEmptyList.of( u ), u => nel => u :: nel )

    def toMultimapVector[T, U]( implicit ev: <:<[A, ( T, U )] ): Map[T, Vector[U]] =
      groupBy[T, U, VectorBuilder[U]]( u => new VectorBuilder[U] += u, u => b => b += u ).mapValues( _.result )

    def toMultimapNev[T, U]( implicit ev: <:<[A, ( T, U )] ): Map[T, NonEmptyVector[U]] =
      groupBy[T, U, ( U, VectorBuilder[U] )](
        u => ( u, new VectorBuilder[U] ),
        u => { case ( h, b ) => ( h, b += u ) }
      ).mapValues { case ( h, b ) => NonEmptyVector( h, b.result() ) }

  }
}
