package com.cheesejuice.fancymansion.model

data class Book(val config: Config, val slides: ArrayList<Slide>)
// 1
data class Config(val id: Long, val version: Long, val updateDate: Long, val publish: Long, val title: String, val writer: String, val illustrator: String, val description: String, val defaultImage: String, val startId: Long)

// 2 <Slide>
data class Slide(val id: Long, val slideImage: String, val title: String, val description: String, var count: Int, val question: String, val choiceItems: ArrayList<ChoiceItem>)

// 2.2 <ChoiceItem>
data class ChoiceItem(val id: Long, val title: String, val showConditions: ArrayList<ShowCondition>, val enterItems: ArrayList<EnterItem>)

// 2.2.2 <ShowCondition>
data class ShowCondition(val id: Long, val conditionId: Long, val conditionCount: Int, val conditionOp: String)

// 2.2.3 <EnterItem>
data class EnterItem(val id: Long, val enterSlideId: Long, val enterConditions: ArrayList<EnterCondition>)

// 2.2.3.2 <EnterCondition>
data class EnterCondition(val id: Long, val conditionId: Long, val conditionCount: Int, val conditionOp: String)