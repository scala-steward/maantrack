package com.maantrack.domain.cardlist
import cats.data.OptionT

trait CardListRepository[F[_]] {

  def add(listRequest: CardListRequest): F[Long]

  def getById(id: Long): OptionT[F, CardList]

  def deleteById(id: Long): OptionT[F, CardList]

  def update(card: CardList): F[Int]
}