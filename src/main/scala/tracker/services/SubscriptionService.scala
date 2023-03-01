package tracker.services

import cats.Monad
import cats.effect.Concurrent
import cats.implicits._
import tracker.models.PriceHistory
import tracker.repositories.{PriceChangeRepository, SubscriptionRepository}

import java.util.UUID

trait SubscriptionService[F[_]] {
  def subscribe(url: String): F[UUID]
  def unsubscribe(subscriptionId: UUID): F[Unit]
  def getPriceChanges(subscriptionId: UUID): F[List[PriceHistory]]
}

class SubscriptionServiceImpl[F[_]: Monad: Concurrent](
    subscriptionRepository: SubscriptionRepository[F],
    priceChangeRepository: PriceChangeRepository[F],
)
    extends SubscriptionService[F] {

  override def subscribe(url: String): F[UUID] =
    for {
      //      subscription <- blocker.delay(Subscription(url))
      subscriptionId <- subscriptionRepository.create(UUID.randomUUID(), url: String)
    } yield subscriptionId

  override def unsubscribe(subscriptionId: UUID): F[Unit] =
    subscriptionRepository.delete(subscriptionId).void

  override def getPriceChanges(subscriptionId: UUID): F[List[PriceHistory]] =
    priceChangeRepository.findBySubscriptionId(subscriptionId)
}
