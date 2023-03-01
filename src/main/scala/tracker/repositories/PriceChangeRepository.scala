package tracker.repositories

import cats.{Applicative, Monad}
import cats.effect.Async
import cats.implicits._
import doobie._
import doobie.implicits._
import tracker.models.{PriceHistory, Subscription}
import doobie.postgres.implicits._

import java.time.LocalDateTime
import java.util.UUID

trait PriceChangeRepository[F[_]] {
  def findAll(): F[List[Subscription]]
  def findBySubscriptionId(subscriptionId: UUID): F[List[PriceHistory]]
  def addPriceHistory(id: UUID, price: BigDecimal): F[Unit]
}

class PriceChangeRepositoryImpl[F[_]: Async](xa: Transactor[F]) extends PriceChangeRepository[F] {

  override def addPriceHistory(subscription_id: UUID, price: BigDecimal): F[Unit] = {
    val insertSql =
      sql"INSERT INTO price_history (subscription_id, price) VALUES ($subscription_id, $price)".update.run

    insertSql.transact(xa).void
  }

  override def findBySubscriptionId(subscriptionId: UUID): F[List[PriceHistory]] = {
    val selectSql =
      sql"SELECT price, checked_at FROM price_history WHERE subscription_id = $subscriptionId AND checked_at >= NOW() - INTERVAL '3 months'"
        .query[(UUID, BigDecimal, LocalDateTime)]
        .to[List]
        .map(_.map { case (subscriptionId, price, checkedAt) => PriceHistory(subscriptionId, price, checkedAt) })

    selectSql.transact(xa)
  }

  override def findAll(): F[List[Subscription]] = {
    val selectSql =
      sql"SELECT id, url FROM subscriptions WHERE checked_at >= NOW() - INTERVAL '3 months'"
        .query[(UUID, String)]
        .to[List]
        .map(_.map { case (subscriptionId, url) => Subscription(subscriptionId, url) })

    selectSql.transact(xa)
  }
}
