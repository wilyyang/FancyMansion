package com.cheesejuice.fancymansion.util

import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.R.*

class Sample {
    companion object{
        fun getSampleJson(): String ="""
        { 
          "config":
          {
            "id":12345,
            "version":101001,
            "updateDate":234256544566,
            "publish":0,
            "title":"고양이 찾기",
            "writer":"양동국",
            "illustrator":"free",
            "description":"나의 고양이 존 크리스탈을 찾아주세요.. \n누군가 데려간걸까요? \n크리스탈의 친구들이 오늘따라 이상하군요..",
            "defaultImage":"image_1.gif",
            "startId":100,
            "defaultEndId":100
          },
          
          "slides":[
            {
              "id":100,
              "slideImage":"image_1.gif",
              "title":"내 고양이 존 크리스탈은 어디있을까?",
              "description":"아아.. 존 크리스탈 너는 어딨는거야.. 누가데려간거야?",
              "count":0,
              "question":"우선 다른 고양이에게 가보자!",
              "choiceItems":[
                {
                  "id":12354,
                  "title":"하얀 고양이 믕믕에게로",
                  
                  "showConditions":[
                    {
                      "id":12354,
                      "conditionId":100,
                      "conditionCount":2,
                      "conditionOp":"all"
                    }],
                  
                  "enterItems":[
                    {
                      "id":12354,
                      "enterSlideId":300,
                      "enterConditions":[
                        {
                          "id":12354,
                          "conditionId":400,
                          "conditionCount":3,
                          "conditionOp":"over"
                        },
                        {
                          "id":12354,
                          "conditionId":100,
                          "conditionCount":10,
                          "conditionOp":"under"
                        }
                      ]
                    },
                    {
                      "id":12354,
                      "enterSlideId":100,
                      "enterConditions":[]
                    }
                  ]
                },
                {
                  "id":23354,
                  "title":"얼룩 고양이 모모에게로",
                  
                  "showConditions":[
                  ],
                  
                  "enterItems":[
                    {
                      "id":12354,
                      "enterSlideId":400,
                      "enterConditions":[
                        {
                          "id":12354,
                          "conditionId":202,
                          "conditionCount":0,
                          "conditionOp":"all"
                        }
                      ]
                    }
                  ]
                }
              ]
            },
            {
              "id":300,
              "slideImage":"image_2.gif",
              "title":"와와와와와와",
              "description":"소소소소소소소소소소소소",
              "count":0,
              "question":"큐큐큐큐큐큐큐큐",
              "choiceItems":[
                {
                  "id":12678,
                  "title":"푸푸푸푸푸",
                  
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":12354,
                      "enterSlideId":400,
                      "enterConditions":[]
                    }
                  ]
                }
              ]
            },
            {
              "id":400,
              "slideImage":"image_5.gif",
              "title":"rkrkrk",
              "description":"rkrkrkrkrkrkrkrkrk",
              "count":0,
              "question":"rkrkrkrkrkrkrkrkrkrkrkrk",
              "choiceItems":[
                {
                  "id":12678,
                  "title":"rkrkrk",
                  
                  "showConditions":[],
                  "enterItems":[
                    {
                      "id":12354,
                      "enterSlideId":100,
                      "enterConditions":[]
                    }
                  ]
                }
              ]
            }
          ]
        }
    """.trimIndent()


        fun getSampleConfig(): String ="""
        { 
          "id":12345,
          "version":101001,
          "updateDate":234256544566,
          "publish":0,
          "title":"고양이 찾기",
          "writer":"양동국",
          "illustrator":"free",
          "description":"나의 고양이 존 크리스탈을 찾아주세요.. \n누군가 데려간걸까요? \n크리스탈의 친구들이 오늘따라 이상하군요..",
          "defaultImage":"image_1.gif",
          "startId":100
        }
    """.trimIndent()

        fun getSampleImageId(image: String):Int = when(image){
            "image_1.gif" -> raw.image_1
            "image_2.gif" -> raw.image_2
            "image_3.gif" -> raw.image_3
            "image_4.gif" -> raw.image_4
            "image_5.gif" -> raw.image_5
            "image_6.gif" -> raw.image_6
            else -> raw.image_1
        }
    }
}