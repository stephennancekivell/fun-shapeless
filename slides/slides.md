class: center, middle

# Lists, Types and Generic Code
pattern matching to pattern smashing

Tricks with list of types.

Working with lists and multiple types, Why List[Any] is bad and how to use ADT's Typeclasses and HList have more reliable code.

by Stephen Nancekivell

@StephenNancekiv

---

```scala
val xs: Seq[Any] = List(1, 2.1, "three")
```

---

```scala
xs map {
  case i: Int => ???
  case d: Double => ???
  case s: String => ???
}
```

But what happens if we add a new type to the list ?

---

Runtime Error

```scala
scala.MatchError: 4 (of class java.lang.Long)
```

---

ADT

algebraic data types

---

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
  case StringNumber(s) => ???
}
```

---

Compilier Error

```scala
cmd18.sc:1: match may not be exhaustive.
It would fail on the following inputs: DoubleNumber(_), StringNumber(_)
```

```scala
scalacOptions += "-Xfatal-warnings"
```

---

Typeclass

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

```scala
val x: Long = 4l
processNumber(x)
```

what happens
---

Complier Error

```scala
cmd6.sc:1: could not find implicit value for parameter handler: $sess.cmd4.NumberHandler[Long]
```

---

Typeclass

Ad-hoc polymorphism

---

Typeclass in the list

```scala
val xs: Seq[Any] = List(1, 2.1, "three")

xs map processNumber
```

---

Compilier Error

```scala
cmd8.sc:1: could not find implicit value for parameter handler: $sess.cmd4.NumberHandler[A]
```

Even though we have processNumber for Int Double and String scalac cant prove it.

---

Compile time vs Runtime

---

Tuples

```scala
val tuple: (Int, Double, String) = (1, 2.0, "three")
```

---

Imagine we're making some code ...

```scala
def processTuple2ii(t: (Int, Int))
def processTuple2si(t: (String, Int))
def processTuple2is(t: (Int, String))
```

---

Typeclasses to remove boiler plate

```scala
def processTuple[A,B](t: (A,B))(
implicit
handlerA: Handler[A],
handlerB: Handler[B])
```

---

This is great, we are so smart

---

```scala
def processTuple3[A,B,C](t: (A,B,C))
```

---

```scala
def processTuple3[A,B,C](t: (A,B,C))
def processTuple4[A,B,C,D](t: (A,B,C,D))
```

boiler plate is back

---

We need a thing ...

List[A], is fixed to one type

(A,B) tuple is fixed to its length

---

HList

---

HList[A,B < HList]


---

```scala
sealed trait HList
sealed trait HNil extends HList
case object HNil extends HNil
case class ::[+H, +T <: HList](head: H, tail: T) extends HList
```

---

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

Also map and flatMap, but not today..
---

```scala
xs.take(5)
```

what happens

---

Compilier error

```scala
xs.take(5) 
cmd26.sc:1: Implicit not found: shapeless.Ops.Take[Int :: Double :: String :: shapeless.HNil, shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless._0]]]]]]. You requested to take shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless.Succ[shapeless._0]]]]] elements, but the HList Int :: Double :: String :: shapeless.HNil is too short.
val res26 = xs.take(5)
                   ^
Compilation Failed
```

---

Compilier Magic

---

```scala
val a = 1 :: 2.0 :: HNil 
a: Int :: Double :: HNil = 1 :: 2.0 :: HNil
val b = 2 :: "three" :: HNil 
b: Int :: String :: HNil = 2 :: three :: HNil
a ++ b 
res21: Int :: Double :: Int :: String :: HNil = 1 :: 2.0 :: 2 :: three :: HNil
```

---

Have our cake and eat it too.

---

```scala
def giveMe = {
  if (Random.nextBoolean) 1 else "two"
}
@ val xy = giveMe :: giveMe :: HNil
xy: Any :: Any :: HNil = two :: 1 :: HNil
```

---

Cant build HList at runtime :'(

---

Now, more tricks

---

Type level recursion

---

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
```

---

Thats great but I dont use HList anywhere in my code..

---

Generic[T]

```scala
import shapeless.Generic

case class Foo(a: Int, b: Double, c: String)

val genFoo = Generic[Foo]
genFoo.to(Foo(1, 2.0, "three"))
res24: genFoo.Repr = 1 :: 2.0 :: three :: HNil
```

---

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

Thank you.