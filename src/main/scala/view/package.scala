package freez

import _root_.scalaz.std.function._
import _root_.scalaz.Monad

package object view {
  /** Facility to manipulate trampoline not only as an alias */
  implicit class FreeTrampoline[A](val free: Free[Function0, A]) extends AnyVal {
    /** Runs a trampoline all the way to the end, tail-recursively. */
    def run(implicit V: FreeViewer[Free]): A = {
      free.go(_())
    }
  }

  object FreeTrampoline {

    @inline implicit def monad[A](implicit M: Monad[({ type l[T] = Free[Function0, T] })#l]) = new Monad[FreeTrampoline] {

      def point[A](a: => A): FreeTrampoline[A] = M.point(a)

      def bind[A, B](fa: FreeTrampoline[A])(f: A => FreeTrampoline[B]): FreeTrampoline[B] = M.bind(fa.free){ a => f(a).free }

    }

    implicit def toFree[A](t: FreeTrampoline[A]): Free[Function0, A] = t.free

    def done[A](a: A)(implicit V: FreeViewer[Free]): FreeTrampoline[A] =
      V.fromView(FreeView.Pure[Function0, A](a))

    def delay[A](a: => A)(implicit V: FreeViewer[Free]): FreeTrampoline[A] =
       suspend(done(a))

    def suspend[A](a: => FreeTrampoline[A])(implicit V: FreeViewer[Free]): FreeTrampoline[A] =
      V.fromView(FreeView.Impure[Function0, A](() => a.free))

  }
}