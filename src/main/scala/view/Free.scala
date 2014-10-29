package freez
package view

import annotation.tailrec
import _root_.scalaz.{Monad, Functor, Coyoneda, Unapply, ~>}
import _root_.scalaz.std.function._

abstract class Free[S[_], A] {
  import FreeView._

  def map[B](f: A => B)(implicit M: Monad[({ type l[A] = Free[S, A] })#l]): Free[S, B] = M.map(this)(f)

  def flatMap[B](f: A => Free[S, B])(implicit M: Monad[({ type l[A] = Free[S, A] })#l]): Free[S, B] = M.bind(this)(f)

  final def mapSuspension[T[_]](f: S ~> T)(implicit S: Functor[S], T: Functor[T], V: FreeViewer[Free]): Free[T, A] = {
    V.fromView(
      V.toView(this) match {
        case Pure(a) => Pure[T, A](a)
        case Impure(a) => Impure[T, A](f(S.map(a){ tf => tf mapSuspension f }))
      }
    )
  }

  final def foldMap[M[_]](f: S ~> M)(implicit S: Functor[S], M: Monad[M], V: FreeViewer[Free]): M[A] = {
    V.toView(this) match {
      case Pure(a)   => M.point(a)
      case Impure(a) => M.bind(f(a)){ _.foldMap(f) }
    }
  }


  /** Runs to completion, using a function that extracts the resumption from its suspension functor. */
  final def go(f: S[Free[S, A]] => Free[S, A])(implicit S: Functor[S], V: FreeViewer[Free]): A = {
    @tailrec def go2(t: Free[S, A]): A = {
      V.toView(t) match {
        case Impure(a) => go2(f(a))
        case Pure(a)   => a
      }
    }
    go2(this)
  }

}


trait FreeCompanion {

  implicit def FreeMonad[S[_]](
    implicit  V: FreeViewer[Free], 
              M: Monad[({ type l[A] = Free[S, A] })#l]
  ) = new Monad[({ type l[A] = Free[S, A] })#l] {

    def point[A](a: => A): Free[S, A] = V.fromView(FreeView.Pure(a))

    def bind[A, B](fa: Free[S, A])(f: A => Free[S, B]): Free[S, B] = bind(fa)(f)

  }

  type FreeC[S[_], A] = Free[({type f[x] = Coyoneda[S, x]})#f, A]

  type Trampoline[A] = Free[Function0, A]

  object Trampoline {

    def done[A](a: A)(implicit V: FreeViewer[Free]): Trampoline[A] =
      V.fromView(FreeView.Pure[Function0, A](a))

    def delay[A](a: => A)(implicit V: FreeViewer[Free]): Trampoline[A] =
       suspend(done(a))

    def suspend[A](a: => Trampoline[A])(implicit V: FreeViewer[Free]): Trampoline[A] =
      V.fromView(FreeView.Impure[Function0, A](() => a))

  }

  implicit class TrampolineOps[A](val tr: Trampoline[A]) {
    /** Runs a trampoline all the way to the end, tail-recursively. */
    def run(implicit V: FreeViewer[Free]): A = {
      tr.go(_())
    }
  }

  type Source[A, B] = Free[({type f[x] = (A, x)})#f, B]


  /** Suspends a value within a functor in a single step. */
  def liftF[S[_], A](value: => S[A])(implicit S: Functor[S], V: FreeViewer[Free]): Free[S, A] =
    V.fromView[S, A](FreeView.Impure[S, A](S.map(value){ v =>
      V.fromView[S, A](FreeView.Pure[S, A](v))
    }))

  /** A version of `liftF` that infers the nested type constructor. */
  def liftFU[MA](value: => MA)(implicit MA: Unapply[Functor, MA], V: FreeViewer[Free]): Free[MA.M, MA.A] =
    liftF(MA(value))(MA.TC, V)

  /** A free monad over a free functor of `S`. */
  def liftFC[S[_], A](s: S[A])(implicit V: FreeViewer[Free]): FreeC[S, A] =
    liftFU(Coyoneda lift s)

}