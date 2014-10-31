import org.scalatest._

import scalaz.Monad
import annotation.tailrec

import scalaz._
import Scalaz._

class BasicFreeSpec extends FlatSpec with Matchers with Instrumented {
  import freez.view._

  val testN = Seq[Int](
    1000, 1000,
    1000
    , 200000, 300000, 500000, 800000
    , 1000000,  2000000, 3000000, 5000000
    , 10000000, 12000000, 15000000 //, 18000000
    // , 20000000, 30000000, 40000000, 50000000
  )

  trait GenericBinds {

    //(a flatMap (b flatMap (c flatMap (...))))
    def lftBind[S[_]](n: Int)(gen: Int => S[Int])(implicit M: Monad[S]) = {
      (1 to n).foldLeft(gen(0)){ case (acc, i) => acc flatMap { a => gen(i) } }
    }

    // (... flatMap (_ => c flatMap (_ => b flatMap (_ => a))))
    def rgtBind[S[_]](n: Int)(gen: Int => S[Int])(implicit M: Monad[S]) = {
      (1 to n).foldLeft(gen(n)){ case (acc, i) => gen(n-i) flatMap { _ => acc } }
    }

  }

  object GenericBinds extends GenericBinds


  class GenericTests(implicit V: FreeViewer[freez.view.Free], M: Monad[FreeTrampoline]) extends GenericBinds {

    def gen[I](i: I): FreeTrampoline[I] = {
      V.fromView(FreeView.Impure[Function0, I]( () => FreeTrampoline.done(i) ))
    }


    def even[A](ns: List[A]): FreeTrampoline[Boolean] = ns match {
      case Nil => FreeTrampoline.done(true)
      case x :: xs => FreeTrampoline.suspend(odd(xs))
    }

    def odd[A](ns: List[A]): FreeTrampoline[Boolean] = ns match {
      case Nil => FreeTrampoline.done(false)
      case x :: xs => FreeTrampoline.suspend(even(xs))
    }


    def work(name: String) = {
      println(s"$name - Left Bind")
      initFile(s"src/test/results/${System.currentTimeMillis()}_${name}_left.txt", Seq("nb", s"${name}_left"))
      testN foreach { n =>
        testTime2File(s"$n") { lftBind(n)(gen _).run }
      }
      closeFile()

      /*println(s"$name - Right Bind")
      initFile(s"src/test/results/${name}_right.txt", Seq("nb", s"${name}_right"))
      testN foreach { n =>
        testTime2File(s"$n") { rgtBind(n)(gen _).run }
      }
      closeFile()

      println(s"$name - Even")
      initFile(s"src/test/results/${name}_even.txt", Seq("nb", s"${name}_even"))
      testN foreach { n =>
        val l = List.fill(n)(0)
        testTime2File(s"$n") { even(l).run }
      }
      closeFile()

      println(s"$name - Odd")
      initFile(s"src/test/results/${name}_odd.txt", Seq("nb", s"${name}_odd"))
      testN foreach { n =>
        val l = List.fill(n)(0)
        testTime2File(s"$n") { even(0 +: l).run }
      }
      closeFile()*/
    }
  }


  "Scalaz Free" should "left/right/odd/even" in {
    import Free._
    import GenericBinds._

    def gen[I](i: I): Trampoline[I] = {
      Suspend( () => Trampoline.done(i) )
    }

    def even[A](ns: List[A]): Trampoline[Boolean] = ns match {
      case Nil => Trampoline.done(true)
      case x :: xs => Trampoline.suspend(odd(xs))
    }

    def odd[A](ns: List[A]): Trampoline[Boolean] = ns match {
      case Nil => Trampoline.done(false)
      case x :: xs => Trampoline.suspend(even(xs))
    }

    println("Scalaz Free - Left Bind")
    initFile(s"src/test/results/${System.currentTimeMillis()}_scalaz_free_left.txt", Seq("nb", "scalaz_free_left"))
    testN foreach { n =>
      testTime2File(s"$n") { lftBind(n)(gen _).run }
    }
    closeFile()

    /*println("Scalaz Free - Right Bind")
    initFile("src/test/results/scalaz_free_right.txt", Seq("nb", "scalaz_free_left"))
    testN foreach { n =>
      testTime2File(s"$n") { rgtBind(n)(gen _).run }
    }
    closeFile()

    println("Scalaz Free - Even")
    initFile("src/test/results/scalaz_free_even.txt", Seq("nb", "scalaz_free_even"))
    testN foreach { n =>
      val l = List.fill(n)(0)
      testTime2File(s"$n") { even(l).run }
    }
    closeFile()

    println("Scalaz Free - Odd")
    initFile("src/test/results/scalaz_free_odd.txt", Seq("nb", "scalaz_free_odd"))
    testN foreach { n =>
      val l = List.fill(n)(0)
      testTime2File(s"$n") { even(0 +: l).run }
    }
    closeFile()*/
  }

/*
  "TFree Strict Free" should "left/right/odd/even" in {
    import freez.view._
    import FreeView._
    import tfingertree.strict.Free
    import Free._

    val tests = new GenericTests()
    import tests._


    work("ftree_strict_free")
  }


  "TFree Lazy Free" should "left/right/odd/even" in {
    import freez.view._
    import FreeView._
    import tfingertree.`lazy`.Free
    import Free._

    val tests = new GenericTests()
    import tests._

    work("ftree_lazy_free")
  }


  "Scalaz FingerTree Free" should "left/right/odd/even" in {
    import freez.view._
    import FreeView._
    import tfingertree.`lazy`.Free
    import Free._

    val tests = new GenericTests()
    import tests._

    work("ftree_scalaz_free")
  }
*/
}
