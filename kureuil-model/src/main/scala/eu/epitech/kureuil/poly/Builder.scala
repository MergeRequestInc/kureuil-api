package eu.epitech.kureuil
package poly

import shapeless.:+:
import shapeless.CNil
import shapeless.Coproduct
import shapeless.Poly1
import shapeless.PolyDefns.Case1
import shapeless.ops.coproduct.Inject

trait Builder[From <: Coproduct, Out] {

  def append[I]( input: I )( implicit inj: Inject[From, I] ): this.type =
    appendCoproduct( inj( input ) )

  type Input = From

  def appendCoproduct( f: From ): this.type

  def build: Out
}

trait CasesBuilder[From <: Coproduct, Out] extends Builder[From, Out] {

  type This = this.type

  val poly: Poly1

  implicit def applyPoly: Cases.Aux[poly.type, From, this.type]

  override final def appendCoproduct( f: From ): this.type = applyPoly( poly, f )
}

trait SimpleBuilder[In, Out] extends Builder[In :+: CNil, Out] {

  def appendSimple( input: In ): this.type

  override final def appendCoproduct( in: In :+: CNil ): this.type =
    in.eliminate( appendSimple, _.impossible )

}

trait Cases[P <: Poly1, C <: Coproduct] {
  type Out

  def apply( p: P, c: C ): Out
}

object Cases {
  type Aux[P <: Poly1, C <: Coproduct, Out0] = Cases[P, C] { type Out = Out0 }

  implicit def cnilCases[P <: Poly1, Out0]: Cases.Aux[P, CNil, Out0] =
    new Cases[P, CNil] {
      type Out = Out0

      def apply( p: P, c: CNil ): Out0 = c.impossible
    }

  implicit def copCases[P <: Poly1, A, C <: Coproduct, Out0](
      implicit tail: Cases.Aux[P, C, Out0],
      case1: Case1.Aux[P, A, Out0]
  ): Cases.Aux[P, A :+: C, Out0] =
    new Cases[P, A :+: C] {
      type Out = Out0

      def apply( p: P, c: A :+: C ): Out0 =
        c.eliminate( case1.apply( _ ), tail.apply( p, _ ) )
    }
}
