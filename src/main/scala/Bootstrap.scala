import cats.effect._
import doobie.WeakAsync.doobieWeakAsyncForAsync
import doobie._
import fs2._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import tracker.repositories.{PriceChangeRepositoryImpl, SubscriptionRepositoryImpl}
import tracker.server.SubscriptionRoutes
import tracker.services.{Database, ParserImpl, PriceTrackerServiceImpl, SubscriptionServiceImpl}
import org.typelevel.log4cats.Logger
import cats.syntax.all._
import cats.implicits._
import doobie.util.transactor.Transactor.Aux
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import tracker.models.AppConfig

import scala.concurrent.duration._

object Bootstrap extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    implicit val logger = Slf4jLogger.getLogger[IO]

    import cats.effect.unsafe.implicits.global

    val appConfig = ConfigSource.default.loadF[IO, AppConfig].unsafeRunSync()

    val transactor = Transactor.fromDriverManager[IO](
      appConfig.database.driver,
      appConfig.database.url,
      appConfig.database.user,
      appConfig.database.password
    )

    val parser                 = new ParserImpl[IO]()
    val priceChangeRepository  = new PriceChangeRepositoryImpl[IO](transactor)
    val subscriptionRepository = new SubscriptionRepositoryImpl[IO](transactor)
    val service                = new SubscriptionServiceImpl[IO](subscriptionRepository, priceChangeRepository, parser)
    val priseTracker           = new PriceTrackerServiceImpl[IO](priceChangeRepository, parser)
    val migration              = new Database(appConfig)
    val apis = Router(
      "/" -> SubscriptionRoutes.make[IO](service, priseTracker)
    ).orNotFound

    val httpServer: Stream[IO, ExitCode] = BlazeServerBuilder[IO]
      .bindHttp(appConfig.http.port, appConfig.http.host)
      .withHttpApp(apis)
      .serve

    val stream: Stream[IO, Unit] = Stream.repeatEval(priseTracker.trackPrices()).metered(appConfig.scheduler.delay).repeat
    val app: IO[Unit]            = (httpServer.parZip(stream.drain)).compile.drain

    Logger[IO].info("start application") >>
      migration.migrate() >>
      app.as(ExitCode.Success)
  }
}
