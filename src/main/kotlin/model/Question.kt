package model

data class Question(
    val options: List<Word>,
    val correctIndex: Int
)
