package org.oneugene.wiki.fromxml

sealed trait ReceiveResult[+A] {
  def map[B](f: A => B): ReceiveResult[B]

  def flatMap[B](f: A => ReceiveResult[B]): ReceiveResult[B]

  def fold[U](failed: Throwable => U, complete: A => U, incomplete: => U): U
}

case object Incomplete extends ReceiveResult[Nothing] {
  override def fold[U](failed: (Throwable) => U, complete: (Nothing) => U, incomplete: => U): U = incomplete

  override def map[B](f: (Nothing) => B): ReceiveResult[B] = this

  override def flatMap[B](f: (Nothing) => ReceiveResult[B]): ReceiveResult[B] = this
}

case class Complete[+A](value: A) extends ReceiveResult[A] {
  override def fold[U](failed: (Throwable) => U, complete: (A) => U, incomplete: => U) = complete(value)

  override def map[B](f: (A) => B): ReceiveResult[B] = ReceiveResult.complete(f(value))

  override def flatMap[B](f: (A) => ReceiveResult[B]): ReceiveResult[B] = f(value)
}

case class Failed(ex: Throwable) extends ReceiveResult[Nothing] {
  override def fold[U](failed: (Throwable) => U, complete: (Nothing) => U, incomplete: => U) = failed(ex)

  override def map[B](f: (Nothing) => B): ReceiveResult[B] = this

  override def flatMap[B](f: (Nothing) => ReceiveResult[B]): ReceiveResult[B] = this
}

object ReceiveResult {
  def incomplete[A]: ReceiveResult[A] = Incomplete

  def failure[A](ex: Throwable): ReceiveResult[A] = Failed(ex)

  def complete[A](value: A): ReceiveResult[A] = Complete(value)
}
