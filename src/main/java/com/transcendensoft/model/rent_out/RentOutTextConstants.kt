package com.transcendensoft.model.rent_out

import com.transcendensoft.model.rent.RentBotCommons

interface RentOutTextConstants {
    companion object {
        // Post strings
        // Hashtag
        const val HASHTAG_RENT = "#сниму_kvartirHUB"

        // Post creation
        const val PLACE = ":metro: Район:"
        const val PRICE = ":moneybag: Бюджет:"
        const val DATE = ":date: Ориентировочная дата:"
        const val PHONE = ":telephone: Телефон:"
        const val TELEGRAM = ":v: Телеграм:"
        const val RENT = ":house: Сниму"
        const val FACILITIES = "Чего хочу:"
        const val ABOUT_RENTER = "О себе: "
        const val I_AM = ":bust_in_silhouette: Меня зовут"

        // Navigation and entering data
        const val START = "У тебя получилось!:blush: Теперь давай красиво оформим твое предложение об аренде апартаментов.\n" +
                "Используй команду: /${RentOutBotCommons.COMMAND_CREATE_POST}\n" +
                "Список всех команд: /${RentOutBotCommons.COMMAND_HELP}"
        const val CANCELLED = "Создание текущей публикации отменено. Для создании новой используйте команду /${RentOutBotCommons.COMMAND_CREATE_POST}"
        const val ERROR_CANCEL = ":scream:Вы не можете отменить публикацию, так как вы не начали ее создавать." +
                "\nКоманда доступна только после того, как вы начнете писать предложение (/${RentBotCommons.COMMAND_CREATE_POST})"
        const val CREATE_POST_TEXT = "Просто следуй инструкциям и бот сгенерирует отличнейшее предложение об аренде апартаментов :thumbsup:\n\n" +
                "Шанс найти хорошое жилье выше, когда четко прописаны требования :wink:\n\n" +
                "Если вы отправили ошибочное предложение, отмените создание публикации с помощью команды " +
                "/${RentOutBotCommons.COMMAND_CANCEL}."
        const val ENTER_NAME = "Как вас зовут?"
        const val ENTER_REQUIRED_DATE = "Введите до когда вам нужно снять жилье.\n\n" +
                "Пример 1: Срочно.\n" +
                "Пример 2: До 1 июля.\n" +
                "Пример 3: Неспешно."
        const val ENTER_REQUIRED_LOCATION = ":world_map: Укажите районы города, в котрых вы предпочитаете жить." +
                " Можете также указать к каким станциям метро хотите жить ближе.\n\n" +
                "<b>Пример:</b> станции метро - Левобережная (Русановка), Льва Толстого, Университет," +
                " Олимпийская, Золотые ворота, Кловская, Арсенальная, Дворец спорта. Подходит район возле ТРЦ Украина."
        const val ENTER_APARTMENT_TYPE = "Выберите тип апартаментов ниже :point_down:"
        const val ENTER_ROOMS_COUNT = "Выберите количество комнат ниже"
        const val ENTER_REQUIRED_PRICE = "Введите бюджет, который можете позволить для месячной арендной платы.:moneybag:\n" +
                "Вы можете указать как диапазон (4000 - 5000 грн), так и конкретное число (5000 грн).\n" +
                ":exclamation: Важно: укажите <b>валюту</b>, в которой будет проводится оплата\n\n" +
                "Пример: 7-8 тыс грн. + к.у"

        const val ENTER_REQUIRED_FACILITIES = "Укажите какие удобства вы ожидаете от жилья.\n\n" +
                "Пример: бойлер, кондиционер, интернет, холодильник, тихие соседи, стиральная машина и т.д."
        const val ENTER_ABOUT_RENTER = "Опишите себя как личность. Помните, арендодатели ищут адекватных и платежеспособных съемщиков.\n" +
                "Также, думаю вам стоит указать с кем вы хотите снимать жилье, есть ли вредные привычки, животные, дети.\n\n" +
                "<b>Пример:</b> я студент, ІТшник. Проблем с оплатой не будет. " +
                "Не курю, редко выпиваю с друзьями, занимаюсь спортом. Без детей и животных. Жить буду сам."

        const val ENTER_PHONE = "Введите ваш номер телефона"
        const val LOAD_PHOTO_QUESTION = "Текстовая публикация создана. Вы хотите загрузить ваше фото или инфографику" +
                " сгенерированную с помощью портала <a href=\"https://sosedi.lun.ua/\">Соседи ЛУН</a>?" +
                "\n\nШанс на то, чтобы снять жилье выше, если вы добавите фото :camera:"
        const val LOAD_PHOTO = ":clap: Замечательно! Теперь загрузите ваше фото или инфографику." +
                " Загрузите их как фото, а не как файл, пожалуйста."
        const val FINISH = "Отлично! Вы успешно создали публикацию об аренде апартаментов.:thumbsup: :thumbsup: :thumbsup:\n" +
                "<b>Мы опубликуем ваше предложение в скором времени после модерации.</b>" +
                "\n\nДля создания еще одной публикации используйте команду /${RentOutBotCommons.COMMAND_CREATE_POST}\n" +
                "Помощь в использовании бота: /${RentOutBotCommons.COMMAND_HELP}\n\n"
        const val ERROR_SEND_PHOTO = "Загрузите фото пожалуйста как альбом. Не нужно вводить что-то другое на данном этапе."
        const val POST_PREVIEW = "Ваша публикация выглядит так :point_down:"
        const val SURE_TO_PUBLISH = "Пересмотрите вашу публикацию и проверьте ее на корректность данных.\n\n" +
                "Если все хорошо - жмите на <b>Опубликовать</b>.\n" +
                "Если вам нужно что-то изменить - жмите на <b>Отмена</b>," +
                " после чего создайте новую публикацию с помощью команды /${RentBotCommons.COMMAND_CREATE_POST}"
        const val PUBLISH_CANCELLED = "Публикация отменена"

        // Helper
        const val GOOD = "Отлично"
        const val COOL = ":clap: Замечательно!"
        const val AWESOME = "Превосходно!"
        const val ALMOST_DONE = "Вы почти создали публикацию!"
        const val LAST_STEP = "Фуух. Остался последний шаг!"
    }
}