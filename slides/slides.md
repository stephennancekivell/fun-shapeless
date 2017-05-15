class: center, middle

# Lists, Types and Generic Code

Working with lists and multiple types. Why List[Any] is bad and how ADT's, Type classes and HList can help is write awesome reliable code.

by Stephen Nancekivell

@StephenNancekiv

---

# List

```scala
val xs: Seq[Any] = List(1, 2.1, "three")
```

---

# List

```scala
xs map {
  case i: Int => ???
  case d: Double => ???
  case s: String => ???
}
```

---

# List

```scala
xs map {
  case i: Int => ???
  case d: Double => ???
  case s: String => ???
}
```

But what happens if we add a new type to the list ?

.center[.w40[![what happens](./whathappensaddnewtype.jpg)]]

---

# List

Runtime Error

```scala
scala.MatchError: 4 (of class java.lang.Long)
```

---

# ADT

algebraic data types

---

# ADT

```scala
sealed trait Number
case class IntNumber(i: Int) extends Number
case class DoubleNumber(d: Double) extends Number
case class StringNumber(s: String) extends Number

val ys: Seq[Number] =
  Seq(IntNumber(1), DoubleNumber(2.1), StringNumber("three"))
```

---

# ADT

```scala
sealed trait Number
case class IntNumber(i: Int) extends Number
case class DoubleNumber(d: Double) extends Number
case class StringNumber(s: String) extends Number

val ys: Seq[Number] =
  Seq(IntNumber(1), DoubleNumber(2.1), StringNumber("three"))

ys map {
  case IntNumber(i) => ???
  case DoubleNumber(d) => ???
}
```

what happens ?

.center[.w25[![what happens](./whathappensmissingcase.jpg)]]

---

# ADT

Compilier Error

```scala
cmd18.sc:1: match may not be exhaustive.
It would fail on the following inputs: StringNumber(_)
```

```scala
scalacOptions += "-Xfatal-warnings"
```

---

# Type Class

```scala
trait NumberHandler[A] {
  def handle(a: A): String
}

implicit val intNumberHandler = new NumberHandler[Int] {
  def handle(a: Int): String = ???
}

implicit val doubleNumberHandler = ???

implicit val stringNumberHandler = ???

def processNumber[A](a: A)(implicit handler: NumberHandler[A]) =
  handler.handle(a)

processNumber(1)
processNumber("three")
```

---

# Type Class

```scala
val x: Long = 4l
processNumber(x)
```

what happens ?

.center[.w50[![what happens](./whathappens.jpg)]]
---

# Type Class

```scala
val x: Long = 4l
processNumber(x)
```

what happens ?

Complier Error

```scala
cmd6.sc:1: could not find implicit value for
  parameter handler: $sess.cmd4.NumberHandler[Long]
```

---

# Type Class in List

```scala
val xs: Seq[Any] = List(1, 2.1, "three")

xs map processNumber
```

what happens ?

.center[.w50[![what happens](./whathappens.jpg)]]

---

# Type Class in List

```scala
val xs: Seq[Any] = List(1, 2.1, "three")

xs map processNumber
```

what happens ?

Compilier Error

```scala
cmd8.sc:1: could not find implicit value for
  parameter handler: $sess.cmd4.NumberHandler[A]
```

Even if we have instance.

---

# Compile Time vs Runtime

---

# Can we do better..

`List[A]`, is fixed to one type

(A,B) tuple is fixed to its length

---

# HList

---

# HList

```scala
HList[A, B <: HList]
```


---

# HList

```scala
sealed trait HList
sealed trait HNil extends HList
case object HNil extends HNil
case class ::[+H, +T <: HList](head: H, tail: T) extends HList
```

---

# HList

```scala
sealed trait HList
sealed trait HNil extends HList
case object HNil extends HNil
case class ::[+H, +T <: HList](head: H, tail: T) extends HList

@ val xs = 1 :: 2.0 :: "three" :: HNil 
xs: Int :: Double :: String :: HNil = 1 :: 2.0 :: three :: HNil
@ xs.head 
res2: Int = 1
@ xs.last 
res3: String = "three"
@ xs.drop(2) 
res5: String :: HNil = three :: HNil
```

