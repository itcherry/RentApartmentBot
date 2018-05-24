package com.transcendensoft.model

import com.transcendensoft.model.BotCommons.Companion.COMMAND_CANCEL
import com.transcendensoft.model.BotCommons.Companion.COMMAND_CREATE_POST
import com.transcendensoft.model.BotCommons.Companion.COMMAND_HELP
import com.vdurmont.emoji.EmojiParser

interface TextConstants {
    companion object {
        // Hashtags
        const val HASHTAG_RENT = "#сдам_kvartirHUB"
        const val HASHTAG_RENT_OUT = "#сниму_kvartirHUB"

        // Post creation
        const val PRICE = ":moneybag: Цена:"
        const val SQUARE = ":footprints: Площадь:"
        const val PHONE = ":telephone: Телефон:"
        const val TELEGRAM = ":v: Телеграм:"
        const val RENT = ":house_with_garden: Сдам"
        const val FACILITIES = "Удобства апартаментов:"
        const val I_AM = ":bust_in_silhouette: Меня зовут"
        const val PHOTO_OF_APARTMENTS = "<b>>>>>>>>>></b> Фото апартаментов там :point_down:"
        const val APARTMENTS_FREE = ":white_check_mark: Апартаменты еще свободны"
        const val APARTMENTS_RENTED = ":red_circle: Апартаменты сданы"
        const val WHO_AM_I = ":male_office_worker: Кто я:"


        // Navigation and entering data
        const val START = "У тебя получилось!:blush: Теперь давай красиво оформим твое предложение о сдачи апартаментов.\n" +
                "Используй команду: /${BotCommons.COMMAND_CREATE_POST}\n" +
                "Список всех команд: /${BotCommons.COMMAND_HELP}"
        const val CANCELLED = "Создание текущей публикации отменено. Для создании новой используйте команду /$COMMAND_CREATE_POST"
        const val ERROR_CANCEL = ":scream:Вы не можете отменить публикацию, так как вы не начали ее создавать." +
                "\nКоманда доступна только после того, как вы начнете писать предложение (/$COMMAND_CREATE_POST)"
        const val CREATE_POST_TEXT = "Просто следуй инструкциям и бот сгенерирует прекрасное предложение о сдачи апартаментов :thumbsup:\n\n" +
                "Хорошие съемщики сразу видят когда четко определены условия жилья :wink:\n\n" +
                "Если вы отправили ошибочное предложение, отмените создание публикации с помощью команды " +
                "/$COMMAND_CANCEL."
        const val ENTER_NAME = "Как вас зовут?"
        const val ENTER_ADDRESS = ":world_map: Укажите точный адресс, где находятся ваши замечательные апартаменты"
        const val ENTER_APARTMENT_TYPE = "Выберите тип апартаментов ниже :point_down:"
        const val ENTER_ROOMS_COUNT = "Выберите количество комнат ниже"
        const val ENTER_PRICE = "Введите месячную арендную стоимость апартаментов.:moneybag:\n" +
                "Также, <b>укажите залоговую сумму</b> и не забудьте упомянуть включены ли <b>комунальные услуги</b>.\n" +
                ":exclamation: Важно: укажите <b>валюту</b>, в которой будет проводится оплата\n\n" +
                "Пример: 6000 грн. + к.у. Залог 6000 грн."
        const val ENTER_SQUARE = "Введите площадь апартаментов, только число (в кв.м)\n\nНапример 45"
        const val ERROR_ENTER_SQUARE = "Вы ввели неправильное значение:pensive: " +
                "Введите только число, без слов и букв. Например 45"
        const val ENTER_FACILITIES = "Укажите какие удобства имеются в ваших апартаментах.\n\n" +
                "Пример: бойлер, кондиционер, интернет Воля с Wi-Fi роутером, 2 раздельных санузла"
        const val ENTER_COMMENT = "Опишите кратко апартаменты. " +
                "Чем они отличаются от других, что в них особенного, как далеко от метро," +
                " есть ли магазины или же парки рядом и т.д."
        const val ENTER_MASTER = "Выберите из списка кем вы являетесь."
        const val ENTER_PHONE = "Введите ваш номер телефона"
        const val LOAD_PHOTO_QUESTION = "Текстовая публикация создана. Вы хотите загрузить фото ваших апартаментов?" +
                "\n\nШанс на то, что у вас снимут квартиру выше, если вы добавите фото :camera:"
        const val LOAD_PHOTO = ":clap: Замечательно! Теперь загрузите фото ваших апартаментов." +
                " Загрузите их как фото, а не как файл, пожалуйста."
        const val FINISH = "Отлично! Вы успешно создали публикацию о сдаче апартаментов.:thumbsup: :thumbsup: :thumbsup:\n" +
                "<b>Мы опубликуем ваше предложение в скором времени после модерации.</b>" +
                "\n\nДля создания еще одной публикации используйте команду /$COMMAND_CREATE_POST\n" +
                "Помощь в использовании бота: /$COMMAND_HELP\n\n"
        const val ERROR_SEND_PHOTO = "Загрузите фото пожалуйста как альбом. Не нужно вводить что-то другое на данном этапе."
        const val POST_PREVIEW = "Ваша публикация выглядит так :point_down:"
        const val SURE_TO_PUBLISH = "Пересмотрите вашу публикацию и проверьте ее на корректность данных.\n\n" +
                "Если все хорошо - жмите на <b>Опубликовать</b>.\n" +
                "Если вам нужно что-то изменить - жмите на <b>Отмена</b>," +
                " после чего создайте новую публикацию с помощью команды /$COMMAND_CREATE_POST"
        const val PUBLISH_CANCELLED = "Публикация отменена"
        const val ERROR_RENTED_COMMAND = "У вас нет опубликованых обьявлений. " +
                "Создайте новый пост с помощью команды /$COMMAND_CREATE_POST"
        const val APARTMENT_STATE_CHANGED = ":thumbsup: Состояние успешно изменено. " +
                "Вы можете убедится в этом, посмотрите на публикацию в @ApartmentHub.\n\n" +
                "Список команд смотреть сдесь: /$COMMAND_HELP"
        // Helper
        const val GOOD = "Отлично"
        const val COOL = ":clap: Замечательно!"
        const val AWESOME = "Превосходно!"
        const val ALMOST_DONE = "Вы почти создали публикацию!"
        const val LAST_STEP = "Фуух. Остался последний шаг!"
    }
}