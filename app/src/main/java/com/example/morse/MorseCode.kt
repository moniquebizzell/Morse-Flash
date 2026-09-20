package com.example.morse

/**
 * Standard International Morse Code mapping and utility functions.
 */
object MorseCode {
    private val CHAR_TO_MORSE = mapOf(
        'A' to ".-",    'B' to "-...",  'C' to "-.-.",  'D' to "-..",
        'E' to ".",     'F' to "..-.",  'G' to "--.",   'H' to "....",
        'I' to "..",    'J' to ".---",  'K' to "-.-",   'L' to ".-..",
        'M' to "--",    'N' to "-.",    'O' to "---",   'P' to ".--.",
        'Q' to "--.-",  'R' to ".-.",   'S' to "...",   'T' to "-",
        'U' to "..-",   'V' to "...-",  'W' to ".--",   'X' to "-..-",
        'Y' to "-.--",  'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--",
        '4' to "....-", '5' to ".....", '6' to "-....", '7' to "--...",
        '8' to "---..", '9' to "----.",
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '!' to "-.-.--",
        ':' to "---...", '-' to "-....-", '/' to "-..-.", '=' to "-...-",
        ' ' to "/"
    )

    // Standard timing units based on standard Morse timing rules
    const val DOT_TIME_MS = 180L
    const val DASH_TIME_MS = DOT_TIME_MS * 3       // 540ms
    const val INTRA_CHAR_GAP_MS = DOT_TIME_MS      // 180ms between elements of same character
    const val INTER_CHAR_GAP_MS = DOT_TIME_MS * 3  // 540ms between characters
    const val INTER_WORD_GAP_MS = DOT_TIME_MS * 7  // 1260ms between words
    const val SOS_LOOP_INTERVAL_MS = 1400L         // Pause between SOS loop cycles

    /**
     * Translates a string into Morse code representation (symbols separated by spaces, words by " / ").
     */
    fun toMorse(text: String): String {
        return text.trim().uppercase().map { char ->
            CHAR_TO_MORSE[char] ?: ""
        }.filter { it.isNotEmpty() }.joinToString(" ")
    }

    /**
     * Translates a single character into Morse symbols.
     */
    fun charToMorse(char: Char): String? {
        return CHAR_TO_MORSE[char.uppercaseChar()]
    }

    /**
     * Checks if a character is supported in Morse code.
     */
    fun isSupported(char: Char): Boolean {
        return CHAR_TO_MORSE.containsKey(char.uppercaseChar())
    }
}