Also map and flatMap..
---

# HList

```scala
val xs = 1 :: 2.0 :: "three" :: HNil

xs.take(5)
```

what happens ?

.center[.w40[![what happens](./whathappens.jpg)]]

---

# HList

```scala
val xs = 1 :: 2.0 :: "three" :: HNil

xs.take(5)
```

what happens ?

Compilier error

```scala
cmd26.sc:1: Implicit not found:
 shapeless.Ops.Take[
   Int :: Double :: String :: shapeless.HNil,
   shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless._0]]]]
   ]].
   You requested to take
   shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless._0]]]]]
   elements, but the HList Int :: Double :: String :: shapeless.HNil is too short.
val res26 = xs.take(5)
```

---

# Compilier So Smart

---

# HList

concat

```scala
val a = 1 :: 2.0 :: HNil 
a: Int :: Double :: HNil = 1 :: 2.0 :: HNil
val b = 2 :: "three" :: HNil 
b: Int :: String :: HNil = 2 :: three :: HNil
a ++ b 
res21: Int :: Double :: Int :: String :: HNil =
    1 :: 2.0 :: 2 :: three :: HNil
```

---

# HList

```scala
def giveMe = {
  if (Random.nextBoolean) 1 else "two"
}
@ val xy = giveMe :: giveMe :: HNil
```

.center[.w40[![what happens](./whathappens.jpg)]]

---

# HList

```scala
def giveMe = {
  if (Random.nextBoolean) 1 else "two"
}
@ val xy = giveMe :: giveMe :: HNil
xy: Any :: Any :: HNil = two :: 1 :: HNil
```

Cant build HList at runtime :'(

---

# Encoder Example

Technique used in json parsers etc

* argonaut
* spray json
* circe
* spark database connectors

---

# Encoder Typeclass

```scala
trait Encoder[A] {
  def encode(a: A): String
}

implicit val intEncoder = new Encoder[Int] {
  override def encode(i: Int): String = i.toString
}

implicit val doubleEncoder = ???
implicit val stringEncoder = ???

```
---

# HList

```scala
implicit val nillEncoder = new Encoder[HNil] {
  override def encode(a: HNil): String = ""
}
implicit def hListEncoder[H, T <: HList](
  implicit
  encoderH: Encoder[H],
  encoderT: Encoder[T]
) = new Encoder[H :: T] {
  def encode(xs: ::[H, T]): String =
  encoderH.encode(xs.head) + "," + encoderT.encode(xs.tail)
}

hListEncoder[Int :: Double :: String :: HNil]
hListEncoder[Double :: Int :: String :: HNil]
```

can call for any permutation!

---

# HList is great

But I dont use it anywhere..

---

# Generic

`Generic[T]`

Generic makes HList's from case classes

```scala
import shapeless.Generic

case class Foo(a: Int, b: Double, c: String)

val genFoo = Generic[Foo]
genFoo.to(Foo(1, 2.0, "three"))
res24: genFoo.Repr = 1 :: 2.0 :: three :: HNil
```

---

# Generic

```scala
implicit def genericEncoder[A, H <: HList](
  implicit
  gen: Generic.Aux[A, H],
  hListEncoder: Encoder[H]
  ): Encoder[A] = new Encoder[A] {
    def encode(a: A): String =
      hListEncoder.encode(gen.to(a))
}

def encode[A](a: A)(implicit encoder: Encoder[A]): String =
  encoder.encode(a)
  
encode(Foo(1, 2.0, "three"))
"1,2.0,three"
```

---

# Thank you.

further reading

* Shapeless, Miles Sabin, 2011
* The Type Astronaut's Guide to Shapeless, Dave Gurnell, book 2016
* Roll your Own Shapeless, Daniel Spiewak, video presentation, scala days 2016
* Scrap your boiler plate, paper 2003
