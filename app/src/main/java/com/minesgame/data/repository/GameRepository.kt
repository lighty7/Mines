package com.minesgame.data.repository

import com.minesgame.data.engine.MinesEngine

/**
 * Source of hidden board generation - the seam where a real-money,
 * server-authoritative backend is plugged in later.
 *
 * For a remote implementation, this becomes `suspend` and the board is
 * generated server-side and returned as a committed hash.
 */
interface GameRepository {
    fun createBoard(mines: Int, boardSize: Int = MinesEngine.DEFAULT_BOARD_SIZE): Set<Int>
}

class LocalGameRepository : GameRepository {
    override fun createBoard(mines: Int, boardSize: Int): Set<Int> =
        MinesEngine.generateMinePositions(mines, boardSize)
}
