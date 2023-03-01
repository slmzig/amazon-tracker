package tracker.models.errors

import scala.util.control.NoStackTrace

case object CurrentlyUnavailable      extends NoStackTrace
case object PriceNotFoundInText       extends NoStackTrace
case object BuyBoxNotFound            extends NoStackTrace
case object ConnectionToProductFailed extends NoStackTrace
