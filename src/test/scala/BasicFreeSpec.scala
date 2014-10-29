import org.scalatest._

import scalaz.Monad
import annotation.tailrec

import scalaz._
import Scalaz._

class BasicFreeSpec extends FlatSpec with Matchers with Instrumented {


  "Scalaz Free" should "left/right bind" in {
    import Free._

    def gen[I](i: I): Trampoline[I] = {
      Suspend( () => Trampoline.done(i) )
    }

    //(a flatMap (b flatMap (c flatMap (...))))
    def lftBind(n: Int) =
      (1 to n).foldLeft(gen(0)){ case (acc, i) => acc flatMap { a => gen(i) } }

    // (... flatMap (_ => c flatMap (_ => b flatMap (_ => a))))
    def rgtBind(n: Int) =
      (1 to n).foldLeft(gen(n)){ case (acc, i) => gen(n-i) flatMap { _ => acc } }

    val testN = Seq[Int](
      1000
      , 200000, 300000, 500000, 800000
      // , 1000000,  2000000,  3000000,  5000000
      // , 10000000, 12000000, 15000000, 18000000
      // , 20000000, 30000000, 40000000  //, 50000000
    )


    println("Scalaz Free - Left Bind")
    initFile("src/test/results/scalaz_free_left.txt", Seq("nb", "scalaz_free_left_bind"))
    testN foreach { n =>
      testTime2File(s"$n") { lftBind(n).run }
    }
    closeFile()

    println("Scalaz Free - Right Bind")
    initFile("src/test/results/scalaz_free_right.txt", Seq("nb", "scalaz_free_left_bind"))
    testN foreach { n =>
      testTime2File(s"$n") { rgtBind(n).run }
    }
    closeFile()

  }



  "Strict Fixed Free" should "left/right bind" in {
    import freez.view._
    import Free._
  	import tfingertree.strict._
    import FreeView._

    def gen[I](i: I): Trampoline[I] = {
      fromView(Impure[Function0, I]( () => Trampoline.done(i) ))
    }

    //(a flatMap (b flatMap (c flatMap (...))))
    def lftBind(n: Int) = {
      (1 to n).foldLeft(gen(0)){ case (acc, i) => acc flatMap { a => gen(i) } }
    }

    // (... flatMap (_ => c flatMap (_ => b flatMap (_ => a))))
    def rgtBind(n: Int) = {
      (1 to n).foldLeft(gen(n)){ case (acc, i) => gen(n-i) flatMap { _ => acc } }
    }

    val testN = Seq[Int](
      1000
      , 200000,   300000,   500000,   800000
      , 1000000,  2000000,  3000000,  5000000
      // , 10000000, 12000000, 15000000, 18000000
      // , 20000000, 30000000, 40000000  //, 50000000
    )


    // println("Strict Free - Left Bind")
    // testN foreach { n =>
    //   testTime2(s"$n") { lftBind(n).run } should equal (n)
    // }

    println("Strict Free - Right Bind")
    testN foreach { n =>
      testTime2(s"$n") { rgtBind(n).run } should equal (n)
    }


  }

/*
  "Lazy Fixed Free" should "left/right bind" in {
    import `lazy`._
    import TFree._
    import TFreeView._

    //val M = TFreeMonad[Function0]

    def gen[I](i: I): Trampoline[I] = {
      fromView(Impure[Function0, I]( () => Trampoline.done(i) ))
    }

    //(a flatMap (b flatMap (c flatMap (...))))
    def lftBind(n: Int) = {
      (1 to n).foldLeft(gen(0)){ case (acc, i) => acc flatMap { a => gen(i) } }
      // @tailrec def step(i: Int, free: Trampoline[Int]): Trampoline[Int] = {
      //   if(i <= n) step(i+1, free flatMap { a => gen(i) })
      //   else free
      // }

      // step(0, gen(0))
    }

    // def foldRight[A, B](l: Seq[A], f: (A, B) => B): B = {

    // }

    // (... flatMap (_ => c flatMap (_ => b flatMap (_ => a))))
    def rgtBind(n: Int) = {
      (1 to n).foldLeft(gen(n)){ case (acc, i) => gen(n-i) flatMap { _ => acc } }
      // @tailrec def step(i: Int, free: Trampoline[Int]): Trampoline[Int] = {
      //   if(i > 0) step(i-1, gen(i) flatMap { _ => free })
      //   else free
      // }

      // step(n, gen(n))
    }

    val testN = Seq[Int](
      1000
      , 200000,   300000,   500000,   800000
      , 1000000,  2000000,  3000000,  5000000
      // , 10000000, 12000000, 15000000, 18000000
      // , 20000000, 30000000, 40000000  //, 50000000
    )

    // println("Lazy Free - Left Bind")
    // testN foreach { n =>
    //   testTime2(s"$n") { lftBind(n).run } should equal (n)
    // }

    println("Lazy Free - Right Bind")
    testN foreach { n =>
      testTime2(s"$n") { rgtBind(n).run } should equal (n)
    }

  }
*/
/*
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

    val testN = Seq[Int](
      1000
      , 200000,   300000,   500000,   800000
      , 1000000,  2000000,  3000000,  5000000
      , 10000000, 12000000, 15000000
      // , 18000000
      // , 20000000, 30000000, 40000000  //, 50000000
    )

    testN foreach { n =>
      val l = List.fill(n)(0)
      testTime(s"Scalaz Free - Even  - $n") { even(l).run } should equal (true)
      testTime(s"Scalaz Free - Odd   - $n") { even(0 +: l).run } should equal (false)
    }
  }


  "Strict Fixed Free" should "even/odd bind" in {
    import strict._
    import TFree._
    import Trampoline._

    def even[A](ns: List[A]): Trampoline[Boolean] = ns match {
      case Nil => done(true)
      case x :: xs => suspend(odd(xs))
    }

    def odd[A](ns: List[A]): Trampoline[Boolean] = ns match {
      case Nil => done(false)
      case x :: xs => suspend(even(xs))
    }

    val testN = Seq[Int](
      1000
      , 200000,   300000,   500000,   800000
      , 1000000,  2000000,  3000000,  5000000
      , 10000000, 12000000, 15000000
      // , 18000000
      // , 20000000, 30000000, 40000000  //, 50000000
    )

    testN foreach { n =>
      val l = List.fill(n)(0)
      testTime(s"Strict Fixed Free - Even  - $n") { even(l).run } should equal (true)
      testTime(s"Strict Fixed Free - Odd   - $n") { even(0 +: l).run } should equal (false)
    }
  }

  "Lazy Fixed Free" should "even/odd bind" in {
    import `lazy`._
    import TFree._
    import Trampoline._

    def even[A](ns: List[A]): Trampoline[Boolean] = ns match {
      case Nil => done(true)
      case x :: xs => suspend(odd(xs))
    }

    def odd[A](ns: List[A]): Trampoline[Boolean] = ns match {
      case Nil => done(false)
      case x :: xs => suspend(even(xs))
    }

    val testN = Seq[Int](
      1000
      , 200000,   300000,   500000,   800000
      , 1000000,  2000000,  3000000,  5000000
      , 10000000, 12000000, 15000000
      // , 18000000
      // , 20000000, 30000000, 40000000  //, 50000000
    )

    testN foreach { n =>
      val l = List.fill(n)(0)
      testTime(s"Lazy Scalaz Free - Even  - $n") { even(l).run } should equal (true)
      testTime(s"Lazy Scalaz Free - Odd   - $n") { even(0 +: l).run } should equal (false)
    }
  }
*/
}
