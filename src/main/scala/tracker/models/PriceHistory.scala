package tracker.models

import java.time.LocalDateTime
import java.util.UUID

case class PriceHistory(subscriptionId:UUID, price: BigDecimal, createDate: LocalDateTime)
