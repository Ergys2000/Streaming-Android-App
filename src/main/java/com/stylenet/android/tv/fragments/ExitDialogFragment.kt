package com.stylenet.android.tv.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.stylenet.android.tv.R

class ExitDialogFragment: DialogFragment() {

    private lateinit var exitButton: Button
    private lateinit var passwordEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_fragment_exit, container, false)
        exitButton = view.findViewById(R.id.exit)
        passwordEditText = view.findViewById(R.id.password)
        return view
    }

    override fun onStart() {
        super.onStart()
        setButtonListeners()
    }
    private fun setButtonListeners(){
        exitButton.setOnClickListener {
            val pass = passwordEditText.text.toString()
            if(pass == getString(R.string.exit_password)){
                requireActivity().finish()
            }else{
                this.dismiss()
            }
        }
    }
}