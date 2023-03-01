package tracker.models

case class AppConfig(
    database: DatabaseConfig,
    http: HttpConfig,
    scheduler: SchedulerConfig
)
