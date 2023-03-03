package tracker.server

import cats.effect._
import cats.implicits._
import io.circe.Encoder
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import tracker.models.errors.{BuyBoxNotFound, CurrentlyUnavailable, PriceNotFoundInText}

trait Routes {
  implicit class FOps[F[_]: Concurrent, A](val value: F[A]) {
    def response(implicit encoder: Encoder[A]): F[Response[F]] = {
      val dsl: Http4sDsl[F] with RequestDslBinCompat = Http4sDsl[F]
      import dsl._
      value
        .flatMap { result: A =>
          Ok(result.asJson)
        }
        .handleErrorWith {
          case CurrentlyUnavailable =>
            BadRequest("Currently unavailable. We don't know when or if this item will be back in stock.")
          case BuyBoxNotFound =>
            BadRequest("Box with price is not available")
          case PriceNotFoundInText =>
            BadRequest("We are not able to parse prise")
          case ex: Throwable =>
            BadRequest("Something went wrong during request")
        }
    }
  }
}
