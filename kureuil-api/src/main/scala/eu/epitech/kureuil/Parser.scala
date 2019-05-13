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
    (string( "or" ) | string( "and" )).map( LogicalOperator.withName ) <~ opt( spaceChar | whitespace )

  val additional_word: Parser[( LogicalOperator, SearchWord )] =
    logical_operator ~ word.map( SearchWord( _, None ) )

  val search_word_parser: Parser[SearchWord] =
    for {
      wo  <- word
      add <- opt( additional_word )
    } yield SearchWord( wo, add )

  val search_word_paren: Parser[SearchWord] =
    for {
      _  <- char( '(' )
      sw <- search_word_parser
      _  <- char( ')' )
    } yield sw

  val search_tag: Parser[SearchTag] =
    for {
      _  <- char( '#' )
      w  <- word
      sw <- opt( search_word_paren )
    } yield SearchTag( w, sw )

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

  def parseBnf( bnf: String ): Option[Expr] = {
    expr
      .parseOnly( bnf )
      .option
  }

  /*
   * This method return an Option of a bool as follow :
   * None = this tag is not present inside the bnf
   * Some(false) = this tag is present but is not mandatory
   * Some(true) = this tag is present and is mandatory
   */
  def isMandatoryOrAbsent( expr: Expr, tag: String ): Option[Boolean] = {
    isMandatoryOrAbsent( expr, tag, andOr = true )
  }

  def isMandatoryOrAbsent( expr: Expr, tag: String, andOr: Boolean ): Option[Boolean] = {
    expr match {
      case Expr( searchTag, _ ) if isMandatoryOrAbsent( searchTag, tag, andOr ).isDefined =>
        isMandatoryOrAbsent( searchTag, tag, andOr )
      case Expr( _, Some( ( lo, ex ) ) ) if lo == LogicalOperator.And => isMandatoryOrAbsent( ex, tag, true )
      case Expr( _, Some( ( lo, ex ) ) ) if lo == LogicalOperator.Or  => isMandatoryOrAbsent( ex, tag, false )
      case _                                                          => None
    }
  }

  def isMandatoryOrAbsent( word: SearchWord, tag: String, andOr: Boolean ): Option[Boolean] = {
    word match {
      case Parser.SearchWord( w, _ ) if w.equals( tag ) => andOr.some
      case Parser.SearchWord( _, Some( ( lo, w ) ) )    => isMandatoryOrAbsent( w, tag, lo == LogicalOperator.And )
      case _                                            => None
    }
  }

  def isMandatoryOrAbsent( searchTag: SearchTag, tag: String, andOr: Boolean ): Option[Boolean] = {
    searchTag match {
      case SearchTag( w, _ ) if w.equals( tag ) => andOr.some
      case SearchTag( _, Some( w ) )            => isMandatoryOrAbsent( w, tag, andOr = true )
      case _                                    => None
    }
  }
}
