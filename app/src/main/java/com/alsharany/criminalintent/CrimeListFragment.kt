package com.alsharany.criminalintent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }
    private lateinit var crimeRecyclerView: RecyclerView

    //  private var adapter: CrimeAdapter? = null
  var adapter: CrimeAdapter? = CrimeAdapter(emptyList())



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView

        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        // updateUI()
        crimeRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData?.observe(
            viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                    Log.i(
                        TAG,
                        "Got crimes ${crimes.size}"
                    )
                    updateUI (crimes)
                }
            }
      )
}

private fun updateUI(crimes: List<Crime>) {

    adapter = CrimeAdapter(crimes)
    crimeRecyclerView.adapter = adapter
}

abstract open class CrimeHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract open fun bind(item: Crime)

}

private inner class NormalCrimeHolder(view: View) : CrimeHolder(view),
    View.OnClickListener {
        private lateinit var crime: Crime
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)
        private val titleTextView: TextView = itemView.findViewById(R.id.requiredcrime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.requiredcrime_date)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
        }
        override fun bind(item: Crime) {

                this.crime = item

                titleTextView.text = this.crime.title
                dateTextView.text = DateFormat.getDateInstance(DateFormat.FULL).format(this.crime.date).toString()
                solvedImageView.visibility=if(item.isSolved){
                    View.VISIBLE
                }
                else
                    View.GONE
              }
    }
    private  inner class RequirredCrimeHolder(view: View) : CrimeHolder(view){
        private lateinit var crime: Crime
         val requiredCrimeTextView: TextView = itemView.findViewById(R.id.requiredcrime_title)
        val requireddateTextView: TextView = itemView.findViewById(R.id.requiredcrime_date)
       // private val requiredContactButton: Button = itemView.findViewById(R.id.contactPoliceBTN)

            override fun bind(item: Crime) {
            this.crime = item

            requiredCrimeTextView.text = this.crime.title
            requireddateTextView.text= DateFormat.getDateInstance(DateFormat.FULL).format(this.crime.date).toString()
        }

    }
    inner class CrimeAdapter(var crimes: List<Crime>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val requiredCrime = 1
        val normalCrime = 2


        override fun getItemViewType(position: Int): Int {
            return if (crimes[position].isSolved == true)
                return requiredCrime
            else
                return normalCrime

        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view: View?
            var recyclerViewholder: RecyclerView.ViewHolder
            when (viewType) {
                requiredCrime -> {

                    val view = layoutInflater.inflate(
                        R.layout.list_item_police_required_crime,
                        parent,
                        false
                    )

                    recyclerViewholder = RequirredCrimeHolder(view)
                }
                else -> {
                    val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
                    recyclerViewholder = NormalCrimeHolder(view)
                }

            }

            return recyclerViewholder

        }



        override fun getItemCount(): Int {
            return crimes.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val crime = crimes[position]
            if (holder is RequirredCrimeHolder)
                holder.bind(crime)
               else
                if(holder is NormalCrimeHolder)
                    holder.bind(crime)





        }
    }
    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}
