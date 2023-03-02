package tracker.services

import cats.Monad
import tracker.models.PriceHistory
import tracker.repositories.{PriceChangeRepositoryAlgebra, SubscriptionRepositoryAlgebra}
import cats.effect._
import cats.implicits._
import java.util.UUID

trait SubscriptionAlgebra[F[_]] {
  def subscribe(url: String): F[UUID]
  def unsubscribe(subscriptionId: UUID): F[Unit]
  def getPriceChanges(subscriptionId: UUID): F[List[PriceHistory]]
}

class SubscriptionService[F[_]: Monad: Concurrent](
                                                    subscriptionRepository: SubscriptionRepositoryAlgebra[F],
                                                    priceChangeRepository: PriceChangeRepositoryAlgebra[F],
                                                    parser: ParserAlgebra[F]
) extends SubscriptionAlgebra[F] {

  override def subscribe(url: String): F[UUID] =
    for {
      document       <- parser.connect(url)
      _              <- parser.checkAvailability(document)
      price          <- parser.getPriceFromBuyBox(document)
      subscriptionId <- subscriptionRepository.create(UUID.randomUUID(), url: String)
      _              <- priceChangeRepository.addPriceHistory(subscriptionId, price)
    } yield subscriptionId

  override def unsubscribe(subscriptionId: UUID): F[Unit] =
    subscriptionRepository.delete(subscriptionId).void

  override def getPriceChanges(subscriptionId: UUID): F[List[PriceHistory]] =
    priceChangeRepository.findBySubscriptionId(subscriptionId)
}

object SubscriptionService {
  def apply[F[_] : Monad : Concurrent](
                                        subscriptionRepository: SubscriptionRepositoryAlgebra[F],
                                        priceChangeRepository: PriceChangeRepositoryAlgebra[F],
                                        parser: ParserAlgebra[F]
                 ): SubscriptionService[F] =
    new SubscriptionService[F](subscriptionRepository, priceChangeRepository, parser)
}
