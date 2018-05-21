package com.transcendensoft.model

interface BotCommons {
    companion object {
        const val TOKEN = "586977338:AAEHGaEAwj9phkpEZ6QY0P3SbvGCXCkj_GQ"
        const val BOT_NAME = "SdamKvartirBot"
        const val USER_TELEGRAM_ID = 252991258

        const val COMMAND_START = "start";
        const val COMMAND_HELP = "help";
        const val COMMAND_CREATE_POST = "createnewpost";
        const val COMMAND_CANCEL = "cancel";
        const val COMMAND_RENTED = "rented";
        const val COMMAND_FREE = "free";

        const val HELP_TEXT = """Бот канала <b>КвартирХаб</b> для тех, кто хочет разместить объявление о сдачи апартаментов
            |
            |/$COMMAND_HELP: справка по командам бота;
            |/$COMMAND_CREATE_POST: создать новый пост о сдаче апартаментов;
            |/$COMMAND_CANCEL: отменить создание поста о сдаче;
            |/$COMMAND_RENTED: отметить, что апартаменты сданы;
            |/$COMMAND_FREE: отметить, что апартаменты свободны и пока не сданы."""

        //DB
        const val USER_MAP = "userMap"
        const val ID_OF_GROUP_WITH_POSTS = -252092566L
    }
}