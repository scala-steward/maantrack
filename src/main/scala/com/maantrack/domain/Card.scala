package com.maantrack.domain

import java.time.Instant

import cats.effect.Sync
import io.circe.generic.auto._
import io.scalaland.chimney.dsl._
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.{ EntityDecoder, EntityEncoder }

case class Card(
  cardId: Long,
  closed: Boolean,
  description: Option[String],
  due: Instant,
  dueCompleted: Boolean,
  boardId: Long,
  listId: Long,
  name: String,
  pos: Int,
  createdDate: Instant,
  modifiedDate: Instant
)

case class CardRequest(
  closed: Boolean,
  description: Option[String],
  due: Instant,
  dueCompleted: Boolean,
  boardId: Long,
  listId: Long,
  name: String,
  pos: Int
) {
  self =>

  def toCard: Card =
    self
      .into[Card]
      .withFieldConst(_.cardId, 0L)
      .withFieldConst(_.createdDate, Instant.now())
      .withFieldConst(_.modifiedDate, Instant.now())
      .transform
}

object Card {
  implicit def cardDecoder[F[_]: Sync]: EntityDecoder[F, Card] = jsonOf
  implicit def cardEncoder[F[_]: Sync]: EntityEncoder[F, Card] = jsonEncoderOf
}

object CardRequest {
  implicit def cardRequestDecoder[F[_]: Sync]: EntityDecoder[F, CardRequest] = jsonOf
  implicit def cardRequestEncoder[F[_]: Sync]: EntityEncoder[F, CardRequest] = jsonEncoderOf
}
