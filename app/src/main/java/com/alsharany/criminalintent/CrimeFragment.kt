package com.alsharany.criminalintent


import DatePickerFragment
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*
import kotlin.time.ExperimentalTime
import java.text.SimpleDateFormat as SimpleDateFormat1

private const val DATE_FORMAT = "EEE, MMM, dd"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 1



class CrimeFragment : Fragment() , DatePickerFragment.DateCallbacks ,
    TimePickerFragment.TimeCallbacks {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var crimeSuspectButton: Button

    //next line belong to challenge 14 ch 15
    private lateinit var crimeCallSuspectButton: Button
    private lateinit var crimeReportButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime , container , false)
        titleField = view.findViewById(R.id.requiredcrime_title) as EditText
        dateButton = view.findViewById(R.id.requiredcrime_date) as Button
        timeButton = view.findViewById(R.id.requiredcrime_time) as Button
        crimeSuspectButton = view.findViewById(R.id.crime_suspect) as Button
        //next line belong to challenge 14 ch 15
        crimeCallSuspectButton = view.findViewById(R.id.crime_call_suspect) as Button
        crimeReportButton = view.findViewById(R.id.crime_report) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox


        return view
    }

    @ExperimentalTime
    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        crimeDetailViewModel.crimLiveData.observe(
            viewLifecycleOwner ,
            { crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            }
        )

    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence? ,
                start: Int ,
                count: Int ,
                after: Int
            ) {                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?, start: Int, before: Int, count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {                // This one too            }        }
            }
        }
        titleField.addTextChangedListener(titleWatcher)
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _ , isChecked ->
                crime.isSolved = isChecked
            }
        }
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment , REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager() , DIALOG_DATE)
            }
        }
        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment , REQUEST_TIME)
                show(this@CrimeFragment.requireFragmentManager() , DIALOG_TIME)

            }
        }
        crimeReportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT , getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT ,
                    getString(R.string.crime_report_subject)
                )

            }.also { intent ->
                val chooserIntent = Intent.createChooser(
                    intent ,
                    getString(R.string.send_report)
                )
                startActivity(chooserIntent)
            }
        }
        crimeSuspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK , ContactsContract.Contacts.CONTENT_URI).apply {
                    type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                }
            setOnClickListener {
                startActivityForResult(pickContactIntent , REQUEST_CONTACT)
            }
        }
        //next block belong to challenge 14 ch 15
        crimeCallSuspectButton.setOnClickListener {
            val callContactIntent = Intent(Intent.ACTION_DIAL)
            callContactIntent.data = Uri.parse("tel:${crime.suspectPhone}")
            startActivity(callContactIntent)

        }
    }

    override fun onActivityResult(requestCode: Int , resultCode: Int , data: Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                // Specify which fields you want your query to return values for
                val queryFields = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME ,
                    //next line belong to challenge 14 ch 15
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )


                // Perform your query - the contactUri is like a "where" clause here
                val cursor = contactUri?.let {
                    requireActivity().contentResolver.query(it , queryFields , null , null , null)
                }
                cursor?.use {
                    // Verify cursor contains at least one result
                    if (it.count == 0) {
                        return
                    }
                    // Pull out the first column of the first row of data
                    // that is your suspect's name
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    //next line belong to challenge 14 ch 15
                    crime.suspectPhone = it.getString(1)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    crimeSuspectButton.text = suspect

                }
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = SimpleDateFormat1(DATE_FORMAT).format(crime.date)

        timeButton.text = SimpleDateFormat1("HH:MM").format(crime.date.time)

        if (crime.suspect.isNotEmpty()) {
            crimeSuspectButton.text = crime.suspect
            crimeCallSuspectButton.visibility = View.VISIBLE
        } else
            crimeCallSuspectButton.visibility = View.GONE
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()

        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT , crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect , crime.suspect)
        }
        return getString(
            R.string.crime_report ,
            crime.title ,
            dateString ,
            solvedString ,
            suspect
        )
    }


    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }


    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID , crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }


    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }


}
