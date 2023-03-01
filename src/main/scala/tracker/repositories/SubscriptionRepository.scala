package tracker.repositories

import cats.effect.Async
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

import java.util.UUID

trait SubscriptionRepository[F[_]] {
  def create(id: UUID, url: String): F[UUID]
  def delete(id: UUID): F[Unit]
}

class SubscriptionRepositoryImpl[F[_]: Async](xa: Transactor[F]) extends SubscriptionRepository[F] {

  override def create(id: UUID, url: String): F[UUID] = {
    val insertSql =
      sql"INSERT INTO subscriptions (id, url) VALUES ($id, $url) RETURNING id"
        .query[UUID]
        .unique

    insertSql
      .transact(xa)
      .handleErrorWith {
        case e =>
          Async[F].raiseError(e)
      }
  }

  override def delete(id: UUID): F[Unit] = {
    val deleteSql =
      sql"DELETE FROM subscriptions WHERE id = $id".update.run

    deleteSql
      .transact(xa)
      .void
      .handleErrorWith {
        case e =>
          Async[F].raiseError(e)
      }
  }
}
