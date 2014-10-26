package freez
package view
package core

import scalaz.{Monad, Functor, Coyoneda, Unapply, ~>}

import freez._

sealed abstract class FreeView[S[_], A]

object FreeView {
  case class Pure[S[_], A](a: A) extends FreeView[S, A]
  case class Impure[S[_], A](a: S[Free[S, A]]) extends FreeView[S, A]
}


trait FreeViewer[Free[_[_], _]] {

  def fromView[S[_], A](h: FreeView[S, A]): Free[S, A]

  def toView[S[_] : Functor, A](free: Free[S, A]): FreeView[S, A]

}
