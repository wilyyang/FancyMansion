package com.cheesejuice.fancymansion.util

import android.util.Log
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.R.*
import com.cheesejuice.fancymansion.model.Book
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.model.Slide
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class Sample {
    companion object {
        // 00 / 00 / 00 / 00 / 00 = slide / choice / showCondition / enterId / enterCondition
        fun extractConfigFromJson(bookId: Long): Config?{
            val configJson = Sample.getSampleConfig(bookId)
            var result: Config? = null
            try{
                result = Json.decodeFromString<Config>(configJson)

            }catch (e : Exception){
                Log.e(Const.TAG, "Exception : "+e.printStackTrace())
                return null
            }
            return result
        }

        fun extractSlideFromJson(bookId: Long, slideId: Long): Slide?{
            val slideJson = Sample.getSlideJson(bookId, slideId)
            var result: Slide? = null
            try{
                result = Json.decodeFromString<Slide>(slideJson)

            }catch (e : Exception){
                Log.e(Const.TAG, "Exception : "+e.printStackTrace())
                return null
            }
            return result
        }

        fun extractBookFromJson(bookId: Long): Book?{
            val bookJson = Sample.getSampleJson()
            var result: Book? = null
            try{
                result = Json.decodeFromString<Book>(bookJson)

            }catch (e : Exception){
                Log.e(Const.TAG, "Exception : "+e.message)
                return null
            }
            return result
        }

        fun getSampleConfig(id: Long): String = """
        { 
            "id":12345,
            "version":101001,
            "updateDate":234256544566,
            "publish":0,
            "title":"나의 고양이 찾기",
            "writer":"팀 치즈쥬스",
            "illustrator":"무료 이미지",
            "description":"나의 고양이 존 크리스탈을 찾아주세요.. \n누군가 데려간걸까요? \n크리스탈의 친구들을 유심히 살펴보면 단서를 얻을지도 모르죠..",
            "defaultImage":"image_1.gif",
            "startId":200000000,
            "defaultEndId":100000000,
            "readMode":"edit",
            "briefs":[
                { "slideId":100000000, "slideTitle":"더 이상 존 크리스탈을 찾을 수 없습니다..." },
                { "slideId":200000000, "slideTitle":"대체 어딨는거야!" },
                { "slideId":300000000, "slideTitle":"하얀 고양이 모야!" },
                { "slideId":400000000, "slideTitle":"모야가 가져다준 물고기!" },
                { "slideId":500000000, "slideTitle":"응꼬를 핥으려는 고양이 웬디!" },
                { "slideId":600000000, "slideTitle":"빙글빙글 상자속에서 돌고도는 너와 나" },
                { "slideId":700000000, "slideTitle":"응꼬를 핥으려는 고양이 웬디!" },
                { "slideId":800000000, "slideTitle":"그 녀석은 용서할 수 없는 녀석이야.." },
                { "slideId":900000000, "slideTitle":"아니 이 녀석 집에 있었잖아?" }
            ]
        }
        """.trimIndent()

        fun getSampleImageId(image: String): Int = when (image) {
            "image_1.gif" -> raw.image_1
            "image_2.gif" -> raw.image_2
            "image_3.gif" -> raw.image_3
            "image_4.gif" -> raw.image_4
            "image_5.gif" -> raw.image_5
            "image_6.gif" -> raw.image_6
            "fish_cat.jpg" -> raw.fish_cat
            "game_end.jpg" -> raw.game_end
            else -> raw.game_end
        }

        fun getSlideJson(bookId: Long, slideId: Long): String =
            when(slideId){
                100000000L ->
                    """
                    {
                      "id":100000000,
                      "slideImage":"game_end.jpg",
                      "title":"더 이상 존 크리스탈을 찾을 수 없습니다...",
                      "description":"대체 무슨 일을 해버린거죠?\n존 크리스탈은 이제 찾을 수 없습니다..\n처음부터 다시 시작하세요...\n",
                      "count":0,
                      "question":"",
                      "choiceItems":[]
                    } 
                    """.trimIndent()
                200000000L ->
                    """
                    {
                      "id":200000000,
                      "slideImage":"image_1.gif",
                      "title":"대체 어딨는거야!",
                      "description":"대체 어딨는거야... 나의 고양이 존 크리스탈...\n사라져버린 존을 누군가 데려간걸까요?\n평소 존과 친했던 고양이 모야와 웬디에게로 가봅시다.",
                      "count":0,
                      "question":"어떤 고양이를 찾아갈까요?",
                      "choiceItems":[
                        {
                          "id":201000000,
                          "title":"하얀 고양이 모야에게로",
                          "showConditions":[
                            {
                              "id":201010000,
                              "conditionId1":400000000,
                              "conditionId2":-1,
                              "conditionCount":1,
                              "conditionOp":"not",
                              "nextLogic":"or"
                            }
                          ],
                          "enterItems":[
                            {
                              "id":201000100,
                              "enterSlideId":300000000,
                              "enterConditions":[]
                            }
                          ]
                        },
                        {
                          "id":202000000,
                          "title":"얼룩 고양이 웬디에게로",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":202000100,
                              "enterSlideId":600000000,
                              "enterConditions":[
                                {
                                  "id":202000101,
                                  "conditionId1":600000000,
                                  "conditionId2":-1,
                                  "conditionCount":0,
                                  "conditionOp":"over",
                                  "nextLogic":"or"
                                }
                              ]
                            },
                            {
                              "id":202000200,
                              "enterSlideId":700000000,
                              "enterConditions":[
                                {
                                  "id":202000201,
                                  "conditionId1":400000000,
                                  "conditionId2":-1,
                                  "conditionCount":0,
                                  "conditionOp":"over",
                                  "nextLogic":"or"
                                }
                              ]
                            },
                            {
                              "id":202000300,
                              "enterSlideId":500000000,
                              "enterConditions":[]
                            }
                          ]
                        }
                      ]
                    }
                    """.trimIndent()
                300000000L ->
                    """
                    {
                      "id":300000000,
                      "slideImage":"image_6.gif",
                      "title":"하얀 고양이 모야!",
                      "description":"\"모야? 모야? 모야아?\"\n하얀 고양이 모야가 물고기를 바라보며 궁금해하네요?\n존의 행방을 물어보기가 곤란하군요?\n하지만 원하는걸 얻기위해선 공손하게 행동해야 합니다.",
                      "count":0,
                      "question":"무엇을 할까요?",
                      "choiceItems":[
                        {
                          "id":301000000,
                          "title":"모야의 머리에서 어항을 빼버린다",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":301000100,
                              "enterSlideId":400000000,
                              "enterConditions":[
                                {
                                  "id":301000101,
                                  "conditionId1":302000000,
                                  "conditionId2":-1,
                                  "conditionCount":3,
                                  "conditionOp":"equal",
                                  "nextLogic":"or"
                                }
                              ]
                            },
                            {
                              "id":301000200,
                              "enterSlideId":100000000,
                              "enterConditions":[]
                            }
                          ]
                        },
                        {
                          "id":302000000,
                          "title":"모야와 함께 물고기를 바라본다",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":302000100,
                              "enterSlideId":100000000,
                              "enterConditions":[
                                {
                                  "id":302000101,
                                  "conditionId1":302000000,
                                  "conditionId2":-1,
                                  "conditionCount":5,
                                  "conditionOp":"over",
                                  "nextLogic":"or"
                                }
                              ]
                            },
                            {
                              "id":302000200,
                              "enterSlideId":300000000,
                              "enterConditions":[]
                            }
                          ]
                        },
                        {
                          "id":303000000,
                          "title":"다시 존을 잃어버린 자리로 돌아간다",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":303000300,
                              "enterSlideId":200000000,
                              "enterConditions":[]
                            }
                          ]
                        }
                      ]
                    }
                    """.trimIndent()
                400000000L ->
                    """
                    {
                      "id":400000000,
                      "slideImage":"fish_cat.jpg",
                      "title":"모야가 가져다준 물고기!",
                      "description":"모야가 당신에게 물고기를 가져다주는군요?\n\"이 물고기가 필요하다구용?\n확실히.. 배가 고픈 고양이에겐 필요할지도?\n그럼 난 이만 ~\"\n당신은 들고있는 모든 물건을 내려놓고 공손히 물고기를 받았습니다.",
                      "count":0,
                      "question":"무엇을 할까요?",
                      "choiceItems":[
                        {
                          "id":401000000,
                          "title":"다시 존을 잃어버린 자리로 돌아간다",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":401000100,
                              "enterSlideId":200000000,
                              "enterConditions":[]
                            }
                          ]
                        }
                      ]
                    }
                    """.trimIndent()
                500000000L ->
                    """
                    {
                      "id":500000000,
                      "slideImage":"image_3.gif",
                      "title":"응꼬를 핥으려는 고양이 웬디!",
                      "description":"너무 배가 고파서... 응꼬를 핥고 있어요...\n\"캬! 아냐! 난 단지 그루밍을 하고 있을 뿐이라고!\"\n웬디가 화가 났군요?\n마침 당신은 박스를 들고 있습니다.\n일설에 고양이는 박스를 아주 좋아한다고 하지요?\n",
                      "count":0,
                      "question":"무엇을 할까요?",
                      "choiceItems":[
                        {
                          "id":501000000,
                          "title":"박스를 가져다 준다",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":501000100,
                              "enterSlideId":600000000,
                              "enterConditions":[]
                            }
                          ]
                        },
                        {
                          "id":502000000,
                          "title":"다시 존을 잃어버린 자리로 돌아간다",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":502000100,
                              "enterSlideId":200000000,
                              "enterConditions":[]
                            }
                          ]
                        }
                      ]
                    } 
                    """.trimIndent()
                600000000L ->
                    """
                    {
                      "id":600000000,
                      "slideImage":"image_5.gif",
                      "title":"빙글빙글 상자속에서 돌고도는 너와 나",
                      "description":"\"우리 만남은 빙글빙글 돌고\"\n웬디와 함께 빙글빙글 돌아볼까요?\n\"상자도 좋은데 빙글빙글 도는 상자라니!\"",
                      "count":0,
                      "question":"무엇을 할까요?",
                      "choiceItems":[
                        {
                          "id":601000000,
                          "title":"빙글빙글 웬디와 함께 돈다",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":601000100,
                              "enterSlideId":600000000,
                              "enterConditions":[]
                            }
                          ]
                        },
                        {
                          "id":602000000,
                          "title":"기분이 좋아진 웬디에게 물고기를 주고 존의 행방을 묻는다",
                          "showConditions":[
                            {
                              "id":602010000,
                              "conditionId1":602000000,
                              "conditionId2":-1,
                              "conditionCount":1,
                              "conditionOp":"under",
                              "nextLogic":"and"
                            },
                            {
                              "id":602020000,
                              "conditionId1":400000000,
                              "conditionId2":-1,
                              "conditionCount":0,
                              "conditionOp":"over",
                              "nextLogic":"or"
                            }
                          ],
                          "enterItems":[
                            {
                              "id":602000100,
                              "enterSlideId":800000000,
                              "enterConditions":[]
                            }
                          ]
                        },
                        {
                          "id":603000000,
                          "title":"다시 존을 잃어버린 자리로 돌아간다",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":603000100,
                              "enterSlideId":200000000,
                              "enterConditions":[]
                            }
                          ]
                        }
                      ]
                    }
                    """.trimIndent()
                700000000L ->
                    """
                    {
                      "id":700000000,
                      "slideImage":"image_3.gif",
                      "title":"응꼬를 핥으려는 고양이 웬디!",
                      "description":"너무 배가 고파서... 응꼬를 핥고 있어요...\n\"캬! 아냐! 난 단지 그루밍을 하고 있을 뿐이라고!\"\n웬디가 화가 났군요?\n",
                      "count":0,
                      "question":"무엇을 할까요?",
                      "choiceItems":[
                        {
                          "id":701000000,
                          "title":"다시 존을 잃어버린 자리로 돌아간다",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":701000100,
                              "enterSlideId":200000000,
                              "enterConditions":[]
                            }
                          ]
                        }
                      ]
                    }
                    """.trimIndent()
                800000000L ->
                    """
                    {
                      "id":800000000,
                      "slideImage":"image_2.gif",
                      "title":"그 녀석은 용서할 수 없는 녀석이야..",
                      "description":"\"존... 그녀석은 내 먹이를 다 먹어 버렸지.. 괘씸한 녀석\"\n이런.. 존이 웬디의 밥을 모두 먹어버렸군요?\n\"물고기도 다 먹었으니 다시 빙글빙글 돌아볼까?\"\n웬디의 표정이 어쩐지 슬퍼보이는군요...",
                      "count":0,
                      "question":"돌아줄래요?",
                      "choiceItems":[
                        {
                          "id":801000000,
                          "title":"빙글빙글 돌아준다",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":801000100,
                              "enterSlideId":600000000,
                              "enterConditions":[]
                            }
                          ]
                        },
                        {
                          "id":802000000,
                          "title":"빙글빙글 안돌아준다",
                          "showConditions":[],
                          "enterItems":[
                            {
                              "id":802000100,
                              "enterSlideId":900000000,
                              "enterConditions":[]
                            }
                          ]
                        }
                      ]
                    }
                    """.trimIndent()
                900000000L ->
                    """
                    {
                      "id":900000000,
                      "slideImage":"image_4.gif",
                      "title":"아니 이 녀석 집에 있었잖아?",
                      "description":"이럴수가! 그렇게 찾았던 존이 언제 돌아와 집에서 자고 있었을까요?\n후.. 걱정도 많이 했지만 귀여우니까 봐준다.\n존 크리스탈.. 오늘도 수고했어..",
                      "count":0,
                      "question":"",
                      "choiceItems":[]
                    } 
                    """.trimIndent()
                else  ->
                    """ 
                    """.trimIndent()
            }


        fun getSampleJson(): String = """
        { 
          "config":
          {
            "id":12345,
            "version":101001,
            "updateDate":234256544566,
            "publish":0,
            "title":"나의 고양이 찾기",
            "writer":"팀 치즈쥬스",
            "illustrator":"무료 이미지",
            "description":"나의 고양이 존 크리스탈을 찾아주세요.. \n누군가 데려간걸까요? \n크리스탈의 친구들을 유심히 살펴보면 단서를 얻을지도 모르죠..",
            "defaultImage":"image_1.gif",
            "startId":200000000,
            "defaultEndId":100000000,
            "readMode":"edit",
            "briefs":[
                { "slideId":100000000, "slideTitle":"더 이상 존 크리스탈을 찾을 수 없습니다..." },
                { "slideId":200000000, "slideTitle":"대체 어딨는거야!" },
                { "slideId":300000000, "slideTitle":"하얀 고양이 모야!" },
                { "slideId":400000000, "slideTitle":"모야가 가져다준 물고기!" },
                { "slideId":500000000, "slideTitle":"응꼬를 핥으려는 고양이 웬디!" },
                { "slideId":600000000, "slideTitle":"빙글빙글 상자속에서 돌고도는 너와 나" },
                { "slideId":700000000, "slideTitle":"응꼬를 핥으려는 고양이 웬디!" },
                { "slideId":800000000, "slideTitle":"그 녀석은 용서할 수 없는 녀석이야.." },
                { "slideId":900000000, "slideTitle":"아니 이 녀석 집에 있었잖아?" }
            ]
          },
          "slides":[

            {
              "id":100000000,
              "slideImage":"game_end.jpg",
              "title":"더 이상 존 크리스탈을 찾을 수 없습니다...",
              "description":"대체 무슨 일을 해버린거죠?\n존 크리스탈은 이제 찾을 수 없습니다..\n처음부터 다시 시작하세요...\n",
              "count":0,
              "question":"",
              "choiceItems":[]
            },
            {
              "id":200000000,
              "slideImage":"image_1.gif",
              "title":"대체 어딨는거야!",
              "description":"대체 어딨는거야... 나의 고양이 존 크리스탈...\n사라져버린 존을 누군가 데려간걸까요?\n평소 존과 친했던 고양이 모야와 웬디에게로 가봅시다.",
              "count":0,
              "question":"어떤 고양이를 찾아갈까요?",
              "choiceItems":[
                {
                  "id":201000000,
                  "title":"하얀 고양이 모야에게로",
                  "showConditions":[
                    {
                      "id":201010000,
                      "conditionId1":400000000,
                      "conditionId2":-1,
                      "conditionCount":1,
                      "conditionOp":"not",
                      "nextLogic":"or"
                    }
                  ],
                  "enterItems":[
                    {
                      "id":201000100,
                      "enterSlideId":300000000,
                      "enterConditions":[]
                    }
                  ]
                },
                {
                  "id":202000000,
                  "title":"얼룩 고양이 웬디에게로",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":202000100,
                      "enterSlideId":600000000,
                      "enterConditions":[
                        {
                          "id":202000101,
                          "conditionId1":600000000,
                          "conditionId2":-1,
                          "conditionCount":0,
                          "conditionOp":"over",
                          "nextLogic":"or"
                        }
                      ]
                    },
                    {
                      "id":202000200,
                      "enterSlideId":700000000,
                      "enterConditions":[
                        {
                          "id":202000201,
                          "conditionId1":400000000,
                          "conditionId2":-1,
                          "conditionCount":0,
                          "conditionOp":"over",
                          "nextLogic":"or"
                        }
                      ]
                    },
                    {
                      "id":202000300,
                      "enterSlideId":500000000,
                      "enterConditions":[]
                    }
                  ]
                }
              ]
            },
            {
              "id":300000000,
              "slideImage":"image_6.gif",
              "title":"하얀 고양이 모야!",
              "description":"\"모야? 모야? 모야아?\"\n하얀 고양이 모야가 물고기를 바라보며 궁금해하네요?\n존의 행방을 물어보기가 곤란하군요?\n하지만 원하는걸 얻기위해선 공손하게 행동해야 합니다.",
              "count":0,
              "question":"무엇을 할까요?",
              "choiceItems":[
                {
                  "id":301000000,
                  "title":"모야의 머리에서 어항을 빼버린다",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":301000100,
                      "enterSlideId":400000000,
                      "enterConditions":[
                        {
                          "id":301000101,
                          "conditionId1":302000000,
                          "conditionId2":-1,
                          "conditionCount":3,
                          "conditionOp":"equal",
                          "nextLogic":"or"
                        }
                      ]
                    },
                    {
                      "id":301000200,
                      "enterSlideId":100000000,
                      "enterConditions":[]
                    }
                  ]
                },
                {
                  "id":302000000,
                  "title":"모야와 함께 물고기를 바라본다",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":302000100,
                      "enterSlideId":100000000,
                      "enterConditions":[
                        {
                          "id":302000101,
                          "conditionId1":302000000,
                          "conditionId2":-1,
                          "conditionCount":5,
                          "conditionOp":"over",
                          "nextLogic":"or"
                        }
                      ]
                    },
                    {
                      "id":302000200,
                      "enterSlideId":300000000,
                      "enterConditions":[]
                    }
                  ]
                },
                {
                  "id":303000000,
                  "title":"다시 존을 잃어버린 자리로 돌아간다",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":303000300,
                      "enterSlideId":200000000,
                      "enterConditions":[]
                    }
                  ]
                }
              ]
            },
            {
              "id":400000000,
              "slideImage":"fish_cat.jpg",
              "title":"모야가 가져다준 물고기!",
              "description":"모야가 당신에게 물고기를 가져다주는군요?\n\"이 물고기가 필요하다구용?\n확실히.. 배가 고픈 고양이에겐 필요할지도?\n그럼 난 이만 ~\"\n당신은 들고있는 모든 물건을 내려놓고 공손히 물고기를 받았습니다.",
              "count":0,
              "question":"무엇을 할까요?",
              "choiceItems":[
                {
                  "id":401000000,
                  "title":"다시 존을 잃어버린 자리로 돌아간다",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":401000100,
                      "enterSlideId":200000000,
                      "enterConditions":[]
                    }
                  ]
                }
              ]
            },
            {
              "id":500000000,
              "slideImage":"image_3.gif",
              "title":"응꼬를 핥으려는 고양이 웬디!",
              "description":"너무 배가 고파서... 응꼬를 핥고 있어요...\n\"캬! 아냐! 난 단지 그루밍을 하고 있을 뿐이라고!\"\n웬디가 화가 났군요?\n마침 당신은 박스를 들고 있습니다.\n일설에 고양이는 박스를 아주 좋아한다고 하지요?\n",
              "count":0,
              "question":"무엇을 할까요?",
              "choiceItems":[
                {
                  "id":501000000,
                  "title":"박스를 가져다 준다",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":501000100,
                      "enterSlideId":600000000,
                      "enterConditions":[]
                    }
                  ]
                },
                {
                  "id":502000000,
                  "title":"다시 존을 잃어버린 자리로 돌아간다",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":502000100,
                      "enterSlideId":200000000,
                      "enterConditions":[]
                    }
                  ]
                }
              ]
            },
            {
              "id":600000000,
              "slideImage":"image_5.gif",
              "title":"빙글빙글 상자속에서 돌고도는 너와 나",
              "description":"\"우리 만남은 빙글빙글 돌고\"\n웬디와 함께 빙글빙글 돌아볼까요?\n\"상자도 좋은데 빙글빙글 도는 상자라니!\"",
              "count":0,
              "question":"무엇을 할까요?",
              "choiceItems":[
                {
                  "id":601000000,
                  "title":"빙글빙글 웬디와 함께 돈다",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":601000100,
                      "enterSlideId":600000000,
                      "enterConditions":[]
                    }
                  ]
                },
                {
                  "id":602000000,
                  "title":"기분이 좋아진 웬디에게 물고기를 주고 존의 행방을 묻는다",
                  "showConditions":[
                    {
                      "id":602010000,
                      "conditionId1":602000000,
                      "conditionId2":-1,
                      "conditionCount":1,
                      "conditionOp":"under",
                      "nextLogic":"and"
                    },
                    {
                      "id":602020000,
                      "conditionId1":400000000,
                      "conditionId2":-1,
                      "conditionCount":0,
                      "conditionOp":"over",
                      "nextLogic":"or"
                    }
                  ],
                  "enterItems":[
                    {
                      "id":602000100,
                      "enterSlideId":800000000,
                      "enterConditions":[]
                    }
                  ]
                },
                {
                  "id":603000000,
                  "title":"다시 존을 잃어버린 자리로 돌아간다",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":603000100,
                      "enterSlideId":200000000,
                      "enterConditions":[]
                    }
                  ]
                }
              ]
            },
            {
              "id":700000000,
              "slideImage":"image_3.gif",
              "title":"응꼬를 핥으려는 고양이 웬디!",
              "description":"너무 배가 고파서... 응꼬를 핥고 있어요...\n\"캬! 아냐! 난 단지 그루밍을 하고 있을 뿐이라고!\"\n웬디가 화가 났군요?\n",
              "count":0,
              "question":"무엇을 할까요?",
              "choiceItems":[
                {
                  "id":701000000,
                  "title":"다시 존을 잃어버린 자리로 돌아간다",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":701000100,
                      "enterSlideId":200000000,
                      "enterConditions":[]
                    }
                  ]
                }
              ]
            },
            {
              "id":800000000,
              "slideImage":"image_2.gif",
              "title":"그 녀석은 용서할 수 없는 녀석이야..",
              "description":"\"존... 그녀석은 내 먹이를 다 먹어 버렸지.. 괘씸한 녀석\"\n이런.. 존이 웬디의 밥을 모두 먹어버렸군요?\n\"물고기도 다 먹었으니 다시 빙글빙글 돌아볼까?\"\n웬디의 표정이 어쩐지 슬퍼보이는군요...",
              "count":0,
              "question":"돌아줄래요?",
              "choiceItems":[
                {
                  "id":801000000,
                  "title":"빙글빙글 돌아준다",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":801000100,
                      "enterSlideId":600000000,
                      "enterConditions":[]
                    }
                  ]
                },
                {
                  "id":802000000,
                  "title":"빙글빙글 안돌아준다",
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":802000100,
                      "enterSlideId":900000000,
                      "enterConditions":[]
                    }
                  ]
                }
              ]
            },
            {
              "id":900000000,
              "slideImage":"image_4.gif",
              "title":"아니 이 녀석 집에 있었잖아?",
              "description":"이럴수가! 그렇게 찾았던 존이 언제 돌아와 집에서 자고 있었을까요?\n후.. 걱정도 많이 했지만 귀여우니까 봐준다.\n존 크리스탈.. 오늘도 수고했어..",
              "count":0,
              "question":"",
              "choiceItems":[]
            }
          ]
        }
        """.trimIndent()
    }
}