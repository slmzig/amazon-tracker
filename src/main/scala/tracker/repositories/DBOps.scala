package tracker.repositories

import cats.effect.Async
import cats.implicits._
import org.typelevel.log4cats.Logger
import tracker.models.errors.DatabaseError

private[repositories] trait DBOps {
  implicit class FOps[F[_]: Async, A](val sql: F[A]) {
    def handleAndLog(implicit logger: Logger[F]): F[A] =
      sql.handleErrorWith { e =>
        Logger[F].error(e.toString) >>
          DatabaseError.raiseError[F, A]
      }
  }
}
