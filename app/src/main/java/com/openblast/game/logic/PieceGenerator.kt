package com.openblast.game.logic

import com.openblast.game.model.Board
import com.openblast.game.model.Piece
import kotlin.random.Random

object PieceGenerator {

    // All standard Block Blast piece templates
    private val TEMPLATES: List<Pair<String, List<List<Boolean>>>> = listOf(
        // Dot 1x1
        "Dot 1x1" to listOf(listOf(true)),

        // Lines
        "Line 1x2" to listOf(listOf(true, true)),
        "Line 2x1" to listOf(listOf(true), listOf(true)),
        "Line 1x3" to listOf(listOf(true, true, true)),
        "Line 3x1" to listOf(listOf(true), listOf(true), listOf(true)),
        "Line 1x4" to listOf(listOf(true, true, true, true)),
        "Line 4x1" to listOf(listOf(true), listOf(true), listOf(true), listOf(true)),
        "Line 1x5" to listOf(listOf(true, true, true, true, true)),
        "Line 5x1" to listOf(listOf(true), listOf(true), listOf(true), listOf(true), listOf(true)),

        // Squares
        "Square 2x2" to listOf(
            listOf(true, true),
            listOf(true, true)
        ),
        "Square 3x3" to listOf(
            listOf(true, true, true),
            listOf(true, true, true),
            listOf(true, true, true)
        ),

        // Rectangles
        "Rect 2x3" to listOf(
            listOf(true, true, true),
            listOf(true, true, true)
        ),
        "Rect 3x2" to listOf(
            listOf(true, true),
            listOf(true, true),
            listOf(true, true)
        ),

        // Small Corners 2x2 (3 blocks)
        "Small Corner TL" to listOf(
            listOf(true, true),
            listOf(true, false)
        ),
        "Small Corner TR" to listOf(
            listOf(true, true),
            listOf(false, true)
        ),
        "Small Corner BL" to listOf(
            listOf(true, false),
            listOf(true, true)
        ),
        "Small Corner BR" to listOf(
            listOf(false, true),
            listOf(true, true)
        ),

        // Big Corners 3x3 (5 blocks)
        "Big Corner TL" to listOf(
            listOf(true, true, true),
            listOf(true, false, false),
            listOf(true, false, false)
        ),
        "Big Corner TR" to listOf(
            listOf(true, true, true),
            listOf(false, false, true),
            listOf(false, false, true)
        ),
        "Big Corner BL" to listOf(
            listOf(true, false, false),
            listOf(true, false, false),
            listOf(true, true, true)
        ),
        "Big Corner BR" to listOf(
            listOf(false, false, true),
            listOf(false, false, true),
            listOf(true, true, true)
        ),

        // Tetris L & J
        "Tetris L 1" to listOf(
            listOf(true, false),
            listOf(true, false),
            listOf(true, true)
        ),
        "Tetris L 2" to listOf(
            listOf(false, false, true),
            listOf(true, true, true)
        ),
        "Tetris L 3" to listOf(
            listOf(true, true),
            listOf(false, true),
            listOf(false, true)
        ),
        "Tetris L 4" to listOf(
            listOf(true, true, true),
            listOf(true, false, false)
        ),

        "Tetris J 1" to listOf(
            listOf(false, true),
            listOf(false, true),
            listOf(true, true)
        ),
        "Tetris J 2" to listOf(
            listOf(true, true, true),
            listOf(false, false, true)
        ),
        "Tetris J 3" to listOf(
            listOf(true, true),
            listOf(true, false),
            listOf(true, false)
        ),
        "Tetris J 4" to listOf(
            listOf(true, false, false),
            listOf(true, true, true)
        ),

        // Tetris T
        "Tetris T Down" to listOf(
            listOf(true, true, true),
            listOf(false, true, false)
        ),
        "Tetris T Up" to listOf(
            listOf(false, true, false),
            listOf(true, true, true)
        ),
        "Tetris T Right" to listOf(
            listOf(true, false),
            listOf(true, true),
            listOf(true, false)
        ),
        "Tetris T Left" to listOf(
            listOf(false, true),
            listOf(true, true),
            listOf(false, true)
        ),

        // Tetris S and Z
        "Tetris Z Horiz" to listOf(
            listOf(true, true, false),
            listOf(false, true, true)
        ),
        "Tetris Z Vert" to listOf(
            listOf(false, true),
            listOf(true, true),
            listOf(true, false)
        ),
        "Tetris S Horiz" to listOf(
            listOf(false, true, true),
            listOf(true, true, false)
        ),
        "Tetris S Vert" to listOf(
            listOf(true, false),
            listOf(true, true),
            listOf(false, true)
        ),

        // Cross / Plus
        "Cross" to listOf(
            listOf(false, true, false),
            listOf(true, true, true),
            listOf(false, true, false)
        )
    )

