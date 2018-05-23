package com.transcendensoft.model

interface BotCommons {
    companion object {
        const val TOKEN = "TOKEN"
        const val BOT_NAME = "SdamKvartirBot"
        const val USER_TELEGRAM_ID = 0L

        const val COMMAND_START = "start";
        const val COMMAND_HELP = "help";
        const val COMMAND_CREATE_POST = "createnewpost";
        const val COMMAND_APARTMENT_STATE = "apartmentstate";
        const val COMMAND_CANCEL = "cancel";
        const val COMMAND_SEND_MESSAGE = "sendmessage"

        const val HELP_TEXT = """Бот канала <b>КвартирХаб</b> для тех, кто хочет разместить объявление о сдачи апартаментов
            |
            |/$COMMAND_HELP: справка по командам бота;
            |/$COMMAND_CREATE_POST: создать новый пост о сдаче апартаментов;
            |/$COMMAND_CANCEL: отменить создание поста о сдаче;
            |/$COMMAND_APARTMENT_STATE: указать сданы ли апартаменты или свободны;
            """

        //DB
        const val USER_MAP = "userMap"
        const val ID_OF_GROUP_WITH_POSTS = 0L

        // Share post to KvartirHub
        const val SHARE_TO_CHANNEL_CALLBACK = "shareToChannelCallback"
        const val SHARE_ACTION = "Опубликовать"
        const val KVARTIR_HUB_CHAT_ID = "@ApartmentHub"
        const val PARAMETER_USER_ID = "userId"
        const val PARAMETER_ORDER_ID = "orderId"
    }
}