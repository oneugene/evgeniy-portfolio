package org.oneugene.log

/**
  * Describes change to the property of an object
  * @param propertyPath path to the property
  * @param newValue new property value
  * @param originalValue original property value
  * @tparam B type of the property
  */
case class PropertyChange[+B](propertyPath: Seq[String], newValue: B, originalValue: B)