    fun generatePieces(board: Board, count: Int = 3, maxColors: Int = 7): List<Piece> {
        val result = mutableListOf<Piece>()
        val occupied = board.occupiedCount()
        val isCongested = occupied >= 30 // More than ~45% of board is occupied
        val isCritical = occupied >= 44  // More than ~70% occupied

        // Pre-evaluate all templates against the current board state
        data class EvaluatedTemplate(
            val name: String,
            val matrix: List<List<Boolean>>,
            val blockCount: Int,
            val maxLinesCleared: Int,
            val validPlacementsCount: Int
        )

        val evaluated = TEMPLATES.map { (name, matrix) ->
            val temp = Piece(name = name, matrix = matrix)
            val placements = board.countValidPlacements(temp)
            val linesCleared = if (placements > 0) board.maxLinesCanClear(temp) else 0
            EvaluatedTemplate(
                name = name,
                matrix = matrix,
                blockCount = temp.blockCount,
                maxLinesCleared = linesCleared,
                validPlacementsCount = placements
            )
        }

        val fittable = evaluated.filter { it.validPlacementsCount > 0 }
        val lineClearers = fittable.filter { it.maxLinesCleared > 0 }

        // Slot 1: ADAPTIVE FINISHER (Actively targets almost completed lines)
        var slot1: EvaluatedTemplate? = null
        if (lineClearers.isNotEmpty()) {
            slot1 = lineClearers.maxByOrNull { it.maxLinesCleared * 10 + it.validPlacementsCount }
        } else if (isCongested) {
            slot1 = fittable.filter { it.blockCount <= 4 }.maxByOrNull { it.validPlacementsCount }
        }
        if (slot1 == null) {
            slot1 = fittable.shuffled(Random).firstOrNull() ?: evaluated.random(Random)
        }

        // Slot 2: BUILDER (Fills remaining empty spaces smoothly)
        val remainingFittable = fittable.filter { it.name != slot1.name }
        var slot2: EvaluatedTemplate? = null
        if (isCritical) {
            slot2 = remainingFittable.filter { it.blockCount <= 3 }.maxByOrNull { it.validPlacementsCount }
        } else if (lineClearers.size >= 2) {
            slot2 = lineClearers.firstOrNull { it.name != slot1.name }
        }
        if (slot2 == null) {
            slot2 = remainingFittable.maxByOrNull { it.validPlacementsCount }
                ?: fittable.shuffled(Random).firstOrNull()
                ?: evaluated.random(Random)
        }

        // Slot 3: STRATEGIC VARIETY (Balances game flow, prevents impossible traps)
        val pool3 = if (isCritical) {
            fittable.filter { it.blockCount <= 3 }
        } else if (isCongested) {
            fittable.filter { it.blockCount <= 5 && it.name != "Square 3x3" && it.name != "Line 5x1" && it.name != "Line 1x5" }
        } else {
            fittable
        }

        val slot3 = pool3.filter { it.name != slot1.name && it.name != slot2.name }.shuffled(Random).firstOrNull()
            ?: pool3.shuffled(Random).firstOrNull()
            ?: fittable.shuffled(Random).firstOrNull()
            ?: evaluated.random(Random)

        val selected = listOf(slot1, slot2, slot3)
        for (tmpl in selected.take(count)) {
            val colorIndex = Random.nextInt(maxColors)
            result.add(
                Piece(
                    name = tmpl.name,
                    matrix = tmpl.matrix,
                    colorIndex = colorIndex
                )
            )
        }

        return result
    }
}
