package com.maantrack.repository.doobies

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import com.maantrack.domain.board.{ Board, BoardRepository, BoardRequest }
import com.maantrack.domain.user.board.AppUserBoard
import com.maantrack.repository.doobies.Doobie._
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import doobie.{ Fragments, Query0, Update0 }
import io.chrisdavenport.log4cats.Logger

object BoardSQL {
  import Fragments.whereAnd

  def byId(id: Long): Query0[Board] =
    (select ++ whereAnd(fr"board_id = $id"))
      .query[Board]

  private def select: Fragment =
    fr"""
        select
             board_id, name, description, closed, pinned, board_url, starred ,created_date, modified_date
        from board
      """

  def insert(board: BoardRequest): Update0 =
    sql"""
         insert into board
               (name, description, closed, pinned, board_url,
               starred , created_date, modified_date)
         values
              ( ${board.name}, ${board.description}, ${board.closed}
              , ${board.pinned}, ${board.boardUrl}, ${board.starred}, NOW(), NOW())
       """.update

  def update(board: Board): Update0 =
    sql"""
         update board    
         set name = ${board.name}
         where board_id = ${board.boardId}
       """.update

  def delete(id: Long): Update0 =
    sql"""
         delete from board
         where board_id = $id
       """.update
}

class BoardRepositoryInterpreter[F[_]: Sync: Logger](xa: Transactor[F]) extends BoardRepository[F] {
  import BoardSQL._

  override def add(userId: Long, boardRequest: BoardRequest): F[Long] =
    (for {
      id <- insert(boardRequest)
             .withUniqueGeneratedKeys[Long]("board_id")
      _ <- AppUserBoardSQL.insert(AppUserBoard(userId, id)).run
    } yield id).transact(xa)

  override def getById(id: Long): OptionT[F, Board] = OptionT(byId(id).option.transact(xa))

  override def deleteById(id: Long): OptionT[F, Board] =
    getById(id)
      .semiflatMap(board => delete(id).run.transact(xa).as(board))

  override def update(board: Board): F[Int] = BoardSQL.update(board).run.transact(xa)
}
