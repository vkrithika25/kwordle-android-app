package org.krithika.Kwordle

enum class ClueType(val ch: Char) {
    CORRECT_CHAR('G'),
    MISPLACED_CHAR('Y'),
    WRONG_CHAR('_');

    companion object {
        fun valueOf(ch: Char): ClueType {
            return when (ch) {
                'G' -> CORRECT_CHAR
                'Y' -> MISPLACED_CHAR
                else -> WRONG_CHAR
            }
        }
    }
}