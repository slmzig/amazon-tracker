package tracker.services

import cats.effect.Sync
import org.jsoup.Jsoup
import tracker.models.errors.ConnectionToProductFailed
import tracker.models.errors.BuyBoxNotFound
import cats.syntax.all._
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.typelevel.log4cats.Logger
import tracker.models.errors.{CurrentlyUnavailable, PriceNotFoundInText}

trait ParserAlgebra[F[_]] {
  def connect(url: String): F[Document]
  def checkAvailability(doc: Document): F[Boolean]
  def getPriceFromBuyBox(doc: Document): F[Double]
}

class Parser[F[_]: Sync: Logger] extends ParserAlgebra[F] {

  private val unAvailableText = "Currently unavailable. We don't know when or if this item will be back in stock."

  def connect(url: String): F[Document] =
    Sync[F]
      .delay(Jsoup.connect(url).userAgent("Mozilla/49.0").get())
      .handleErrorWith { err =>
        Logger[F].warn(err.toString) >>
          ConnectionToProductFailed.raiseError[F, Document]
      }

  def checkAvailability(doc: Document): F[Boolean] =
    Sync[F].defer {
      val isPresent: Elements = doc.select("#availability")
      if (isPresent.size() > 0 && isPresent.hasText && isPresent.text() == unAvailableText) {
        CurrentlyUnavailable.raiseError[F, Boolean]
      } else {
        false.pure[F]
      }
    }

  def getPriceFromBuyBox(doc: Document): F[Double] =
    Sync[F].defer {
      val isPresent: Elements = doc.select("#price_inside_buybox")
      if (isPresent.size() > 0 && isPresent.hasText) {
        parsePrice(isPresent.text())
      } else {
        BuyBoxNotFound.raiseError[F, Double]
      }
    }

  def parsePrice(priceStr: String): F[Double] =
    Sync[F]
      .delay(priceStr.replace("$", "").toDouble)
      .handleErrorWith { exception =>
        Logger[F].warn(exception.toString) >>
          PriceNotFoundInText.raiseError[F, Double]
      }
}

object Parser {
  def apply[F[_]: Sync: Logger] = new Parser[F]
}
