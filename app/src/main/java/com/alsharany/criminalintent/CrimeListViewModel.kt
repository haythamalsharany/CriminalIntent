package com.alsharany.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java.io.File

class CrimeListViewModel : ViewModel() {
    private var crimeRepository: CrimeRepository? = null
    var crimeListLiveData: LiveData<List<Crime>>? = null

    init {

        crimeRepository = CrimeRepository.get()
        crimeListLiveData = crimeRepository!!.getCrimes()
    }

    fun addCrime(crime: Crime) {
        crimeRepository?.addCrime(crime)
    }

    fun getPhotoFile(crime: Crime): File {
        return crimeRepository!!.getPhotoFile(crime)
    }
}