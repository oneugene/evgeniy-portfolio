package org.oneugene.log

import org.oneugene.log.UserContainer.User

import scalaz.Lens

trait PropertyChangeReplay[A] {
  def replayChange[B](value: A, change: PropertyChange[B]): A
}

class PropertyChangeReplayImpl[A](rootRepo: LensRepository[A]) extends PropertyChangeReplay[A]{

  override def replayChange[B](value: A, change: PropertyChange[B]): A = {
    val lens: Lens[A, B] = createLens(change.propertyPath)
    val currentValue = lens.get(value)
    val expectedValue = change.originalValue
    if(currentValue!=expectedValue){
      ???
    }else {
      lens.set(value, change.newValue)
    }
  }

  private def createLens[B](path: Seq[String]): Lens[A, B] =
    path.reverse match {
      case Seq() => Lens.lensId[A].asInstanceOf[Lens[A, B]]
      case head +: tail =>
        val foldResult: Lens[A, _] = tail.foldLeft(rootRepo.getLens(head) -> rootRepo.getSubRepository(head))(
          composeLens2
        )._1
        foldResult.asInstanceOf[Lens[A, B]]
      }

  private def composeLens2[A1, B, C](l: (Lens[A1, B], LensRepository[B]), propertyName: String): (Lens[A1, C], LensRepository[C]) = {
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

