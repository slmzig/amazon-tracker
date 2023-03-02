package tracker.services

import cats.effect.Sync
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import tracker.repositories.PriceChangeRepositoryAlgebra

import java.util.UUID

trait PriceTrackerAlgebra[F[_]] {
  def trackPrices(): F[Unit]
}

class PriceTrackerService[F[_]: Sync: Logger](priceChangeRepository: PriceChangeRepositoryAlgebra[F], parser: ParserAlgebra[F])
    extends PriceTrackerAlgebra[F] {

  def trackPrices(): F[Unit] =
    for {
      subscriptions <- priceChangeRepository.findAll()
      _ <- subscriptions.traverse { subscription =>
            getProductPrice(subscription.id, subscription.url).handleErrorWith { er =>
              Logger[F].warn(er.toString) >>
                ().pure[F]
            }
          }
    } yield ()

  private def getProductPrice(subscriptionId: UUID, url: String) =
    for {
      document <- parser.connect(url)
      _        <- parser.checkAvailability(document)
      price    <- parser.getPriceFromBuyBox(document)
      _        <- priceChangeRepository.addPriceHistory(subscriptionId, price) // TODO insert prices with batches for less call to db
    } yield ()

}

object PriceTrackerService {
  def apply[F[_]: Sync: Logger](
                                 priceChangeRepository: PriceChangeRepositoryAlgebra[F],
                                 parser: ParserAlgebra[F]
  ): PriceTrackerService[F] =
    new PriceTrackerService[F](priceChangeRepository, parser)
}
