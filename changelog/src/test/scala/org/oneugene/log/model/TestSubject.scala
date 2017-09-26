package org.oneugene.log.model

import cats.data.Writer
import monocle.macros.GenLens
import monocle.{Lens, PLens}
import org.oneugene.log.play.PropertyChangeLens
import org.oneugene.log.replay.{LensReplayRecord, LensRepository}

case class TestSubject(value: Int)

/**
  * Contains lenses for [[TestSubject]] case class.
  * This code shows how to generate [[PropertyChangeLens]] without macros
  */
object TestSubjectLenses {

  import cats.implicits.{catsKernelStdMonoidForVector, catsSyntaxWriterId}

  private val setValue: Writer[Vector[String], Int] => TestSubject => Writer[Vector[String], TestSubject] = (fieldLog) => (source) =>
    fieldLog.flatMap(fieldValue =>
      if (source.value == fieldValue)
        source.writer(Vector.empty)
      else
        source.copy(value = fieldValue).writer(Vector("value")))


  val valueLens: PropertyChangeLens[TestSubject, Int] =
    PLens[TestSubject, Writer[Vector[String], TestSubject], Int, Writer[Vector[String], Int]](_.value)(setValue)
}


private[model] object TestSubjectRepository extends LensRepository[TestSubject] {
  private val valueLens: Lens[TestSubject, _] = GenLens[TestSubject](_.value)
  private val lenses: Map[String, Lens[TestSubject, _]] = Map[String, Lens[TestSubject, _]](
    "value" -> valueLens
  )

  override def resolve[B](propertyName: String): Either[String, LensReplayRecord[TestSubject, B]] = {
    val opt = lenses.get(propertyName).map(l =>
      LensReplayRecord(l.asInstanceOf[Lens[TestSubject, B]], Option.empty))
    Either.cond(opt.isDefined, opt.get, s"Unknown property $propertyName in class TestSubject")
  }
}
