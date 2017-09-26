package org.oneugene.log.model

import cats.data.Writer
import monocle.PLens
import monocle.macros.GenLens
import org.oneugene.log.play.PropertyChangeLens
import org.oneugene.log.replay.{LensReplayRecord, LensRepository, PropertyChangeReplayImpl}

case class TestContainerObject(reference: TestSubject, name: String)

/**
  * Contains lenses for [[TestContainerObject]] case class.
  * This code shows how to generate [[PropertyChangeLens]] without macros
  */
object TestContainerObjectLenses {

  import cats.implicits.{catsKernelStdMonoidForVector, catsSyntaxWriterId}


  private val setName: Writer[Vector[String], String] => TestContainerObject => Writer[Vector[String], TestContainerObject] = (fieldLog) => (source) =>
    fieldLog.flatMap(fieldValue =>
      if (source.name == fieldValue)
        source.writer(Vector.empty)
      else
        source.copy(name = fieldValue).writer(Vector("name")))

  private val setReference: Writer[Vector[String], TestSubject] => TestContainerObject => Writer[Vector[String], TestContainerObject] = (fieldLog) => (source) =>
    fieldLog.flatMap(fieldValue =>
      if (source.reference == fieldValue)
        source.writer(Vector.empty)
      else
        source.copy(reference = fieldValue).writer(Vector("reference")))

  val referenceLens: PropertyChangeLens[TestContainerObject, TestSubject] =
    PLens[TestContainerObject, Writer[Vector[String], TestContainerObject], TestSubject, Writer[Vector[String], TestSubject]](_.reference)(setReference)
  val nameLens: PropertyChangeLens[TestContainerObject, String] =
    PLens[TestContainerObject, Writer[Vector[String], TestContainerObject], String, Writer[Vector[String], String]](_.name)(setName)
}

private[model] object TestContainerObjectRepository extends LensRepository[TestContainerObject] {
  private val records = Map[String, LensReplayRecord[TestContainerObject, _]](
    "name" -> LensReplayRecord(GenLens[TestContainerObject](_.name), Option.empty),
    "reference" -> LensReplayRecord(GenLens[TestContainerObject](_.reference), Some(TestSubjectRepository))
  )

  override def resolve[B](propertyName: String): Either[String, LensReplayRecord[TestContainerObject, B]] = {
    val r = records.get(propertyName)
    Either.cond(r.isDefined, r.get.asInstanceOf[LensReplayRecord[TestContainerObject, B]], s"Unknown property $propertyName in class TestContainerObject")
  }
}

object TestPropertyChangeReplay extends PropertyChangeReplayImpl(TestContainerObjectRepository)
