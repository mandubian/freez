import org.scalatest._

import scalaz.Monad
import annotation.tailrec

import scalaz._
import Scalaz._

class BasicFreeSpec extends FlatSpec with Matchers with Instrumented {

  object GenericTests {
    //(a flatMap (b flatMap (c flatMap (...))))
    def lftBind[S[_]](n: Int)(gen: Int => S[Int])(implicit M: Monad[S]) = {
      (1 to n).foldLeft(gen(0)){ case (acc, i) => acc flatMap { a => gen(i) } }
    }

    // (... flatMap (_ => c flatMap (_ => b flatMap (_ => a))))
    def rgtBind[S[_]](n: Int)(gen: Int => S[Int])(implicit M: Monad[S]) = {
      (1 to n).foldLeft(gen(n)){ case (acc, i) => gen(n-i) flatMap { _ => acc } }
    }
  }

  import GenericTests._

  val testN = Seq[Int](
    1000
    , 200000, 300000, 500000, 800000
    // , 1000000,  2000000,  3000000,  5000000
    // , 10000000, 12000000, 15000000, 18000000
    // , 20000000, 30000000, 40000000  //, 50000000
  )

  "Scalaz Free" should "left/right bind" in {
    import Free._

    def gen[I](i: I): Trampoline[I] = {
      Suspend( () => Trampoline.done(i) )
    }

    println("Scalaz Free - Left Bind")
    initFile("src/test/results/scalaz_free_left.txt", Seq("nb", "scalaz_free_left_bind"))
    testN foreach { n =>
      testTime2File(s"$n") { lftBind(n)(gen _).run }
    }
    closeFile()

    println("Scalaz Free - Right Bind")
    initFile("src/test/results/scalaz_free_right.txt", Seq("nb", "scalaz_free_left_bind"))
    testN foreach { n =>
      testTime2File(s"$n") { rgtBind(n)(gen _).run }
    }
    closeFile()

  }



  "TFree Strict Free" should "left/right bind" in {
    import freez.view._
    import FreeView._
    import tfingertree.strict.Free
    import Free._

    def gen[I](i: I): Trampoline[I] = {
      viewer.fromView(Impure[Function0, I]( () => Trampoline.done(i) ))
    }

    println("Strict Free - Left Bind")
    initFile("src/test/results/ftree_strict_free_left.txt", Seq("nb", "ftree_strict_free_left_bind"))
    testN foreach { n =>
      testTime2File(s"$n") { lftBind(n)(gen _).run }
    }
    closeFile()

    println("Strict Free - Right Bind")
    initFile("src/test/results/ftree_strict_free_right.txt", Seq("nb", "ftree_strict_free_right_bind"))
    testN foreach { n =>
      testTime2File(s"$n") { rgtBind(n)(gen _).run }
    }
    closeFile()


  }


  "TFree Lazy Free" should "left/right bind" in {
    import freez.view._
    import FreeView._
    import tfingertree.`lazy`.Free
    import Free._

    def gen[I](i: I): Trampoline[I] = {
      viewer.fromView(Impure[Function0, I]( () => Trampoline.done(i) ))
    }

    println("Lazy Free - Left Bind")
    initFile("src/test/results/ftree_lazy_free_left.txt", Seq("nb", "ftree_lazy_free_left_bind"))
    testN foreach { n =>
      testTime2File(s"$n") { lftBind(n)(gen _).run }
    }
    closeFile()

    println("Lazy Free - Right Bind")
    initFile("src/test/results/ftree_lazy_free_right.txt", Seq("nb", "ftree_lazy_free_right_bind"))
    testN foreach { n =>
      testTime2File(s"$n") { rgtBind(n)(gen _).run }
    }
    closeFile()

  }


  "Scalaz Free" should "even/odd bind" in {
    import Free._

    def even[A](ns: List[A]): Trampoline[Boolean] = ns match {
      case Nil => Trampoline.done(true)
      case x :: xs => Trampoline.suspend(odd(xs))
    }

    def odd[A](ns: List[A]): Trampoline[Boolean] = ns match {
      case Nil => Trampoline.done(false)
      case x :: xs => Trampoline.suspend(even(xs))
    }

    initFile("src/test/results/scalaz_free_even.txt", Seq("nb", "scalaz_free_even"))
    testN foreach { n =>
      val l = List.fill(n)(0)
      testTime2File(s"$n") { even(l).run }
    }
    closeFile()

    initFile("src/test/results/scalaz_free_odd.txt", Seq("nb", "scalaz_free_odd"))
    testN foreach { n =>
      val l = List.fill(n)(0)
      testTime2File(s"$n") { even(0 +: l).run }
    }
    closeFile()
  }


  "TFree Strict Free" should "even/odd bind" in {
    import freez.view._
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

    initFile("src/test/results/ftree_strict_free_even.txt", Seq("nb", "ftree_strict_free_even"))
    testN foreach { n =>
      val l = List.fill(n)(0)
      testTime2File(s"$n") { even(l).run }
    }
    closeFile()

    initFile("src/test/results/ftree_strict_free_odd.txt", Seq("nb", "ftree_strict_free_odd"))
    testN foreach { n =>
      val l = List.fill(n)(0)
      testTime2File(s"$n") { even(0 +: l).run }
    }
    closeFile()
  }

  "TFree Lazy Free" should "even/odd bind" in {
    import freez.view._
    import tfingertree.`lazy`.Free
    import Free._

    def even[A](ns: List[A]): Trampoline[Boolean] = ns match {
      case Nil => Trampoline.done(true)
      case x :: xs => Trampoline.suspend(odd(xs))
    }

    def odd[A](ns: List[A]): Trampoline[Boolean] = ns match {
      case Nil => Trampoline.done(false)
      case x :: xs => Trampoline.suspend(even(xs))
    }

    initFile("src/test/results/ftree_lazy_free_even.txt", Seq("nb", "ftree_lazy_free_even"))
    testN foreach { n =>
      val l = List.fill(n)(0)
      testTime2File(s"$n") { even(l).run }
    }
    closeFile()

    initFile("src/test/results/ftree_lazy_free_odd.txt", Seq("nb", "ftree_lazy_free_odd"))
    testN foreach { n =>
      val l = List.fill(n)(0)
      testTime2File(s"$n") { even(0 +: l).run }
    }
    closeFile()
  }

}
