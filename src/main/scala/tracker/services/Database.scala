package tracker.services

import cats.effect.{Async, IO, Sync}
import cats.syntax.functor._
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux
import org.flywaydb.core.Flyway
import tracker.models.configs.AppConfig

object Database {
  def dbTransactor[F[_]: Async](appConfig: AppConfig): Aux[IO, Unit] =
    Transactor.fromDriverManager[IO](
      appConfig.database.driver,
      appConfig.database.url,
      appConfig.database.user,
      appConfig.database.password
    )

  /**
    * Runs the flyway migration
    */
  def initializeDb[F[_]](appConfig: AppConfig)(implicit S: Sync[F]): F[Unit] =
    S.delay {
        val fw: Flyway =
          Flyway
            .configure()
            .dataSource(appConfig.database.url, appConfig.database.user, appConfig.database.password)
            .load()
        fw.migrate()
      }
      .as(())
}
