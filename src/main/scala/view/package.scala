package freez

import _root_.scalaz.std.function._

package object view {
  /** Facility to manipulate trampoline not only as an alias */
  implicit class FreeTrampoline[A](val free: Free[Function0, A]) extends AnyVal {
    /** Runs a trampoline all the way to the end, tail-recursively. */
    def run(implicit V: FreeViewer[Free]): A = {
      free.go(_())
    }
  }

  object FreeTrampoline {

    implicit def monad[A](M: ({ type l[T] = Monad[Function0, T] })#l) = M

    implicit def toFree[A](t: Trampoline[A]): Free[Function0, A] = t.free

    def done[A](a: A)(implicit V: FreeViewer[Free]): Trampoline[A] =
      V.fromView(FreeView.Pure[Function0, A](a))

    def delay[A](a: => A)(implicit V: FreeViewer[Free]): Trampoline[A] =
       suspend(done(a))

    def suspend[A](a: => Trampoline[A])(implicit V: FreeViewer[Free]): Trampoline[A] =
      V.fromView(FreeView.Impure[Function0, A](() => a))

  }
}