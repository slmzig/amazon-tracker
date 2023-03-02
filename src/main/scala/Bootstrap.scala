import cats.effect._
import doobie.WeakAsync.doobieWeakAsyncForAsync
import fs2._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import tracker.models.AppConfig
import tracker.repositories.{PriceChangeRepository, SubscriptionRepository}
import tracker.server.SubscriptionRoutes
import tracker.services.{Database, Parser, PriceTrackerService, SubscriptionService}

object Bootstrap extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {

    implicit val logger = Slf4jLogger.getLogger[IO]

    val application = for {
      config                 <- ConfigSource.default.loadF[IO, AppConfig]
      tr                     = Database.dbTransactor[IO](config)
      parser                 = Parser[IO]
      priceChangeRepository  = PriceChangeRepository[IO](tr)
      subscriptionRepository = SubscriptionRepository[IO](tr)
      service                = SubscriptionService[IO](subscriptionRepository, priceChangeRepository, parser)
      priseTracker           = PriceTrackerService[IO](priceChangeRepository, parser)
      _                      <- Database.initializeDb[IO](config)
      routes = Router(
        "/" -> SubscriptionRoutes.make[IO](service)
      ).orNotFound
      stream = Stream.repeatEval(priseTracker.trackPrices()).metered(config.scheduler.delay).repeat
      server = BlazeServerBuilder[IO]
        .bindHttp(config.http.port, config.http.host)
        .withHttpApp(routes)
        .serve
      app <- (server.parZip(stream.drain)).compile.drain
    } yield app

    application.as(ExitCode.Success)

  }
}
