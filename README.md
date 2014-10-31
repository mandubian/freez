## Freez (Draft / Experimental)

> This project is a sandbox to test other representations of Free Monads: serious representations but also funny, optimized, specific representations and whatever goes through your mind.


### Observable Free

The 1st representation provided in the project is a direct conversion from Haskell code based on the paper ["Reflection without Remorse" by O.Kiselyov & A. Van Der Ploeg](http://homepages.cwi.nl/~ploeg/papers/zseq.pdf). It implements a FreeMonad as a viewable FreeMonad based on a typed-aligned deque to solve the classic quadratic complexity & the observability issues. The Haskell code can be found [there](https://github.com/atzeus/reflectionwithoutremorse).

I've tried to make the code as generic as possible to be able to test this approach using different deques. For now, I've implemented 3 versions:

- Scalaz FingerTree IndSeq
- Typed-aligned strict FingerTree (draft implementation)
- Typed-aligned lazier FingerTree (draft implementation)

#### Creating a new observable Free based on a deque?

1. create a Free companion object extending `freez.view.DequeFreeComp`

2. give a type `type Deque[R[_, _], A, B]` (`R[A, B]` represents the monadic binding function `A => Free[S, B]`)

3. implement an implicit `TSequence[Deque]`


Here is the example of the version based on Scalaz FingerTree:

```scala
// a singleton implementing DequeFreeComp
object Free extends freez.view.DequeFreeComp {
  import _root_.scalaz.{Monad, Functor, IndSeq}

  // the deque type (it can be typed-aligned but Any can be far enough ;))
  type Deque[R[_, _], A, B] = IndSeq[Any]

  // the implement TSequence
  implicit object TS extends TSequence[Deque] {
    def tempty[C[_, _], X]: Deque[C, X, X] = IndSeq[Any]()

    def tsingleton[C[_, _], X, Y](c: => C[X, Y]): Deque[C, X, Y] = IndSeq[Any](c)

    def tappend[C[_, _], X, Y, Z](a: Deque[C, X, Y], b: => Deque[C, Y, Z]): Deque[C, X, Z] = {
      a ++ b
    }

    def tviewl[C[_, _], X, Y](s: => Deque[C, X, Y]): TViewl[Deque, C, X, Y] = {
      val v = s.self.viewl
      v.headOption match {
        case None    => TViewl.EmptyL[Deque, C, X]().asInstanceOf[TViewl[Deque, C, X, Y]]
        case Some(h) => v.tailOption match {
          case None     => TViewl.LeafL[Deque, C, X, Y, Y](h.asInstanceOf[C[X, Y]], () => IndSeq[Any]())
          case Some(t)  => TViewl.LeafL[Deque, C, X, Any, Y](h.asInstanceOf[C[X, Any]], () => new IndSeq(t))
        }
      }

    }
  }
}
```

For typed-aligned FingerTrees, it's even easier as the `TSequence` is provided:

```scala
object Free extends freez.view.DequeFreeComp {
  type Deque[R[_, _], A, B] = TFingerTree[R, A, B]
}
```

#### Use it

For example, use the strict fingertree Free

```scala
import freez.view._
import FreeView._
import tfingertree.strict.Free
import Free._

def even[A](ns: List[A]): Trampoline[Boolean] = ns match {
  case Nil => Trampoline.done(true)
  case x :: xs => Trampoline.suspend(odd(xs))
}

def odd[A](ns: List[A]): Trampoline[Boolean] = ns match {
  case Nil => Trampoline.done(false)
  case x :: xs => Trampoline.suspend(even(xs))
}
```

### Performance Charts

#### Recursive Left Binds

```scala
(a flatMap (b flatMap (c flatMap (...))))
```

[Chart](https://plot.ly/~mandubian/33)

<br/>
<br/>
### Other ideas? Don't hesitate to contribute

