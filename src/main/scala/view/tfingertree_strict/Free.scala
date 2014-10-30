package freez.view.tfingertree
package strict

import annotation.tailrec


object Free extends freez.view.DequeFreeComp {
  type Deque[R[_, _], A, B] = TFingerTree[R, A, B]
}










