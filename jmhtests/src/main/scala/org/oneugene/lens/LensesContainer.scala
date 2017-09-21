package org.oneugene.lens

import org.oneugene.log.model.{BDate, BDateDay, User}

import scalaz.{Lens=>ZLens}
import monocle.{Lens =>MLens}
import monocle.macros.GenLens

object LensesContainer {
  val nameZLens: ZLens[User, String] = ZLens.lensu[User, String]((user, name) => user.copy(name = name), _.name)
  val birthDateZLens: ZLens[User, BDate] = ZLens.lensu[User, BDate]((user, bdate) => user.copy(birthDate = bdate), _.birthDate)
  val dayZLens: ZLens[BDate, BDateDay] = ZLens.lensu[BDate, BDateDay]((date, day) => date.copy(day = day), _.day)

  val birthDayZLens: ZLens[User, BDateDay] = dayZLens <=< birthDateZLens


  val nameMyLens: MyLens[User, User, String, String] = MyLens[User, User, String, String]((user, name) => user.copy(name = name), _.name)
  val birthDateMyLens: MyLens[User, User, BDate, BDate] = MyLens[User, User, BDate, BDate]((user, bdate) => user.copy(birthDate = bdate), _.birthDate)
  val dayMyLens: MyLens[BDate, BDate, BDateDay, BDateDay] = MyLens[BDate, BDate, BDateDay, BDateDay]((date, day) => date.copy(day = day), _.day)

  val birthDayMyLens: MyLens[User, User, BDateDay, BDateDay] = dayMyLens <=< birthDateMyLens

  val nameMLens: MLens[User, String] = GenLens[User](_.name)
  val birthDateMLens: MLens[User, BDate] = GenLens[User](_.birthDate)
  val dayMLens: MLens[BDate, BDateDay] = GenLens[BDate](_.day)

  val birthDayMLens: MLens[User, BDateDay] = birthDateMLens ^|-> dayMLens
}
