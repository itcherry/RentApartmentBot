package com.transcendensoft.model.rent_out

import com.transcendensoft.model.rent.RentBotCommons.Companion.COMMAND_APARTMENT_STATE

interface RentOutBotCommons {
    companion object {
        const val BOT_NAME = "SnimuKvartirBot"

        const val COMMAND_START = "start";
        const val COMMAND_HELP = "help";
        const val COMMAND_CREATE_POST = "createnewpost";
        const val COMMAND_CANCEL = "cancel";
        const val COMMAND_SEND_MESSAGE = "sendmessage"

        const val HELP_TEXT = """Бот канала <b>КвартирХаб</b> для тех, кто хочет разместить объявление об аренде апартаментов
            |
            |/$COMMAND_HELP: справка по командам бота;
            |/$COMMAND_CREATE_POST: создать новый пост о сдаче апартаментов;
            |/$COMMAND_CANCEL: отменить создание поста о сдаче;
            """

        //DB
        const val USER_MAP = "userMapRentOut"

        // Share post to KvartirHub
        const val SHARE_TO_CHANNEL_CALLBACK = "shareToChannelCallback"
        const val SHARE_ACTION = "Опубликовать"
        const val PARAMETER_USER_ID = "userId"
        const val PARAMETER_ORDER_ID = "orderId"
    }
}