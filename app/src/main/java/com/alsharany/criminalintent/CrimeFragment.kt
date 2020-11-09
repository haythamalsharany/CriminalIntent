package com.alsharany.criminalintent


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.io.File
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
private const val REQUEST_PHOTO = 2



class CrimeFragment : Fragment() , DatePickerFragment.DateCallbacks ,
    TimePickerFragment.TimeCallbacks {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var crimeSuspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var treeObserver: ViewTreeObserver
    private var viewWidth = 0
    private var viewHeight = 0

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
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView
        treeObserver = photoView.viewTreeObserver
        treeObserver.addOnGlobalLayoutListener {
            viewWidth = photoView.width
            viewHeight = photoView.height
        }




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
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(
                        requireActivity() ,
                        "com.alsharany.criminalintent.fileprovider" ,
                        photoFile
                    )
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
        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity = packageManager.resolveActivity(
                captureImage , PackageManager.MATCH_DEFAULT_ONLY
            )
            if (resolvedActivity == null)
                isEnabled = false
            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT , photoUri)
                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(
                        captureImage ,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName ,
                        photoUri ,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                startActivityForResult(captureImage , REQUEST_PHOTO)
            }
        }
        photoView.setOnClickListener {
            ZoomCrimeImageFragmentDialog.newInstance(photoFile).apply {
                show(this@CrimeFragment.requireFragmentManager() , "zoom")
            }

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
                    requireActivity().contentResolver.query(
                        it , queryFields ,
                        null , null , null
                    )
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
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(
                    photoUri ,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                updatePhotoView(viewWidth , viewHeight)
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
        updatePhotoView(viewWidth , viewHeight)
    }

    private fun updatePhotoView(width: Int , height: Int) {
        var pictureUtils = PictureUtils()
        if (photoFile.exists()) {
            val bitmap = pictureUtils.getScaledBitmap(
                photoFile.path ,
                width ,
                height
            )
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }


    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT , crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
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
        photoView.viewTreeObserver.apply {
            if (isAlive) {
                addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(
            photoUri ,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

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
