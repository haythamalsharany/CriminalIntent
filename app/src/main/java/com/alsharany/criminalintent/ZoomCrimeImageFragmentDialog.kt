package com.alsharany.criminalintent

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import java.io.File

private val FILE_ARG = "photo File"

class ZoomCrimeImageFragmentDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val view = activity?.layoutInflater?.inflate(R.layout.zoom_image_fragmen_dialog , null)
        val imgeView = view?.findViewById(R.id.zoom_image_view) as ImageView
        val imgeFile = arguments?.getSerializable(FILE_ARG) as File
        if (imgeFile.exists()) {
            var pictureUtils = PictureUtils()
            val bitmap = pictureUtils.getScaledBitmap(
                imgeFile.path ,
                requireActivity()
            )
            imgeView.setImageBitmap(bitmap)
        } else
            imgeView.setImageDrawable(null)



        return AlertDialog.Builder(requireContext() , R.style.ThemeOverlay_AppCompat_Dialog_Alert)
            .setView(view)
            .setTitle("Image zooming")
            .setNegativeButton("back") { dialog , _ ->
                dialog.cancel()

            }.create()
    }

    companion object {
        fun newInstance(photoFile: File): ZoomCrimeImageFragmentDialog {
            val args = Bundle().apply {
                putSerializable(FILE_ARG , photoFile)
            }
            return ZoomCrimeImageFragmentDialog().apply {
                arguments = args
            }

        }
    }
}