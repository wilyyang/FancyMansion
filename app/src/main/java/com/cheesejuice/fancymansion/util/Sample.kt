package com.cheesejuice.fancymansion.util

class Sample {
    public fun getSampleJson(): String ="""
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
              "title":"고양이 찾기",
              "description":"디테일 샘플",
              "count":0,
              "question":"질문",
              "slideItems":[
                {
                  "id":12354,
                  "title":"선택지1",
                  
                  "showConditions":[
                    {
                      "id":12354,
                      "conditionId":202,
                      "conditionCount":0
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
                          "enterSlide":300
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
}