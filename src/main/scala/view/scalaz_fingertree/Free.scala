package freez
package view
package tfingertree
package `lazy`

import annotation.tailrec

import core._


object Free extends freez.view.Free {
  import scalaz.{Monad, Functor, IndSeq}
  import TFingerTree.TFingerTreeSeq

  type Deque[R[_, _], A, B] = IndSeq[Any]

  implicit object TS extends TSequence[Deque] {
    def tempty[C[_, _], X]: Deque[C, X, X] = IndSeq[Any]()

    def tsingleton[C[_, _], X, Y](c: => C[X, Y]): Deque[C, X, Y] = IndSeq[Any](c)

    def tappend[C[_, _], X, Y, Z](a: Deque[C, X, Y], b: => Deque[C, Y, Z]): Deque[C, X, Z] = {
      a ++ b
    }

    def tviewl[C[_, _], X, Y](s: Deque[C, X, Y]): TViewl[Deque, C, X, Y] = {
      val v = s.self.viewl
      v.headOption match {
        case None    => TViewl.EmptyL[Deque, C, X]().asInstanceOf[TViewl[Deque, C, X, Y]]
        case Some(h) => v.tailOption match {
          case None     => TViewl.LeafL[Deque, C, X, Y, Y](h.asInstanceOf[C[X, Y]], IndSeq[Any]())
          case Some(t)  => TViewl.LeafL[Deque, C, X, Any, Y](h.asInstanceOf[C[X, Any]], new IndSeq(t))
        }
      }

    }
  }
}










