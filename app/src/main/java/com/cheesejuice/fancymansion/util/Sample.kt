package com.cheesejuice.fancymansion.util

class Sample {
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
            "startId":100
          },
          
          "slides":[
            {
              "id":100,
              "slideImage":"image_1.gif",
              "title":"내 고양이 존 크리스탈은 어디있을까?",
              "description":"아아.. 존 크리스탈 너는 어딨는거야.. 누가데려간거야?",
              "count":0,
              "question":"우선 다른 고양이에게 가보자!",
              "slideItems":[
                {
                  "id":12354,
                  "title":"하얀 고양이 믕믕에게로",
                  
                  "showConditions":[
                    {
                      "id":12354,
                      "conditionId":600,
                      "conditionCount":3,
                      "conditionOp":"over"
                    },
                    {
                      "id":12355,
                      "conditionId":600,
                      "conditionCount":6,
                      "conditionOp":"below"
                    }
                  ],
                  
                  "enterItems":[
                    {
                      "id":12354,
                      "enterConditions":[
                        {
                          "id":12354,
                          "conditionId":202,
                          "conditionCount":0,
                          "enterSlide":300,
                          "conditionOp":"equal"
                        }
                      ]
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
                      "enterConditions":[
                        {
                          "id":12354,
                          "conditionId":202,
                          "conditionCount":0,
                          "enterSlide":300,
                          "conditionOp":"all"
                        }
                      ]
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
}