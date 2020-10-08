package com.alsharany.criminalintent

import java.util.*

data class Crime(val id:UUID=UUID.randomUUID(),
                 var title:String="",
                 val date:Date=Date(),
                 var isSolved:Boolean=false,
                 var requiredPolice:Int=0)