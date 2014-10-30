package freez.view

import annotation.tailrec
import _root_.scalaz.{Monad, Functor, Coyoneda, Unapply, ~>}

import freez._

abstract class DequeFreeComp extends FreeComp {
  self =>

  type Deque[R[_, _], A, B]

  type FC[F[_], A, B] = A => Free[F, B]
  type FMExp[F[_], A, B] = Deque[({ type l[X, Y] = FC[F, X, Y] })#l, A, B]

  // The optimized representation of Free
  case class FM[S[_], X, A](head: FreeView[S, X], tail: FMExp[S, X, A]) extends Free[S, A]


  implicit def FreeMonad[S[_]](
    implicit  TS: TSequence[Deque],
              V: FreeViewer[Free]
  ) = new Monad[({ type l[A] = Free[S, A] })#l] {

    def point[A](a: => A): Free[S, A] = V.fromView(FreeView.Pure(a))

    def bind[A, B](fa: Free[S, A])(f: A => Free[S, B]): Free[S, B] = self.bind(fa)(f)

  }

  implicit def viewer(implicit TS: TSequence[Deque]) = new FreeViewer[Free]{

    final def fromView[S[_], A](h: FreeView[S, A]): Free[S, A] =
      FM(h, TS.tempty[({ type l[X, Y] = FC[S, X, Y] })#l, A])

    @tailrec final def toView[S[_], A](free: Free[S, A])(
      implicit F: Functor[S]
    ): FreeView[S, A] = {
      import FreeView._

      type FCS[A, B] = ({ type l[X, Y] = FC[S, X, Y] })#l[A, B]

      free match {
        case f:FM[S, x, A] => f.head match {
          case Pure(x) =>
            TS.tviewl[FCS, x, A](f.tail) match {
              case _: TViewl.EmptyL[Deque, FCS, x] =>
                Pure(x)

              case l: TViewl.LeafL[Deque, FCS, u, v, A] =>
                toView(
                  l.head(x.asInstanceOf[u]) match {
                    case f2: FM[S, x, v] =>
                      FM(
                        f2.head,
                        TS.tappend[FCS, x, v, A](f2.tail, l.tail())
                      )
                  }
                )
            }
          case Impure(a) =>
            Impure(F.map(a){
              case f2: FM[S, y, x] =>
                FM(f2.head, TS.tappend[FCS, y, x, A](f2.tail, f.tail))
            })
        }
      }
    }
  }

  final def bind[S[_], A, B](fa: Free[S, A])(f: A => Free[S, B])(implicit TS: TSequence[Deque], V: FreeViewer[Free]): Free[S, B] = {
    type FCS[A, B] = ({ type l[X, Y] = FC[S, X, Y] })#l[A, B]
    fa match {
      case free: FM[S, x, A] =>
        FM(
          free.head,
          TS.tappend[FCS, x, A, B](
            free.tail,
            TS.tsingleton[FCS, A, B](f)
          )
        )
    }
  }

}










