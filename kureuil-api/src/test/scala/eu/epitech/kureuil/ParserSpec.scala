package eu.epitech.kureuil

import org.scalatest._

class ParserSpec extends WordSpec with Matchers {

  import atto.Atto._
  import Parser._

  val tag1: String        = "cpp"
  val tag2: String        = "cpp11"
  val tag3: String        = "cpp14"
  val or: LogicalOperator = LogicalOperator.Or
  val query: String       = s"#$tag1($tag2 ${or.entryName} $tag3)"

  "Parsing word" should {
    "match keyword" in {
      val parsed = word.parseOnly( tag1 ).option
      parsed.fold( fail( "Cannot parse word correctly" ) )( _ shouldEqual "cpp" )
    }
  }

  "Parsing logical operator" should {
    "return or operator" in {
      val parsed = logical_operator.parseOnly( "or" ).option
      parsed.fold( fail( "Cannot parse logical operator correctly" ) )( _ shouldEqual LogicalOperator.Or )
    }
  }

  "Parsing query" should {
    "return correct expression" in {
      val parsed = expr.parseOnly( query ).option
      parsed.fold( fail( "Cannot parse expression from query string" ) ) {
        case Expr( tag, addition ) =>
          tag.word shouldEqual tag1
          addition shouldBe empty
          tag.searchWord shouldBe defined
          tag.searchWord.map {
            case SearchWord( word2, addition2 ) =>
              word2 shouldEqual tag2
              addition2 shouldBe defined
              addition2.map {
                case ( lo, SearchWord( word3, addition3 ) ) =>
                  lo shouldEqual LogicalOperator.Or
                  word3 shouldEqual tag3
                  addition3 shouldBe empty
              }
          }
      }
    }
  }
}
