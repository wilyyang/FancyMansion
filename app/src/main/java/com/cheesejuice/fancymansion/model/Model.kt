package com.cheesejuice.fancymansion.model

import com.cheesejuice.fancymansion.CondNext
import com.cheesejuice.fancymansion.CondOp
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.Const.Companion.END_SLIDE_ID
import kotlinx.serialization.Serializable

@Serializable
data class Book(val config: Config, var logic: Logic, var slides: MutableList<Slide>)

// (1)
@Serializable
data class Config(val bookId: Long, var version: Long = 0L, var updateTime: Long = System.currentTimeMillis(), var publishCode: String = "", var title: String, var writer: String = "", var illustrator: String = "", var description: String = "", var coverImage: String = "", var defaultEndId: Long = END_SLIDE_ID, var readMode: String = "edit", var tagList: MutableList<String> = mutableListOf())

// (2)
@Serializable
data class Logic(val bookId: Long, var logics: MutableList<SlideLogic> = mutableListOf())

// (2.2)
@Serializable
data class SlideLogic(val slideId: Long, var slideTitle: String, var choiceItems: MutableList<ChoiceItem> = mutableListOf())

// (2.2.3)
@Serializable
data class ChoiceItem(val id: Long, var title: String, var showConditions: MutableList<Condition> = mutableListOf(), var enterItems: MutableList<EnterItem> = mutableListOf())

// (2.2.3.4)
@Serializable
data class EnterItem(val id: Long, var enterSlideId: Long = Const.ID_NOT_FOUND, var enterConditions: MutableList<Condition> = mutableListOf())

// (2.2.3.3) / (2.2.3.4.3)
@Serializable
data class Condition(val id: Long, var conditionId1: Long = Const.ID_NOT_FOUND, var conditionId2: Long = Const.ID_NOT_FOUND, var conditionCount: Int = 0, var conditionOp: String = CondOp.ALL.opName, var conditionNext: String = CondNext.OR.relName)

// (3)
@Serializable
data class Slide(val slideId: Long, var slideTitle: String, var slideImage: String = "", var description: String = "", var question: String)
