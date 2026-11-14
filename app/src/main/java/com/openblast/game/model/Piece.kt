package com.openblast.game.model

import java.util.UUID

data class StripInfo(
    val r: Int,
    val c: Int,
    val w: Int,
    val h: Int
)

data class Piece(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val matrix: List<List<Boolean>>,
    val colorIndex: Int = 0
) {
    val height: Int = matrix.size
    val width: Int = if (matrix.isNotEmpty()) matrix[0].size else 0

    val blockCount: Int = matrix.sumOf { row -> row.count { it } }

    operator fun get(row: Int, col: Int): Boolean {
        if (row !in 0 until height || col !in 0 until width) return false
        return matrix[row][col]
    }

    fun partitionIntoStrips(): List<StripInfo> {
        val occupied = Array(height) { r ->
            BooleanArray(width) { c -> this[r, c] }
        }
        val strips = mutableListOf<StripInfo>()

        val totalCount = blockCount
        var minR = height
        var maxR = -1
        var minC = width
        var maxC = -1
        for (r in 0 until height) {
            for (c in 0 until width) {
                if (occupied[r][c]) {
                    if (r < minR) minR = r
                    if (r > maxR) maxR = r
                    if (c < minC) minC = c
                    if (c > maxC) maxC = c
                }
            }
        }
        val bboxW = if (maxC >= minC) maxC - minC + 1 else 0
        val bboxH = if (maxR >= minR) maxR - minR + 1 else 0
        if (totalCount == bboxW * bboxH && bboxW > 0 && bboxH > 0) {
            return listOf(StripInfo(minR, minC, bboxW, bboxH))
        }

        var bestVertCol = -1
        var bestVertStartR = -1
        var bestVertLen = 0
        for (c in 0 until width) {
            var currLen = 0
            var currStart = -1
            for (r in 0 until height) {
                if (occupied[r][c]) {
                    if (currLen == 0) currStart = r
                    currLen++
                    if (currLen > bestVertLen) {
                        bestVertLen = currLen
                        bestVertStartR = currStart
                        bestVertCol = c
                    }
                } else {
                    currLen = 0
                }
            }
        }

        var bestHorizRow = -1
        var bestHorizStartC = -1
        var bestHorizLen = 0
        for (r in 0 until height) {
            var currLen = 0
            var currStart = -1
            for (c in 0 until width) {
                if (occupied[r][c]) {
                    if (currLen == 0) currStart = c
                    currLen++
                    if (currLen > bestHorizLen) {
                        bestHorizLen = currLen
                        bestHorizStartC = currStart
                        bestHorizRow = r
                    }
                } else {
                    currLen = 0
                }
            }
        }

        if (bestVertLen >= bestHorizLen && bestVertLen >= 2) {
            strips.add(StripInfo(bestVertStartR, bestVertCol, 1, bestVertLen))
            for (r in bestVertStartR until (bestVertStartR + bestVertLen)) {
                occupied[r][bestVertCol] = false
            }
        } else if (bestHorizLen >= 2) {
            strips.add(StripInfo(bestHorizRow, bestHorizStartC, bestHorizLen, 1))
            for (c in bestHorizStartC until (bestHorizStartC + bestHorizLen)) {
                occupied[bestHorizRow][c] = false
            }
        }

        for (r in 0 until height) {
            var startC = -1
            var len = 0
            for (c in 0 until width) {
                if (occupied[r][c]) {
                    if (len == 0) startC = c
                    len++
                } else {
                    if (len >= 2) {
                        strips.add(StripInfo(r, startC, len, 1))
                        for (i in startC until (startC + len)) occupied[r][i] = false
                    }
                    len = 0
                }
            }
            if (len >= 2) {
                strips.add(StripInfo(r, startC, len, 1))
                for (i in startC until (startC + len)) occupied[r][i] = false
            }
        }

        for (r in 0 until height) {
            for (c in 0 until width) {
                if (occupied[r][c]) {
                    strips.add(StripInfo(r, c, 1, 1))
                    occupied[r][c] = false
                }
            }
        }

        return strips
    }
}
