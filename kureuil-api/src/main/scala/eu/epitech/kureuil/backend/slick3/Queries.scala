package eu.epitech.kureuil
package backend
package slick3

import mouse.option._
import scala.{collection => sc}
import scala.util.Failure
import scala.util.Success
import slick.ast.BaseTypedType
import slick.dbio.Effect.Read
import slick.sql.FixedSqlStreamingAction

trait Queries { self: DbContext =>

  import profile.api._

  def columnCriterion[P, T <: Table[_]]( f: T => Rep[P] )( expected: Option[P] )(
      implicit ev: BaseTypedType[P]
  ): T => Rep[Boolean] =
    expected.fold[T => Rep[Boolean]]( _ => true: Rep[Boolean] )( exp => r => f( r ) === exp )

  type OptCriterion[T] = Rep[Option[T]] => Rep[Option[Boolean]]

  def columnCriterionIfPresent[P, T <: Table[_]]( f: T => Rep[P] )( expected: Option[P] )(
      implicit ev: BaseTypedType[P],
      ev2: slick.lifted.OptionLift[T, Rep[Option[T]]]
  ): OptCriterion[T] =
    expected.cata[OptCriterion[T]](
      exp => opt => opt.map( t => f( t ) === exp ),
      _ => Some( true ): Rep[Option[Boolean]]
    )

  def relationCriterion[P, T <: Table[_]]( f: T => Rep[P] )(
      expected: Rep[P]
  )( implicit ev: BaseTypedType[P], ev2: slick.lifted.OptionLift[T, Rep[Option[T]]] ): OptCriterion[T] =
    opt => opt.map( t => f( t ) === expected )

  def ignoreIntegrityConstraintViolations[A]( action: DmlIO[A] ): DmlIO[Unit] =
    action.asTry
      .flatMap {
        case Success( _ )                                 => DmlIO.successful( () )
        case Failure( IntegrityConstraintViolation( _ ) ) => DmlIO.successful( () )
        case Failure( err )                               => DmlIO.failed( err )
      }

  abstract class TableQueries[K, T, Ts <: Table[T]](
      val query: Query[Ts, T, sc.Seq],
      val tableKey: Ts => Rep[K],
      val criterion: T => Ts => Rep[Boolean],
      val setKey: ( T, K ) => T
  )(
      implicit K: BaseTypedType[K]
  ) {

    def getAll( t: T ): FixedSqlStreamingAction[sc.Seq[T], T, Read] = query.filter( criterion( t ) ).result

    def get( t: T ): DmlIO[T] = getAll( t ).head

    def insert( t: T ): DmlIO[T] =
      query.returning( query.map( tableKey ) ).into( setKey ) += t

    def recoverInsert[A]( insertAction: DmlIO[A] )( rec: => DmlIO[A] ): DmlIO[A] =
      insertAction.asTry
        .flatMap {
          case Success( res )                               => DmlIO.successful( res )
          case Failure( IntegrityConstraintViolation( _ ) ) => rec
          case Failure( err )                               => DmlIO.failed( err )
        }

  }

  class GetOrInsert[K, T, Ts <: Table[T]](
      query: Query[Ts, T, sc.Seq],
      tableKey: Ts => Rep[K],
      criterion: T => Ts => Rep[Boolean],
      setKey: ( T, K ) => T
  )(
      implicit K: BaseTypedType[K]
  ) extends TableQueries( query, tableKey, criterion, setKey )
      with ( T => DmlIO[T] ) {

    def getOption( t: T ): DmlIO[Option[T]] = getAll( t ).headOption

    def getOrInsert( t: T ): DmlIO[T] =
      for {
        opt <- getOption( t )
        res <- opt.cata( DmlIO.successful, insert( t ) )
      } yield res

    override def apply( t: T ): DmlIO[T] = {
      recoverInsert( getOrInsert( t ) )( get( t ) )
    }

  }

  class UpdateOrInsert[K, U, T, Ts <: Table[T]](
      query: Query[Ts, T, sc.Seq],
      tableKey: Ts => Rep[K],
      criterion: T => Ts => Rep[Boolean],
      setKey: ( T, K ) => T,
      val updateFields: Ts => Rep[U],
      val updateValue: T => U
  )(
      implicit K: BaseTypedType[K],
      U: BaseTypedType[U]
  ) extends TableQueries( query, tableKey, criterion, setKey )
      with ( T => DmlIO[T] ) {

    def update( t: T ): DmlIO[Int] =
      query
        .filter( criterion( t ) )
        .map( updateFields )
        .update( updateValue( t ) )

    def updateOrInsert( t: T ): DmlIO[T] =
      update( t ).flatMap {
        case 0 => insert( t )
        case _ => get( t )
      }

    override def apply( t: T ): DmlIO[T] =
      recoverInsert( updateOrInsert( t ) )( update( t ) >> get( t ) )

  }

}
