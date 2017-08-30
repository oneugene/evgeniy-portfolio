package org.oneugene.log

case class PropertyChange[+B](propertyPath: Seq[String], newValue: B, originalValue: B)
