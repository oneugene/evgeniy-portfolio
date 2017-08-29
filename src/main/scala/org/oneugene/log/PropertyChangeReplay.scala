package org.oneugene.log

import scalaz.{Lens, NonEmptyList, Validation}
import scalaz.Scalaz._

trait PropertyChangeReplay[A] {
  def replayChange[B](value: A, change: PropertyChange[B]): Validation[NonEmptyList[String], A]
}

class PropertyChangeReplayImpl[A](rootRepo: LensRepository[A]) extends PropertyChangeReplay[A] {

  override def replayChange[B](value: A, change: PropertyChange[B]): Validation[NonEmptyList[String], A] = {
    val lens: Lens[A, B] = createLens(change.propertyPath)
    val currentValue = lens.get(value)
    val expectedValue = change.originalValue
    if (currentValue != expectedValue) {
      s"Original value mismatch, expected $expectedValue, but got $currentValue".failureNel
    } else {
      lens.set(value, change.newValue).successNel
    }
  }

  private def createLens[B](path: Seq[String]): Lens[A, B] =
    path match {
      case Seq() => Lens.lensId[A].asInstanceOf[Lens[A, B]]
      case init :+ last =>
        val foldResult: Lens[A, _] = init.foldRight(rootRepo.getLens(last) -> rootRepo.getSubRepository(last))(composeLens2)._1
        foldResult.asInstanceOf[Lens[A, B]]
    }

  private def composeLens2[A1, B, C](propertyName: String, l: (Lens[A1, B], LensRepository[B])): (Lens[A1, C], LensRepository[C]) = {
    l match {
      case (lens, repo) =>
        composeLens(lens, repo, propertyName)
    }

  }

  private def composeLens[A1, B, C](lens: Lens[A1, B], repo: LensRepository[B], propertyName: String): (Lens[A1, C], LensRepository[C]) = {
    val subLens = repo.getLens[C](propertyName)
    val subRepo = repo.getSubRepository[C](propertyName)
    (lens >=> subLens, subRepo)
  }
}

object UserPropertyChangeReplay extends PropertyChangeReplayImpl(UserLensRepository)

