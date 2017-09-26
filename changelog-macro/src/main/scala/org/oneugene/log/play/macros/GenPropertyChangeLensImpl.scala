package org.oneugene.log.play.macros

import org.oneugene.log.play.PropertyChangeLens

/**
  * An implementation of macro which generates [[PropertyChangeLens]].
  * Macro code is based on Monocle GenLens macro code.
  */
private object GenPropertyChangeLensImpl {
  type Context = scala.reflect.macros.blackbox.Context

  def genLensImpl[S: c.WeakTypeTag, A: c.WeakTypeTag](c: Context)(field: c.Expr[S => A]): c.Expr[PropertyChangeLens[S, A]] = {
    import c.universe._

    field match {
      case Expr(
      Function(
      List(ValDef(_, termDefName, _, EmptyTree)),
      Select(Ident(termUseName), fieldTermName))
      ) if termDefName.decodedName.toString == termUseName.decodedName.toString =>
        val fieldName = fieldTermName.decodedName.toString
        mkLensImpl[S, A](c)(fieldName)
      case _ => c.abort(c.enclosingPosition, s"Illegal field reference ${show(field.tree)}")
    }
  }

  def mkLensImpl[S: c.WeakTypeTag, A: c.WeakTypeTag](c: Context)(fieldName: String): c.Expr[PropertyChangeLens[S, A]] = {
    import c.universe._

    val sourceType = weakTypeOf[S]
    val focusType = weakTypeOf[A]

    val getterMethod = sourceType.decls.collectFirst {
      case m: MethodSymbol if m.isCaseAccessor && m.name.decodedName.toString == fieldName => m
    }.getOrElse(c.abort(c.enclosingPosition, s"Cannot find method $fieldName in $sourceType"))
    val constructor = sourceType.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.getOrElse(c.abort(c.enclosingPosition, s"Cannot find constructor in $sourceType"))

    val field = constructor.paramLists.head
      .find(_.name.decodedName.toString == fieldName)
      .getOrElse(c.abort(c.enclosingPosition, s"Cannot find constructor field named $fieldName in $sourceType"))

    c.Expr[PropertyChangeLens[S, A]](
      q"""
         import cats.data.Writer
         import monocle.PLens
         import cats.implicits.{catsKernelStdMonoidForVector, catsSyntaxWriterId}

         PLens[$sourceType, Writer[Vector[String], $sourceType], $focusType, Writer[Vector[String], $focusType]](_.$getterMethod)(
               (fieldLog) => (source) =>
                 fieldLog.flatMap(fieldValue => if (source.$getterMethod == fieldValue) source.writer(Vector.empty) else source.copy($field = fieldValue).writer(Vector($fieldName)))
             )
       """
    )
  }
}
