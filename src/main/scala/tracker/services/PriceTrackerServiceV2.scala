package tracker.services

import cats.effect.Sync
import cats.syntax.all._
import org.jsoup.Jsoup
import org.typelevel.log4cats.Logger
import tracker.models.PriceNotFound
import tracker.repositories.PriceChangeRepository

import java.util.UUID
import scala.util.control.NoStackTrace

trait PriceTrackerService[F[_]] {
  def trackPrices(): F[Unit]
  def getProductPrice(url: String): F[Double]
}

class PriceTrackerServiceImpl[F[_]: Sync: Logger](priceChangeRepository: PriceChangeRepository[F])
    extends PriceTrackerService[F] {

  def trackPrices(): F[Unit] =
    for {
      subscriptions <- priceChangeRepository.findAll()
      _ <- subscriptions.traverse { subscription =>
            addPrice(subscription.id, subscription.url).handleErrorWith {
              case PriceNotFound =>
                Logger[F].warn("price not found") >>
                  ().pure[F]
              case er =>
                Logger[F].warn(er.getMessage) >>
                  ().pure[F]
            }
          }
    } yield ()

  private def addPrice(subscriptionId: UUID, url: String) =
    for {
      price <- getProductPrice(url)
      _     <- priceChangeRepository.addPriceHistory(subscriptionId, price)
    } yield ()

  def getProductPrice(url: String): F[Double] =
    Sync[F].blocking {
      val doc = Jsoup
        .connect(url)
        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
        .get()
      val priceText = doc.select(".priceToPay span span.a-price-whole").text()
      if (priceText.isEmpty) {
        PriceNotFound.raiseError[F, Double]
      } else {
        val price = priceText.replace("$", "").toDouble
        price.pure[F]
      }
    }.flatten
}
