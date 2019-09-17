package com.maantrack.domain.board
import cats.data.OptionT

trait BoardRepository[F[_]] {

  def add(userId: Long, boardRequest: BoardRequest): F[Long]

  def getById(id: Long): OptionT[F, Board]

  def deleteById(id: Long): OptionT[F, Board]

  def update(board: Board): F[Int]
}