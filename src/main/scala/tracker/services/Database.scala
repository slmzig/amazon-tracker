package tracker.services

import cats.effect.IO
import com.typesafe.config.Config
import org.flywaydb.core.Flyway
import tracker.models.AppConfig

import scala.concurrent.{ExecutionContext, Future}

class Database(config: AppConfig) {
  def migrate():IO[Unit] =
    IO {
      Flyway
        .configure()
        .dataSource(config.database.url, config.database.user, config.database.password)
        .locations(config.database.path)
        .load()
        .migrate()
    }
}
