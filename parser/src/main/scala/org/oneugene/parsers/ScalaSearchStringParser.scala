package org.oneugene.parsers

import scala.util.parsing.combinator.JavaTokenParsers
import org.apache.commons.lang3.StringEscapeUtils

trait SearchStringParsers extends JavaTokenParsers {
  /**
    *
    * @return parser which can convert string into AST like tree represented by [[Predicate]]
    */
  def expression: Parser[Predicate]
}

object ScalaSearchStringParser extends SearchStringParsers {

  private def valueTransformer(x: ~[~[String, String], Seq[String]]) = {
    ValueOpPredicate(x._1._2, x._1._1, x._2)
  }

  val attribute: Parser[String] =
    """[A-Za-z]\w*""".r ^^ {
      _.toString
    }

  val valueString: Parser[String] = stringLiteral ^^ { x => StringEscapeUtils.unescapeJava(x.substring(1, x.length() - 1)) }

  val valueSingleton: Parser[Seq[String]] = valueString ^^ {
    Seq(_)
  }

  val valueCouple: Parser[Seq[String]] = "(" ~> ((valueString <~ ",") ~ valueString) <~ ")" ^^ { x => Seq(x._1, x._2) }

  val valueList: Parser[Seq[String]] = "(" ~> (valueString ~ rep("," ~> valueString)) <~ ")" ^^ { x => x._1 :: x._2 }

  val likeParser: Parser[ValueOpPredicate] = attribute ~ "LIKE" ~ valueSingleton ^^ valueTransformer

  val leParser: Parser[ValueOpPredicate] = attribute ~ "<=" ~ valueSingleton ^^ valueTransformer

  val geParser: Parser[ValueOpPredicate] = attribute ~ ">=" ~ valueSingleton ^^ valueTransformer

  val eqParser: Parser[ValueOpPredicate] = (attribute ~ "=" ~ (valueSingleton | valueList)) ^^ valueTransformer

  val neParser: Parser[ValueOpPredicate] = (attribute ~ "<>" ~ (valueSingleton | valueList)) ^^ valueTransformer

  val betweenParser: Parser[ValueOpPredicate] = (attribute ~ "BETWEEN" ~ valueCouple) ^^ valueTransformer

  val valueOpParsers: Parser[ValueOpPredicate] = eqParser | likeParser | leParser | geParser | neParser | betweenParser

  def notParser: Parser[Predicate] = ("NOT" ~ allPredicateParsers2) ^^ { x => CompositePredicate(x._1, Seq(x._2)) }

  val allPredicateParsers: Parser[Predicate] = valueOpParsers | notParser

  def allPredicateParsers2 = allPredicateParsers | ("(" ~> allPredicateParsers <~ ")")


  private val factor: Parser[Predicate] = allPredicateParsers2 | "(" ~> expression <~ ")"

  /**/
  private val and: Parser[Predicate] = factor ~ rep("AND" ~ factor) ^^ {
    case first ~ theRest =>
      theRest.foldLeft(first) { (acc, cur) =>
        cur match {
          case oper ~ next =>
            CompositePredicate(oper, Seq(acc, next))
        }
      }
  }

  def expression: Parser[Predicate] = and ~ rep("OR" ~ and) ^^ {
    case first ~ theRest =>
      theRest.foldLeft(first) { (acc, cur) =>
        cur match {
          case oper ~ next =>
            CompositePredicate(oper, Seq(acc, next))
        }
      }
  }

}

/**
  * Base trait for AST-like nodes
  */
sealed trait Predicate

/**
  * Value object for AST-like tree node which contains information about an operation on other AST-like nodes
  *
  * @constructor creates new [[Predicate]] with operation and children to apply the operation to
  * @param operation the logical operation to apply to the children
  * @param children the nodes of the tree
  */
case class CompositePredicate(operation: String, children: Seq[Predicate]) extends Predicate

/**
  * Value object for AST-like tree node which contains information about an operation on a list of values
  *
  * @constructor creates new object for AST-like tree node which contains information about an operation on a list of values
  * @param operation the operation to apply to the variable reference and sequence of values
  * @param attribute the variable name
  * @param values the sequence of string values
  */
case class ValueOpPredicate(operation: String, attribute: String, values: Seq[String]) extends Predicate
