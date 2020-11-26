package course.examples.ui.alertdialog

import android.app.AlertDialog
import android.app.Dialog

import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.artapp.MainActivity

// Class that creates the AlertDialog
class RoomKeyDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(): RoomKeyDialogFragment {
            return RoomKeyDialogFragment()
        }
    }

    // Build AlertDialog using AlertDialog.Builder
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inputText = EditText(activity)
        inputText.hint = "Room Key"
        return AlertDialog.Builder(activity)
            .setTitle("Enter Room Key:")
            .setView(inputText)

            // User cannot dismiss dialog by hitting back button
            .setCancelable(true)

            // Set up Cancel Button
            .setNegativeButton("Cancel") { _, _ ->
                dismiss()
            }

            // Set up OK Button
            .setPositiveButton("OK") { _, _ ->
                (activity as MainActivity).requestToEnterExistingRoom(inputText.text.toString())
            }
            .create()
    }
}
