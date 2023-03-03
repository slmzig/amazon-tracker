package tracker.repositories

import cats.effect.Async
import cats.implicits._
import doobie._
import doobie.implicits._
import tracker.models.{PriceHistory, Subscription}
import doobie.postgres.implicits._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.LocalDateTime
import java.util.UUID

trait PriceChangeRepositoryAlgebra[F[_]] {
  def findAll(): F[List[Subscription]]
  def findBySubscriptionId(subscriptionId: UUID): F[List[PriceHistory]]
  def addPriceHistory(id: UUID, price: BigDecimal): F[Unit]
}

class PriceChangeRepository[F[_]: Async](xa: Transactor[F]) extends PriceChangeRepositoryAlgebra[F] with DBOps {

  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]
  override def addPriceHistory(subscription_id: UUID, price: BigDecimal): F[Unit] = {
    val insertSql =
      sql"INSERT INTO price_history (subscription_id, price) VALUES ($subscription_id, $price)".update.run

    insertSql.transact(xa).void.handleAndLog
  }

  override def findBySubscriptionId(subscriptionId: UUID): F[List[PriceHistory]] = {
    val selectSql =
      sql"SELECT price, checked_at FROM price_history WHERE subscription_id = $subscriptionId AND checked_at >= NOW() - INTERVAL '3 months'"
        .query[(UUID, BigDecimal, LocalDateTime)]
        .to[List]
        .map(_.map { case (subscriptionId, price, checkedAt) => PriceHistory(subscriptionId, price, checkedAt) })

    selectSql.transact(xa).handleAndLog
  }

  override def findAll(): F[List[Subscription]] = {
    val selectSql =
      sql"SELECT id, url FROM subscriptions WHERE checked_at >= NOW() - INTERVAL '3 months'"
        .query[(UUID, String)]
        .to[List]
        .map(_.map { case (subscriptionId, url) => Subscription(subscriptionId, url) })

    selectSql.transact(xa).handleAndLog
  }
}
object PriceChangeRepository {
  def apply[F[_]: Async](xa: Transactor[F]) = new PriceChangeRepository[F](xa)
}
