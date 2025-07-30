package ru.mrdavik.trainer.telegram

import java.io.File

object WordsFileParser {
    /**
     * Поддерживает любые разделители: ',', ';', '\t', '|', а также формат словаря в кавычках.
     * Сохраняет результат в формате: слово|перевод|0
     */
    fun parseAnyFormat(inputFile: String, outputTxtFile: String) {
        val lines = EncodingUtil.readFileAutoEncoding(File(inputFile))
        File(outputTxtFile).bufferedWriter().use { out ->
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue

                val regex = Regex("^\"([^\"]+)\"\\s*[;,\\t]+\\s*\"([^\"]+)\"")
                val match = regex.find(trimmed)
                if (match != null) {
                    val original = match.groupValues[1].trim()
                    val translate = match.groupValues[2].trim()
                    if (original.isNotEmpty() && translate.isNotEmpty()) {
                        out.write("$original|$translate|0\n")
                    }
                    continue
                }

                val parts = trimmed.split('|', ';', ',', '\t').map { it.trim().removeSurrounding("\"") }
                if (parts.size >= 2) {
                    val original = parts[0]
                    val translate = parts[1]
                    if (original.isNotEmpty() && translate.isNotEmpty()) {
                        out.write("$original|$translate|0\n")
                    }
                }
            }
        }
    }
}
