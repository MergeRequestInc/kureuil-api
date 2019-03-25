package eu.epitech.kureuil

import atto.Atto._
import atto._
import cats.syntax.option._
import enumeratum._

/*
 * <expr> ::= (<search_tag> | <word>) [ <logical_operator> <expr> ]
 * <search_tag> ::= '#' <word> [ '(' <search_word> ')' ]
 * <search_word> ::= <word> [ <logical_operator> <search_word> ]
 * <word> ::= [<digit><letter>\-_\s]*
 * <logical_operator> ::= 'or' | 'and'
 * <digit> ::= [0-9]
 * <letter> ::= ['a'-'z''A'-'Z']
 */
object Parser {

  sealed abstract class LogicalOperator( override val entryName: String )
      extends EnumEntry
      with Product
      with Serializable
  object LogicalOperator extends Enum[LogicalOperator] {
    final case object Or  extends LogicalOperator( "or" )
    final case object And extends LogicalOperator( "and" )
    override def values: IndexedSeq[LogicalOperator] = findValues
  }
  case class SearchWord( word: String, addition: Option[( LogicalOperator, SearchWord )] )
  case class SearchTag( word: String, searchWord: Option[SearchWord] )
  case class Expr( searchTag: SearchTag, addition: Option[( LogicalOperator, Expr )] )

  val dashOrUnderscore: Parser[Char] = char( '_' ) | char( '-' )

  val word: Parser[String] =
    many( letterOrDigit | dashOrUnderscore )
      .map( _.mkString ) <~ opt( spaceChar | whitespace )

  val logical_operator: Parser[LogicalOperator] =
    (string( "or" ) | string( "and" )).map( LogicalOperator.withName ) <~ (spaceChar | whitespace)

  val additional_word: Parser[( LogicalOperator, SearchWord )] =
    logical_operator ~ word.map( SearchWord( _, None ) )

  val search_word_parser: Parser[SearchWord] =
    for {
      wo  <- word
      add <- opt( additional_word )
    } yield SearchWord( wo, add )

  val search_tag: Parser[SearchTag] =
    for {
      _  <- char( '#' )
      w  <- word
      _  <- char( '(' )
      sw <- search_word_parser
      _  <- char( ')' )
    } yield SearchTag( w, sw.some )

  val additional_expr: Parser[( LogicalOperator, Expr )] =
    logical_operator ~ (search_tag | word.map( SearchTag( _, None ) )).map( Expr( _, None ) )

  val expr: Parser[Expr] =
    for {
      first  <- search_tag | word.map( SearchTag( _, None ) )
      others <- opt( additional_expr )
    } yield
      Expr(
        first,
        others
      )
}
