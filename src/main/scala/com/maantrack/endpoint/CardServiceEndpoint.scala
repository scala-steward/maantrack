package com.maantrack.endpoint

import cats.implicits._
import cats.effect.Sync
import com.maantrack.domain.card.{ CardRequest, CardService }
import com.maantrack.domain.user.User
import org.http4s.dsl.Http4sDsl
import tsec.authentication.{ SecuredRequestHandler, TSecAuthService, TSecBearerToken, asAuthed }
import org.http4s.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.HttpRoutes

class CardServiceEndpoint[F[_]: Sync](
  cardService: CardService[F],
  Auth: SecuredRequestHandler[F, Long, User, TSecBearerToken[Long]]
) extends Http4sDsl[F] {

  private val cardEndpoint: AuthService[F] = TSecAuthService {
    case req @ POST -> Root asAuthed _ =>
      for {
        cardReq <- req.request.as[CardRequest]
        cardId  <- cardService.add(cardReq)
        res     <- Ok(cardId.asJson)
      } yield res

    case GET -> Root / LongVar(cardId) asAuthed _ =>
      cardService.getById(cardId).value.flatMap {
        case Some(card) => Ok(card.asJson)
        case None       => NotFound(s"Card with card id $cardId not found".asJson)
      }

    case DELETE -> Root / LongVar(cardId) asAuthed _ =>
      cardService.deleteById(cardId).value.flatMap {
        case Some(list) => Ok(list.asJson)
        case None       => NotFound(s"Card with card id $cardId not found".asJson)
      }
  }

  val service: HttpRoutes[F] = Auth.liftService(cardEndpoint)
}
