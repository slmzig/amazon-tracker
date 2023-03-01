package tracker.models

case class DatabaseConfig(
    driver: String,
    url: String,
    user: String,
    password: String,
    path: String
)
