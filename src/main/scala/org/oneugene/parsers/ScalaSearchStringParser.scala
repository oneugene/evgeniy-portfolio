package org.oneugene.parsers

import scala.util.parsing.combinator.JavaTokenParsers
import org.apache.commons.lang3.StringEscapeUtils

object ScalaSearchStringParser extends JavaTokenParsers {

  private def valueTransformer(x: ~[~[String, String], Seq[String]]) = {
    ValueOpPredicate(x._1._2, x._1._1, x._2)
  }

  val attribute: Parser[String] = """[A-Za-z]\w*""".r ^^ { _.toString }

  val valueString: Parser[String] = stringLiteral ^^ { x => StringEscapeUtils.unescapeJava(x.substring(1, x.length() - 1)) }

  val valueSingleton: Parser[Seq[String]] = valueString ^^ { Seq(_) }

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

  /**/
  private val subOr = "OR" ~ and
  private val and: Parser[Predicate] = factor ~ rep(subAnd) ^^ {
    case first ~ theRest =>
      theRest.foldLeft(first) { (acc, cur) =>
        cur match {
          case oper ~ next =>
            CompositePredicate(oper, Seq(acc, next))
        }
      }
  }
  private val subAnd = "AND" ~ factor

  val expression: Parser[Predicate] = and ~ rep(subOr) ^^ {
    case first ~ theRest =>
      theRest.foldLeft(first) { (acc, cur) =>
        cur match {
          case oper ~ next =>
            CompositePredicate(oper, Seq(acc, next))
        }
      }
  }
  private def factor: Parser[Predicate] = allPredicateParsers2 | "(" ~> expression <~ ")"
}

sealed trait Predicate
case class CompositePredicate(operation: String, children: Seq[Predicate]) extends Predicate

case class ValueOpPredicate(operation: String, attribute: String, values: Seq[String]) extends Predicate
