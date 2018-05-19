package com.transcendensoft.model

interface BotCommons {
    companion object {
        const val TOKEN = "586977338:AAEHGaEAwj9phkpEZ6QY0P3SbvGCXCkj_GQ"
        const val BOT_NAME = "SdamKvartirBot"
        const val USER_TELEGRAM_ID = 252991258
        const val COMMAND_START = "start";
        const val COMMAND_HELP = "help";
        const val COMMAND_CREATE_POST = "createpost";

        const val HELP_TEXT = """/${COMMAND_START}: запуск бота
            |/${COMMAND_HELP}: справка по командам бота
            |/${COMMAND_CREATE_POST}: создать новый пост о сдаче апартаментов"""

        //DB
        const val USER_MAP = "userMap"
    }
}