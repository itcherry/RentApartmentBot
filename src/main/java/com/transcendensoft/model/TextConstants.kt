package com.transcendensoft.model

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
        const val PHOTO_OF_APARTMENTS = "Фото апартаментов там :point_down:"
        const val APARTMENTS_FREE = ":white_check_mark: Апартаменты еще свободны"
        const val APARTMENTS_RENTED = ":red_circle: Апартаменты сданы"

        // Navigation and entering data
        const val START = "У тебя получилось!:blush: Теперь давай красиво оформим твое предложение о сдачи апартаментов.\n" +
                "Используй команду: /${BotCommons.COMMAND_CREATE_POST}"
        const val CREATE_POST_TEXT = "Просто следуй инструкциям и бот сгенерирует прекрасное предложение о сдачи апартаментов :thumbsup:\n\n" +
                "Хорошие съемщики сразу видят когда четко определены условия жилья :wink:"
        const val ENTER_NAME = "Как вас зовут?"
        const val ENTER_ADDRESS = ":world_map: Укажите точный адресс, где находятся ваши замечательные апартаменты"
        const val ENTER_APARTMENT_TYPE = "Выберите тип апартаментов ниже :point_down:"
        const val ENTER_ROOMS_COUNT = "Выберите количество комнат ниже"
        const val ENTER_PRICE = "Введите месячную арендную стоимость апартаментов.:moneybag:\n" +
                "Также, укажите залоговую сумму.\n" +
                "Не забудьте упомянуть включены ли комунальные услуги.\n\n" +
                "Пример: 6000 грн. + к.у. Залог 6000 грн."
        const val ENTER_SQUARE = "Введите площадь апартаментов, только число (в кв.м)\n\nНапример 45"
        const val ERROR_ENTER_SQUARE = "Вы ввели неправильное значение:pensive: Введите только число, без слов и букв. Например 45"
        const val ENTER_FACILITIES = "Укажите какие удобства имеются в ваших апартаментах.\n\n" +
                "Пример: бойлер, кондиционер, интернет Воля с Wi-Fi роутером, 2 раздельных санузла"
        const val ENTER_COMMENT = "Опишите кратко апартаменты. " +
                "Чем они отличаются от других, что в них особенного, как далеко от метро," +
                " есть ли магазины или же парки рядом и т.д."
        const val ENTER_MASTER = "Выберите из списка кем вы являетесь."
        const val ENTER_PHONE = "Введите ваш номер телефона"
        const val LOAD_PHOTO_QUESTION = "Текстовая публикация создана. Вы хотите загрузить фото ваших апартаментов?" +
                "\n\nШанс на то, что у вас снимут квартиру выше, если вы добавите фото :camera:"
        const val LOAD_PHOTO = ":clap: Замечательно! Теперь загрузите фото ваших апартаментов. Загрузите их как фото, а не как файл, пожалуйста."
        const val FINISH = "Отлично! Вы успешно создали публикацию о сдаче апартаментов.:thumbsup::thumbsup::thumbsup:\n" +
                "Мы опубликуем ваш пост в скором времени после модерации."
        const val ERROR_SEND_PHOTO = "Загрузите фото пожалуйста. Не нужно вводить что-то другое на данном этапе."

        // Helper
        const val GOOD = "Отлично"
        const val COOL = ":clap: Замечательно!"
        const val AWESOME = "Превосходно!"
        const val ALMOST_DONE = "Вы почти создали публикацию!"
        const val LAST_STEP = "Фуух. Остался последний шаг!"
    }
}