package freez
package view
package tfingertree
package `lazy`

import annotation.tailrec

import core._


object Free extends freez.view.Free {
  import scalaz.{Monad, Functor, Coyoneda, Unapply}
  import TFingerTree.TFingerTreeSeq

  type Deque[R[_, _], A, B] = TFingerTree[R, A, B]
}










