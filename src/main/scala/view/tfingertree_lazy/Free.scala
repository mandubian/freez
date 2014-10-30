package freez
package view
package tfingertree
package `lazy`

import annotation.tailrec


object Free extends freez.view.DequeFreeComp {
  type Deque[R[_, _], A, B] = TFingerTree[R, A, B]
}










