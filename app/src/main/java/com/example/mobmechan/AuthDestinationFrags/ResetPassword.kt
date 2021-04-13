package com.example.mobmechan.AuthDestinationFrags


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.mobmechan.R

import com.example.mobmechan.databinding.FragmentResetPasswordBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ResetPassword.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResetPassword : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var resetPasswordBinding: FragmentResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        resetPasswordBinding= FragmentResetPasswordBinding.inflate(inflater)
        return resetPasswordBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resetPasswordBinding.btnresetpass.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_resetPassword_to_loginFrag)
        }

        resetPasswordBinding.loginlinkresetpass.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_resetPassword_to_loginFrag)
        }
        resetPasswordBinding.registerlinkresetpass.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_resetPassword_to_registrationFrag)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ResetPassword.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResetPassword().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}