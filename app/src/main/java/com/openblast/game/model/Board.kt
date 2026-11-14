package com.openblast.game.model

data class CompletedLines(
    val rows: List<Int>,
    val cols: List<Int>
) {
    val totalLines: Int = rows.size + cols.size
    val isEmpty: Boolean = totalLines == 0
}

data class Board(
    val size: Int = 8,
    // grid[row][col] -> null if empty, colorIndex if occupied
    val grid: List<List<Int?>> = List(size) { List(size) { null } },
    val cellStrips: Map<Pair<Int, Int>, StripInfo> = emptyMap()
) {

    fun isCellOccupied(row: Int, col: Int): Boolean {
        if (row !in 0 until size || col !in 0 until size) return true
        return grid[row][col] != null
    }

    fun canPlace(piece: Piece, targetRow: Int, targetCol: Int): Boolean {
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece[r, c]) {
                    val boardRow = targetRow + r
                    val boardCol = targetCol + c

                    // Check bounds
                    if (boardRow !in 0 until size || boardCol !in 0 until size) {
                        return false
                    }
                    // Check collision
                    if (grid[boardRow][boardCol] != null) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun canPlaceAnywhere(piece: Piece): Boolean {
        for (r in 0..(size - piece.height)) {
            for (c in 0..(size - piece.width)) {
                if (canPlace(piece, r, c)) {
                    return true
                }
            }
        }
        return false
    }

    fun occupiedCount(): Int = grid.sumOf { row -> row.count { it != null } }

    fun countSimulatedLinesCleared(piece: Piece, targetRow: Int, targetCol: Int): Int {
        if (!canPlace(piece, targetRow, targetCol)) return 0
        var lines = 0
        for (r in 0 until piece.height) {
            val boardR = targetRow + r
            var full = true
            for (c in 0 until size) {
                val occupied = if (c in targetCol until (targetCol + piece.width) && piece[r, c - targetCol]) {
                    true
                } else {
                    grid[boardR][c] != null
                }
                if (!occupied) {
                    full = false
                    break
                }
            }
            if (full) lines++
        }
        for (c in 0 until piece.width) {
            val boardC = targetCol + c
            var full = true
            for (r in 0 until size) {
                val occupied = if (r in targetRow until (targetRow + piece.height) && piece[r - targetRow, c]) {
                    true
                } else {
                    grid[r][boardC] != null
                }
                if (!occupied) {
                    full = false
                    break
                }
            }
            if (full) lines++
        }
        return lines
    }

    fun maxLinesCanClear(piece: Piece): Int {
        var maxCleared = 0
        for (r in 0..(size - piece.height)) {
            for (c in 0..(size - piece.width)) {
                if (canPlace(piece, r, c)) {
                    val cleared = countSimulatedLinesCleared(piece, r, c)
                    if (cleared > maxCleared) {
                        maxCleared = cleared
                    }
                }
            }
        }
        return maxCleared
    }

    fun countValidPlacements(piece: Piece): Int {
        var count = 0
        for (r in 0..(size - piece.height)) {
            for (c in 0..(size - piece.width)) {
                if (canPlace(piece, r, c)) {
                    count++
                }
            }
        }
        return count
    }

    fun hasValidMove(pieces: List<Piece>): Boolean {
        return pieces.any { canPlaceAnywhere(it) }
    }

    fun place(piece: Piece, targetRow: Int, targetCol: Int): Board {
        val newGrid = grid.map { it.toMutableList() }.toMutableList()
        val newCellStrips = cellStrips.toMutableMap()
        val strips = piece.partitionIntoStrips()

        for (strip in strips) {
            val stripInfo = StripInfo(
                r = targetRow + strip.r,
                c = targetCol + strip.c,
                w = strip.w,
                h = strip.h
            )
            for (sr in 0 until strip.h) {
                for (sc in 0 until strip.w) {
                    val r = targetRow + strip.r + sr
                    val c = targetCol + strip.c + sc
                    newCellStrips[r to c] = stripInfo
                }
            }
        }

        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece[r, c]) {
                    newGrid[targetRow + r][targetCol + c] = piece.colorIndex
                }
            }
        }
        return copy(grid = newGrid.map { it.toList() }, cellStrips = newCellStrips)
    }

    fun findCompletedLines(): CompletedLines {
        val fullRows = mutableListOf<Int>()
        for (r in 0 until size) {
            if (grid[r].all { it != null }) {
                fullRows.add(r)
            }
        }

        val fullCols = mutableListOf<Int>()
        for (c in 0 until size) {
            var colFull = true
            for (r in 0 until size) {
                if (grid[r][c] == null) {
                    colFull = false
                    break
                }
            }
            if (colFull) {
                fullCols.add(c)
            }
        }

        return CompletedLines(rows = fullRows, cols = fullCols)
    }

    fun clearLines(rows: List<Int>, cols: List<Int>): Board {
        if (rows.isEmpty() && cols.isEmpty()) return this

        val newGrid = grid.map { it.toMutableList() }.toMutableList()

        for (r in rows) {
            for (c in 0 until size) {
                newGrid[r][c] = null
            }
        }

        for (c in cols) {
            for (r in 0 until size) {
                newGrid[r][c] = null
            }
        }

        val newCellStrips = cellStrips.filterKeys { (r, c) ->
            r !in rows && c !in cols
        }

        return copy(grid = newGrid.map { it.toList() }, cellStrips = newCellStrips)
    }
}
