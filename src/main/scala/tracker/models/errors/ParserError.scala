package tracker.models.errors

import scala.util.control.NoStackTrace

sealed trait ParserError              extends NoStackTrace
case object CurrentlyUnavailable      extends ParserError
case object PriceNotFoundInText       extends ParserError
case object BuyBoxNotFound            extends ParserError
case object ConnectionToProductFailed extends ParserError
