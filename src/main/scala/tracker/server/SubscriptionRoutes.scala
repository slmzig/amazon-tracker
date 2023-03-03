package tracker.server

import cats.Monad
import cats.effect._
import cats.implicits._
import io.circe.Encoder._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import tracker.models.{PriceHistory, SubscriptionUrl}
import tracker.services.SubscriptionAlgebra

import java.util.UUID

object SubscriptionRoutes extends Routes {

  def make[F[_]: Monad: Concurrent](
      subscriptionService: SubscriptionAlgebra[F]
  ): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] with RequestDslBinCompat = Http4sDsl[F]
    import dsl._
    implicit val directorDecoder: EntityDecoder[F, PriceHistory] = jsonOf[F, PriceHistory]

    HttpRoutes.of[F] {
      case req @ POST -> Root / "subscriptions" =>
        val result: F[UUID] = for {
          subscription <- req.decodeJson[SubscriptionUrl]
          id           <- subscriptionService.subscribe(subscription.url)
        } yield id

        result.response

      case GET -> Root / "subscriptions" / UUIDVar(subscriptionId) =>
        subscriptionService.getPriceChanges(subscriptionId).response

      case DELETE -> Root / "subscriptions" / UUIDVar(subscriptionId) =>
        subscriptionService.unsubscribe(subscriptionId).response
    }
  }
}
