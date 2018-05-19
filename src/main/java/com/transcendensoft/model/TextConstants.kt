package com.transcendensoft.model

import com.vdurmont.emoji.EmojiParser

interface TextConstants {
    companion object {
        // Hashtags
        const val HASHTAG_RENT = "#сдам_kvartirHUB"
        const val HASHTAG_RENT_OUT = "#сниму_kvartirHUB"

        // Post creation
        const val PRICE = "Цена: "
        const val SQUARE = "Площадь: "
        const val PHONE = "Телефон: "
        const val TELEGRAM = "Телеграм: "
        const val RENT = "Сдам "
        const val FACILITIES = "Удобства: "
        const val NAME = "Имя арендодателя: "
        const val I_AM = "Я"

        // Navigation and entering data
        const val START = "У тебя получилось!:smile: Теперь давай красиво оформим твое предложение о сдачи апартаментов.\n" +
                "Следуй инструкциям и у тебя получится прекрасное предложение о сдачи.\n" +
                "Хорошие съемщики сразу видят когда четко видно условия."
        const val ENTER_NAME = "Как вас зовут?"
        const val ENTER_ADDRESS = "Укажите точный адресс, где находятся ваши замечательные апартаменты"
        const val ENTER_APARTMENT_TYPE = "Выберите тип апартаментов ниже"
        const val ENTER_ROOMS_COUNT = "Выберите количество комнат ниже"
        const val ENTER_PRICE = "Введите месячную арендную стоимость апартаментов. \n" +
                " Также, укажите залоговую сумму.\n" +
                " Не забудьте упомянуть включены ли комунальные услуги.\n" +
                " Пример: 6000 грн. + к.у. Залог 6000 грн."
        const val ENTER_SQUARE = "Введите площадь апартаментов (в кв.м)"
        const val ENTER_FACILITIES = "Укажите какие удобства имеются в ваших апартаментах.\n" +
                "Пример: бойлер, кондиционер, интернет Воля с Wi-Fi роутером, 2 раздельных санузла"
        const val ENTER_COMMENT = "Опишите кратко апартаменты. " +
                "Чем они отличаются от других, что в них особенного, как далеко от метро," +
                " есть ли магазины или же парки рядом и т.д."
        const val ENTER_MASTER = "Выберите из списка кем вы являетесь."
        const val ENTER_PHONE = "Введите ваш номер телефона"

        // Helper
        const val GOOD = "Отлично"
        const val COOL = "Замечательно!"
        const val AWESOME = "Превосходно!"
        const val ALMOST_DONE = "Вы почти сделали пост!"
        const val LAST_STEP = "Фуух. Остался последний шаг!"
        const val LOAD_PHOTO = "Текстовый пост создан. Теперь можете загрузить фото ваших апартаментов.\n" +
                "Обязательно загружайте их как фото, а не как файл"
        const val FINISH = "Отлично! Вы успешно создали пост о сдаче апартаментов.\n" +
                "Мы опубликуем пост в скором времени после модерации."
    }
}