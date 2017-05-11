package tricks

object Trick4 {
  sealed trait HList
  sealed trait HNil extends HList
  case object HNil extends HNil
  case class ::[+H, +T <: HList](head: H, tail: T) extends HList

  val t: ::[String, ::[Int, HNil]] = ::("", ::(1, HNil))

  def doit(a: ::[String, ::[Int, HNil]]): String = ???

}

object Trick4Seq {

  // from FP in scala

  sealed trait List[+A] // `List` data type, parameterized on a type, `A`
  case object Nil extends List[Nothing] // A `List` data constructor representing the empty list
  /* Another data constructor, representing nonempty lists. Note that `tail` is another `List[A]`,
  which may be `Nil` or another `Cons`.
   */
  case class Cons[+A](head: A, tail: List[A]) extends List[A]

  val xs: List[Int] = Cons(1, Cons(2, Cons(3, Nil)))
}
