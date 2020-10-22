package com.alsharany.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }
    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var noDataTextView: TextView
    private lateinit var addCrimeButton: Button
    private var adapter: CrimeAdapter? = CrimeAdapter()

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater ,
        container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_crime_list , container , false)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView


        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        // updateUI()
        crimeRecyclerView.adapter = adapter
        noDataTextView = view.findViewById(R.id.empty_list_textview)
        // the next two  lines of code belong to challenge No 13 ch 14
        addCrimeButton = view.findViewById(R.id.addCrimeBtn)
        noDataTextView.text = "ther is no data"
        return view
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        crimeListViewModel.crimeListLiveData?.observe(viewLifecycleOwner , { crimes ->
            crimes?.let {
                Log.i(
                    TAG ,
                    "Got crimes ${crimes.size}"
                )
                updateUI(crimes)
            }
        })
        // the next block of code  belong to challenge No 13 ch 14
        addCrimeButton.setOnClickListener {
            val crime = Crime()
            crimeListViewModel.addCrime(crime)
            callbacks?.onCrimeSelected(crime.id)

        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateUI(crimes: List<Crime>) {

        //the next line is belong to challenge 13 ch 14
        if (!crimes.isEmpty()) {
            adapter = CrimeAdapter()
            //the next two lines are  belong to challenge 13 ch 14
            noDataTextView.visibility = View.GONE
            addCrimeButton.visibility = View.GONE

            crimeRecyclerView.adapter = adapter
            // some edit belonge to challenage No.11
            val adapterTemp = crimeRecyclerView.adapter as CrimeAdapter
            adapterTemp.submitList(crimes)
        } else {
            //the next line is belong to challenge 13 ch 14
            crimeRecyclerView.visibility = View.GONE

        }

    }


    inner class CrimeAdapter :
    // some edit belonge to challenage No.11
    // change extend from RecyclerView.Adapter to  androidx.recyclerview.widget.ListAdapter<Crime,RecyclerView.ViewHolder>
        androidx.recyclerview.widget.ListAdapter<Crime , RecyclerView.ViewHolder>(CrimeDiffUtil()) {
        private val requiredCrime = 1
        private val normalCrime = 2
        private val emptyCrime = 0


        private inner class NormalCrimeHolder(view: View) : RecyclerView.ViewHolder(view) ,
            View.OnClickListener {
            private lateinit var crime: Crime
            private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)
            private val titleTextView: TextView = itemView.findViewById(R.id.requiredcrime_title)
            private val dateTextView: TextView = itemView.findViewById(R.id.requiredcrime_date)

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(v: View) {
                //Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
                callbacks?.onCrimeSelected(crime.id)
            }

            fun bind(item: Crime) {

                this.crime = item

                titleTextView.text = this.crime.title
                dateTextView.text =
                    DateFormat.getDateInstance(DateFormat.FULL).format(this.crime.date).toString()
                solvedImageView.visibility = if (item.isSolved) {
                    View.VISIBLE
                } else
                    View.GONE
            }
        }


        private inner class RequirredCrimeHolder(view: View) : RecyclerView.ViewHolder(view) ,
            View.OnClickListener {
            private lateinit var crime: Crime
            val requiredCrimeTextView: TextView = itemView.findViewById(R.id.requiredcrime_title)
            val requireddateTextView: TextView = itemView.findViewById(R.id.requiredcrime_date)

            // private val requiredContactButton: Button = itemView.findViewById(R.id.contactPoliceBTN)
            init {
                itemView.setOnClickListener(this)
            }

            fun bind(item: Crime) {
                this.crime = item

                requiredCrimeTextView.text = this.crime.title
                requireddateTextView.text =
                    DateFormat.getDateInstance(DateFormat.FULL).format(this.crime.date).toString()
            }

            override fun onClick(v: View?) {
                callbacks?.onCrimeSelected(crime.id)
            }

        }


        override fun getItemViewType(position: Int): Int {
            // some edit belonge to challenage No.11
            //because no list in this type of adapter I change  from crimes[position] to getItem(position)

            return if (!getItem(position).isSolved) {
                requiredCrime
            } else {
                normalCrime
            }

        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val recyclerViewholder: RecyclerView.ViewHolder
            val view: View?
            when (viewType) {
                this.requiredCrime -> {

                    view = layoutInflater.inflate(
                        R.layout.list_item_police_required_crime ,
                        parent ,
                        false
                    )

                    recyclerViewholder = view?.let { RequirredCrimeHolder(it) }!!
                }


                else -> {
                    view = layoutInflater.inflate(R.layout.list_item_crime , parent , false)
                    recyclerViewholder = view?.let { NormalCrimeHolder(it) }!!
                }

            }

            return recyclerViewholder

        }




        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // some edit belonge to challenage No.11
            //because no list in this type of adapter I change  from crimes[position] to getItem(position)
            val crime = getItem(position)
            if (holder is RequirredCrimeHolder)
                holder.bind(crime)
            else
                if (holder is NormalCrimeHolder)
                    holder.bind(crime)


        }
    }


    // some edit belonge to challenage No.11
    // create  crimeDiffUtil to check diffirence between items
    private class CrimeDiffUtil : DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime , newItem: Crime): Boolean {

            return oldItem.id === newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime , newItem: Crime): Boolean {

            return (
                    oldItem.id == newItem.id &&
                            oldItem.title == newItem.title &&
                            oldItem.date == newItem.date &&
                            oldItem.isSolved == newItem.isSolved)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu , inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu , inflater)
        inflater.inflate(R.menu.fragment_crime_list , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else ->
                super.onOptionsItemSelected(item)


        }

    }
}
