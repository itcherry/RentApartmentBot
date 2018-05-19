package com.transcendensoft

import com.transcendensoft.handler.RentBot
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.exceptions.TelegramApiException

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    val telegramBotsApi = TelegramBotsApi()
    try {
        telegramBotsApi.registerBot(RentBot())
        //TODO registerBots
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }
}