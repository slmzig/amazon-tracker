package tracker.models.configs

case class DatabaseConfig(
    driver: String,
    url: String,
    user: String,
    password: String,
    path: String
)
