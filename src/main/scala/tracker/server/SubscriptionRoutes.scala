package tracker.server

import cats.Monad
import cats.effect._
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import tracker.models.{PriceHistory, PriceNotFound, SubscriptionUrl}
import tracker.services.{PriceTrackerServiceImpl, SubscriptionService}

object SubscriptionRoutes {

  def make[F[_]: Monad: Concurrent](
      subscriptionService: SubscriptionService[F],
      trackerServiceImpl: PriceTrackerServiceImpl[F]
  ): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    implicit val directorDecoder: EntityDecoder[F, PriceHistory] = jsonOf[F, PriceHistory]

    HttpRoutes.of[F] {
      case req @ POST -> Root / "subscriptions" =>
        val result = for {
          subscription <- req.decodeJson[SubscriptionUrl]
          _            <- trackerServiceImpl.getProductPrice(subscription.url)
          id           <- subscriptionService.subscribe(subscription.url)
        } yield id

        result.flatMap(r => Ok(r.asJson)).handleErrorWith {
          case PriceNotFound => BadRequest("this product is temporary unavailable")
          case ex: Throwable =>
            BadRequest(ex.getMessage)
        }

      case GET -> Root / "subscriptions" / UUIDVar(subscriptionId) =>
        subscriptionService.getPriceChanges(subscriptionId).flatMap { list =>
          Ok(list.asJson)
        }

      case DELETE -> Root / "subscriptions" / UUIDVar(subscriptionId) =>
        subscriptionService.unsubscribe(subscriptionId).flatMap(Ok(_))

    }
  }
}
