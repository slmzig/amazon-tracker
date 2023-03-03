package tracker.repositories

import cats.effect.Async
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID

trait SubscriptionRepositoryAlgebra[F[_]] {
  def create(id: UUID, url: String): F[UUID]
  def delete(id: UUID): F[Unit]
}

class SubscriptionRepository[F[_]: Async](xa: Transactor[F]) extends SubscriptionRepositoryAlgebra[F] with DBOps {

  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]
  override def create(id: UUID, url: String): F[UUID] = {
    val insertSql =
      sql"INSERT INTO subscriptions (id, url) VALUES ($id, $url) RETURNING id"
        .query[UUID]
        .unique

    insertSql
      .transact(xa)
      .handleAndLog
  }

  override def delete(id: UUID): F[Unit] = {
    val deleteSql =
      sql"DELETE FROM subscriptions WHERE id = $id".update.run

    deleteSql
      .transact(xa)
      .void
      .handleAndLog
  }
}

object SubscriptionRepository {
  def apply[F[_]: Async](xa: Transactor[F]) = new SubscriptionRepository[F](xa)
}
