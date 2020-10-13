package com.alsharany.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class CrimeListViewModel : ViewModel() {
    var crimeRepository :CrimeRepository?=null
    var crimeListLiveData:LiveData<List<Crime>>?=null

    init {
        /*for(i in 0 until  100){
            val crime=Crime()
            crime.title="crime #$i"
            crime.isSolved=i%2==0
            crime.requiredPolice=if(i%2!=0){
                1
            }
            else
                2
            crimes+=crime
        }*/
        crimeRepository = CrimeRepository.get()
           crimeListLiveData = crimeRepository!!.getCrimes()
    }
}