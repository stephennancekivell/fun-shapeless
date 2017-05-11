package tricks

import org.scalatest.FreeSpec
import shapeless._

object Trick3 {
  trait Encoder[A] {
    def encode(a: A): String

    def contraMap[B](fn: (B) => A): Encoder[B] =
      createEncoder(b => encode(fn(b)))
  }

  def createEncoder[A](fn: (A) => String) = new Encoder[A] {
    def encode(a: A): String = fn(a)
  }

  implicit val intEncoder = createEncoder[Int](i => i.toString)
  implicit val longEncoder = createEncoder[Long](i => i.toString)
  implicit val stringEncoder = createEncoder[String](i => i)
  implicit val doubleEncoder = longEncoder.contraMap[Double](d => d.toLong)

  implicit def optionEncoder[A](implicit encoder: Encoder[A]) =
    createEncoder[Option[A]](opt => opt.map(encoder.encode).getOrElse(""))

  implicit def nillEncoder = new Encoder[HNil] {
    override def encode(a: HNil): String = ""
  }

  implicit def hListEncoder[H, T <: HList](
      implicit encoderA: Encoder[H],
      encoderB: Encoder[T]
  ) =
    createEncoder[H :: T](ab =>
      encoderA.encode(ab.head) + "," + encoderB.encode(ab.tail))

  implicit def genericEncoder[A, H <: HList](
      implicit gen: Generic.Aux[A, H],
      hListEncoder: Encoder[H]): Encoder[A] =
    new Encoder[A] {
      def encode(a: A): String =
        hListEncoder.encode(gen.to(a))
    }

  def encode[A](a: A)(implicit encoder: Encoder[A]): String = encoder.encode(a)

  case class DataDto(id: Long, name: String, opt: Option[String])

  val dto = DataDto(1, "", Some("wh"))

  encode(dto)

  val gen = Generic[DataDto]
  val hlist = gen.to(dto)
}

class Test3 extends FreeSpec {
  import Trick3._

  "trick3" in {
    println("e " + encode(1 :: 2l :: Option(1.1) :: HNil))
    println("hlist" + hlist)
    println("hlistclass " + classOf[gen.Repr])
    println("typeable " + Typeable[gen.Repr].describe)

    println("cc " + encode(dto))

  }
}
