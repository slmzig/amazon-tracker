package tracker.models.configs

case class AppConfig(
    database: DatabaseConfig,
    http: HttpConfig,
    scheduler: SchedulerConfig
)
