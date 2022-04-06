package com.cheesejuice.fancymansion.model

import com.cheesejuice.fancymansion.Const.Companion.END_SLIDE_ID
import kotlinx.serialization.Serializable

@Serializable
data class Book(var config: Config, var logic: Logic, var slides: MutableList<Slide>)

// (1)
@Serializable
data class Config(var bookId: Long, var version: Long = 0L, var updateTime: Long = System.currentTimeMillis(), var publishId: Long = 0, var title: String, var writer: String = "", var illustrator: String = "", var description: String = "", var coverImage: String = "", var defaultEndId: Long = END_SLIDE_ID, var readMode: String = "edit")

// (2)
@Serializable
data class Logic(var bookId: Long, var logics: MutableList<SlideLogic> = mutableListOf())

// (2.2)
@Serializable
data class SlideLogic(var slideId: Long, var slideTitle: String, var choiceItems: MutableList<ChoiceItem> = mutableListOf())

// (2.2.3)
@Serializable
data class ChoiceItem(var id: Long, var title: String, var showConditions: MutableList<Condition> = mutableListOf(), var enterItems: MutableList<EnterItem> = mutableListOf())

// (2.2.3.4)
@Serializable
data class EnterItem(var id: Long, var enterSlideId: Long, var enterConditions: MutableList<Condition>)

// (2.2.3.3) / (2.2.3.4.3)
@Serializable
data class Condition(var id: Long, var conditionId1: Long, var conditionId2: Long, var conditionCount: Int, var conditionOp: String, var conditionNext: String)

// (3)
@Serializable
data class Slide(var slideId: Long, var slideTitle: String, var slideImage: String = "", var description: String = "", var question: String)
