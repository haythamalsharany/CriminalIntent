package com.alsharany.criminalintent

import DatePickerFragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.text.DateFormat
import java.util.*
import kotlin.time.ExperimentalTime

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1

class CrimeFragment : Fragment() , DatePickerFragment.DateCallbacks ,
    TimePickerFragment.TimeCallbacks {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
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
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
//        dateButton.apply {
//            text = crime.date.toString()
//            isEnabled = false
//        }


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
            timeButton.setOnClickListener {
                TimePickerFragment.newInstance(crime.date).apply {
                    setTargetFragment(this@CrimeFragment , REQUEST_TIME)
                    show(this@CrimeFragment.requireFragmentManager() , DIALOG_TIME)

                }
            }
        }
    }


    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text =
            DateFormat.getDateInstance(DateFormat.FULL).format(this.crime.date).toString()
        timeButton.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(crime.date.time)


        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()

        }
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

    override fun onTimeSelected(time: Date) {

        updateUI()
    }


//    override fun onTimeSelected(hour: Int , minute: Int) {
//        this.hour=hour
//        this.minute=minute
//        updateUI()
//
//    }
}
