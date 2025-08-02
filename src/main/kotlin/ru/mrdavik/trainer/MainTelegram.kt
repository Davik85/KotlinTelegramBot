package ru.mrdavik.trainer

import ru.mrdavik.trainer.telegram.*
import ru.mrdavik.trainer.telegram.dto.TelegramResponse
import ru.mrdavik.trainer.trainer.LearnWordsTrainer
import java.io.File

const val COMMAND_MENU = "/menu"
const val COMMAND_START = "/start"
const val BOT_USERNAME = "@EnglishWordsDavikBot"
val COMMAND_MENU_FULL = "$COMMAND_MENU$BOT_USERNAME"
val COMMAND_START_FULL = "$COMMAND_START$BOT_USERNAME"

val BUILT_IN_DICT_PATHS = mapOf(
    CALLBACK_LOAD_DICT_TOP100 to "dictionaries/top100.txt",
    CALLBACK_LOAD_DICT_TRAVEL to "dictionaries/travel.txt",
    CALLBACK_LOAD_DICT_IT to "dictionaries/it.txt",
    CALLBACK_LOAD_DICT_FOOD to "dictionaries/food.txt",
    CALLBACK_LOAD_DICT_BASIC to "dictionaries/basic.txt"
)

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Ошибка: Необходимо передать токен бота в качестве аргумента командной строки.")
        return
    }
    val botToken = args[0]
    var lastUpdateId = 0L

    val botService = TelegramBotService(botToken)
    val trainers = mutableMapOf<Long, LearnWordsTrainer>()
    val userDictTitles = mutableMapOf<Long, String>() // название текущего словаря для каждого пользователя
    val userHasCustomDict = mutableSetOf<Long>() // чаты где есть пользовательский словарь

    while (true) {
        try {
            Thread.sleep(2000)
            val response: TelegramResponse = botService.getUpdates(lastUpdateId)
            var maxUpdateId = lastUpdateId

            for (update in response.result) {
                try {
                    if (update.updateId > maxUpdateId) {
                        maxUpdateId = update.updateId
                    }
                    val chatId: Long = update.message?.chat?.id
                        ?: update.callbackQuery?.message?.chat?.id
                        ?: continue

                    val trainer = trainers.getOrPut(chatId) {
                        LearnWordsTrainer(fileName = "words_$chatId.txt")
                    }

                    val document = update.message?.document
                    if (document != null) {
                        val fileName = document.fileName
                        val getFileResp = botService.getFile(document.fileId)
                        val filePath = getFileResp.result?.filePath
                        if (filePath != null) {
                            val rawFileName = "raw_${chatId}_$fileName"
                            val txtFileName = "words_$chatId.txt"
                            botService.downloadFile(filePath, rawFileName)
                            WordsFileParser.parseAnyFormat(rawFileName, txtFileName)
                            trainer.reloadDictionary(txtFileName)
                            userHasCustomDict.add(chatId)
                            userDictTitles[chatId] = "Свой словарь"
                            botService.sendMessage(chatId, "Словарь успешно загружен и обновлён!")
                        }
                        continue
                    }

                    val text = update.message?.text
                    val data = update.callbackQuery?.data

                    if (
                        text?.equals(COMMAND_MENU, ignoreCase = true) == true ||
                        text?.equals(COMMAND_START, ignoreCase = true) == true ||
                        text?.equals(COMMAND_MENU_FULL, ignoreCase = true) == true ||
                        text?.equals(COMMAND_START_FULL, ignoreCase = true) == true
                    ) {
                        botService.sendMainMenu(chatId, userHasCustomDict.contains(chatId))
                        continue
                    }

                    if (data in BUILT_IN_DICT_PATHS.keys) {
                        val dictPath = BUILT_IN_DICT_PATHS[data]
                        val dictTitle = BUILT_IN_DICTIONARIES.find { it.second == data }?.first ?: "Словарь"
                        if (dictPath != null) {
                            val userDictFile = File("words_$chatId.txt")
                            val stream = object {}.javaClass.classLoader.getResourceAsStream(dictPath)
                                ?: File(dictPath).inputStream()
                            userDictFile.outputStream().use { output -> stream.copyTo(output) }
                            trainer.reloadDictionary(userDictFile.absolutePath)
                            userDictTitles[chatId] = dictTitle
                            botService.sendDictionaryMenu(chatId, dictTitle)
                        }
                        continue
                    }

                    if (data == CALLBACK_MY_DICTIONARY) {
                        userDictTitles[chatId] = "Свой словарь"
                        trainer.reloadDictionary("words_$chatId.txt")
                        botService.sendDictionaryMenu(chatId, "Свой словарь")
                        continue
                    }

                    if (data == CALLBACK_BACK_TO_MAIN) {
                        botService.sendMainMenu(chatId, userHasCustomDict.contains(chatId))
                        continue
                    }

                    if (data == CALLBACK_UPLOAD_YOURS) {
                        botService.sendMessage(
                            chatId,
                            """
                            Для загрузки своего словаря отправьте TXT или CSV файл боту.
                            Формат: каждое слово на новой строке, разделитель - запятая, точка с запятой, табуляция или | .
                            Пример:
                            table|стол
                            cat|кошка
                            hello|привет
                            ИИ хорошо справляются с этой задачей, просто укажите тему и количество слов в нужном формате, потом вставьте эти слова в текстовый файл и отправьте боту. 
                            Обратите внимание, что при загрузке нового словаря, он будет заменять старый. Нельзя одновременно загрузить больше одного словаря.
                            """.trimIndent()
                        )
                        continue
                    }

                    if (data == CALLBACK_LEARN_WORDS) {
                        val dictTitle = userDictTitles[chatId] ?: "Словарь"
                        val question = trainer.nextQuestion()
                        if (question == null) {
                            botService.sendMessage(chatId, "Все слова в словаре \"$dictTitle\" выучены!")
                        } else {
                            botService.sendQuestion(chatId, question)
                        }
                        continue
                    }
                    if (data == CALLBACK_STATS) {
                        val dictTitle = userDictTitles[chatId] ?: "Словарь"
                        val stats = trainer.statisticsString()
                        botService.sendMessage(chatId, "Статистика по словарю \"$dictTitle\":\n$stats")
                        continue
                    }
                    if (data == CALLBACK_RESET_STATS) {
                        trainer.resetProgress()
                        botService.sendMessage(chatId, "Статистика сброшена для выбранного словаря!")
                        val dictTitle = userDictTitles[chatId] ?: "Словарь"
                        botService.sendDictionaryMenu(chatId, dictTitle)
                        continue
                    }

                    if (data?.startsWith("answer_") == true) {
                        val answerIdx = data.removePrefix("answer_").toIntOrNull()
                        if (answerIdx != null) {
                            val isCorrect = trainer.checkAnswer(answerIdx)
                            if (isCorrect) {
                                botService.sendMessage(chatId, "Правильно!")
                            } else {
                                val currentQuestion = trainer.getCurrentQuestion()
                                if (currentQuestion != null) {
                                    val correct = currentQuestion.options[currentQuestion.correctIndex]
                                    botService.sendMessage(
                                        chatId,
                                        "Неправильно! ${correct.original} – это ${correct.translate}"
                                    )
                                }
                            }
                            val question = trainer.nextQuestion()
                            if (question == null) {
                                botService.sendMessage(chatId, "Все слова выучены!")
                            } else {
                                botService.sendQuestion(chatId, question)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("ОШИБКА при обработке update: ${e.message}")
                    e.printStackTrace()
                }
            }
            if (response.result.isNotEmpty()) {
                lastUpdateId = maxUpdateId + 1
            }
        } catch (e: Exception) {
            println("ГЛОБАЛЬНАЯ ОШИБКА основного цикла: ${e.message}")
            e.printStackTrace()
            Thread.sleep(5000)
        }
    }
}
