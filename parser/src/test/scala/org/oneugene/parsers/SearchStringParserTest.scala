package org.oneugene.parsers

import scala.util.parsing.input.CharSequenceReader
import org.scalacheck.Gen
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import ScalaSearchStringParser._

class SearchStringParserTest extends FlatSpec with Matchers with PropertyChecks with OptionValues {

  val attrNameGen = for {
    root <- Gen.alphaStr.filter { x => x.length() > 0 }
    rootTail <- Gen.alphaNumChar
  } yield root + rootTail

  "Attribute parser" should "correctly parse correct attribute name" in {
    forAll(attrNameGen) { (attrStr: String) =>
      val input = new CharSequenceReader(attrStr)
      attribute(input) match {
        case Success(`attrStr`, _) =>
        case f => fail(f.toString)
      }
    }
  }

  "Attribute parser" should "fail on bad attribute names" in {
    val badNames = Gen.oneOf(Array("", "1", "g.g", "1qwe"))
    forAll(badNames) { (attrStr: String) =>
      val input = new CharSequenceReader(attrStr)
      attribute(input) match {
        case Success(`attrStr`, _) => fail(s"Success on invalid attr name: $attrStr")
        case _ =>
      }
    }
  }

  "Value list parser" should "work" in {
    val input = new CharSequenceReader("(\"qwerty1\", \"qwerty2\", \"qwerty3\")")
    valueList(input) match {
      case Success(values, _) =>
        values should contain theSameElementsInOrderAs Vector("qwerty1", "qwerty2", "qwerty3")
      case f => fail(f.toString)
    }
  }

  "Like parser" should "work" in {
    val input = new CharSequenceReader("LikeName1 LIKE \"qwerty\\\"1%\"")
    expression(input) match {
      case Success(ValueOpPredicate(operation, attributeName, values), _) =>
        operation should ===("LIKE")
        attributeName should ===("LikeName1")
        values should contain theSameElementsAs List("qwerty\"1%")
      case f => fail(f.toString)
    }
  }

  "Less or equals parser" should "work" in {
    val input = new CharSequenceReader("LeName <= \"1\"")
    expression(input) match {
      case Success(ValueOpPredicate(operation, attributeName, values), _) =>
        operation should ===("<=")
        attributeName should ===("LeName")
        values should contain theSameElementsAs List("1")
      case f => fail(f.toString)
    }
  }

  "Greater or equals parser" should "work" in {
    val input = new CharSequenceReader("GeName >= \"2015-01-01 00:00:00.000\"")
    expression(input) match {
      case Success(ValueOpPredicate(operation, attributeName, values), _) =>
        operation should ===(">=")
        attributeName should ===("GeName")
        values should contain theSameElementsAs List("2015-01-01 00:00:00.000")
      case f => fail(f.toString)
    }
  }

  "Equals parser" should "correctly parse single value" in {
    val input = new CharSequenceReader("EqName = \"qwerty\\\"1\"")
    expression(input) match {
      case Success(ValueOpPredicate(operation, attributeName, values), _) =>
        operation should ===("=")
        attributeName should ===("EqName")
        values should contain theSameElementsAs List("qwerty\"1")
      case f => fail(f.toString)
    }
  }

  "Equals parser" should "correctly parse multiple value" in {
    val input = new CharSequenceReader("EqName = (\"qwerty\\\"1\", \"qwerty2\")")
    expression(input) match {
      case Success(ValueOpPredicate(operation, attributeName, values), _) =>
        operation should ===("=")
        attributeName should ===("EqName")
        values should contain theSameElementsAs List("qwerty\"1", "qwerty2")
      case f => fail(f.toString)
    }
  }

  "Not equals parser" should "correctly parse single value" in {
    val input = new CharSequenceReader("NeName <> \"qwerty\\\"1\"")
    expression(input) match {
      case Success(ValueOpPredicate(operation, attributeName, values), _) =>
        operation should ===("<>")
        attributeName should ===("NeName")
        values should contain theSameElementsAs List("qwerty\"1")
      case f => fail(f.toString)
    }
  }

  "Not equals parser" should "correctly parse multiple value" in {
    val input = new CharSequenceReader("NeName <> (\"qwerty\\\"1\", \"qwerty2\")")
    expression(input) match {
      case Success(ValueOpPredicate(operation, attributeName, values), _) =>
        operation should ===("<>")
        attributeName should ===("NeName")
        values should contain theSameElementsAs List("qwerty\"1", "qwerty2")
      case f => fail(f.toString)
    }
  }

