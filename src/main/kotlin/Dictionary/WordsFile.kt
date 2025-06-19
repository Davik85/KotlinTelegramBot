package org.example.WordsFile

import java.io.File

fun main() {
    val wordsFile: File = File("words.txt")
    wordsFile.createNewFile()

    wordsFile.writeText("hello привет\n")
    wordsFile.appendText("dog собака\n")
    wordsFile.appendText("cat кошка\n")

    val lines = wordsFile.readLines()
    println("Содержимое словаря:")
    for (line in lines) {
        println(line)
    }
}
