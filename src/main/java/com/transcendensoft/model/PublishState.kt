package com.transcendensoft.model

enum class PublishState(val text: String, val callbackData: String) {
    PUBLISH("Опубликовать", "callbackPublish"),
    CANCEL("Отмена", "callbackCancel")
}