package com.cheesejuice.fancymansion.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(var id:String = "", var uid: String = "", var email: String = "", var userName: String = "", var photoUrl:String = "", var uploadBookTime:Long = 0L, var addCommentTime:Long = 0L, var uploadBookIds: MutableList<String> = mutableListOf())

@Serializable
data class Comment(var id:String = "", var uid: String = "", var email: String = "", var userName: String = "", var photoUrl:String = "", var comment: String = "", var updateTime: Long = 0L, var bookPublishCode: String = "", var editCount: Int = 0, var editTime:Long = 0L)