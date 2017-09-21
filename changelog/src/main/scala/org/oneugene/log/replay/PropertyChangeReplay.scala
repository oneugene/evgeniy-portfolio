package org.oneugene.log.replay

import monocle.Lens
import org.oneugene.log.PropertyChange

trait PropertyChangeReplay[A] {
  /**
    * Replays a change on the object instance
    *
    * @param value  object value to replay changes for
    * @param change change description to replay
    * @tparam B type of property the change is focused to
    * @return modified object or an error string if replay failed
    */
  def replayChange[B](value: A, change: PropertyChange[B]): Either[String, A]
}

class PropertyChangeReplayImpl[A](rootRepo: LensRepository[A]) extends PropertyChangeReplay[A] {
  override def replayChange[B](value: A, change: PropertyChange[B]): Either[String, A] = {
    val mayBeLens = createLens[B](change.propertyPath)
    mayBeLens.flatMap(lens => {
      val currentValue = lens.get(value)
      val expectedValue = change.originalValue
      if (currentValue != expectedValue) {
        Left(s"Original value mismatch, expected $expectedValue, but got $currentValue")
      } else {
        Right(lens.set(change.newValue)(value))
      }
    })
  }

  private def createLens[B](path: Seq[String]): Either[String, Lens[A, B]] =
    path match {
      case Seq() => Right(Lens.id[A].asInstanceOf[Lens[A, B]])
      case init :+ last =>
        val foldResult = init.foldRight(rootRepo.resolve(last))(composeLens2)
        foldResult.map(_.lens.asInstanceOf[Lens[A, B]])
    }

  private def composeLens2[A1, B, C](propertyName: String, record: Either[String, LensReplayRecord[A1, B]]): Either[String, LensReplayRecord[A1, C]] = {
    for {
      current <- record
      subRepoEither = Either.cond(current.subRepo.isDefined, current.subRepo.get, s"Unknown property name: $propertyName")
      subRepo <- subRepoEither
      next <- subRepo.resolve[C](propertyName)
    } yield LensReplayRecord(current.lens ^|-> next.lens, next.subRepo)
  }
}