  "Between parser" should "correctly parse 2 values" in {
    val input = new CharSequenceReader("Beetween BETWEEN (\"qwerty\\\"1\", \"qwerty2\")")
    expression(input) match {
      case Success(ValueOpPredicate(operation, attributeName, values), _) =>
        operation should ===("BETWEEN")
        attributeName should ===("Beetween")
        values should contain theSameElementsAs List("qwerty\"1", "qwerty2")
      case f => fail(f.toString)
    }
  }

  "AND parser" should "correctly parse 2 children" in {
    val input = new CharSequenceReader("A = \"1\" AND B <> \"2\"")
    expression(input) match {
      case Success(CompositePredicate(operation, Seq(ValueOpPredicate(operation1, attribute1, values1), ValueOpPredicate(operation2, attribute2, values2))), _) =>
        operation should ===("AND")
        attribute1 should ===("A")
        operation1 should ===("=")
        values1 should contain theSameElementsAs List("1")
        attribute2 should ===("B")
        operation2 should ===("<>")
        values2 should contain theSameElementsAs List("2")
      case f => fail(f.toString)
    }
  }

  "OR parser" should "correctly parse 2 children" in {
    val input = new CharSequenceReader("A = \"1\" OR B <> \"2\"")
    expression(input) match {
      case Success(CompositePredicate(operation, Seq(ValueOpPredicate(operation1, attribute1, values1), ValueOpPredicate(operation2, attribute2, values2))), _) =>
        operation should ===("OR")
        attribute1 should ===("A")
        operation1 should ===("=")
        values1 should contain theSameElementsAs List("1")
        attribute2 should ===("B")
        operation2 should ===("<>")
        values2 should contain theSameElementsAs List("2")
      case f => fail(f.toString)
    }
  }

  "NOT parser" should "correctly parse 1 child" in {
    val input = new CharSequenceReader("NOT Not = \"1\"")
    expression(input) match {
      case Success(CompositePredicate(operation, Seq(ValueOpPredicate(operation1, attribute1, values1))), _) =>
        operation should ===("NOT")
        attribute1 should ===("Not")
        operation1 should ===("=")
        values1 should contain theSameElementsAs List("1")
      case f => fail(f.toString)
    }
  }

  "AND" should "be more important than OR" in {
    val query =
      """
a = "1" AND b="2" OR c="3" AND d="4"
      """
    val input = new CharSequenceReader(query)
    val res = expression(input)

    res match {
      case Success(predicate, _) =>
        predicate match {
          case CompositePredicate("OR", List(CompositePredicate("AND", _), CompositePredicate("AND", _))) =>
          case f => fail("expected OR with 2 and child, but got" + f)
        }
      case f => fail(f.toString)
    }
  }

  "Parenthesis" should "allow to set evaluation order" in {
    val query1 =
      """
a = "1" OR b="2" AND c="3"
      """
    val res1 = expression(new CharSequenceReader(query1))

    res1 match {
      case Success(predicate, _) =>
        predicate match {
          case CompositePredicate("OR", List(_, CompositePredicate("AND", _))) =>
          case f => fail("expected OR with AND child, but got" + f)
        }
      case f => fail(f.toString)
    }

    val query2 =
      """
(a = "1" OR b="2") AND c="3"
      """
    val res2 = expression(new CharSequenceReader(query2))

    res2 match {
      case Success(predicate, _) =>
        predicate match {
          case CompositePredicate("AND", List(CompositePredicate("OR", _), _)) =>
          case f => fail("expected AND with OR child, but got" + f)
        }
      case f => fail(f.toString)
    }
  }
  "Performance" should "be measured" in {
    val query =
      """
                  q = "1" AND c<="2" OR r>="3" AND u BETWEEN ("4", "5") AND (qwe <> "tryrty" OR l45 <= "asd")
      """
    val iters = 1000000
    val input = new CharSequenceReader(query)
    val start = System.currentTimeMillis()
    for (i <- 1 to iters) {
      val res = ScalaSearchStringParser.expression(input)
      //      println(res)
    }
    val end = System.currentTimeMillis()
    println(s"$iters took ${end - start} ms, ${1.0 * iters / (end - start)} per ms")
  }
}
