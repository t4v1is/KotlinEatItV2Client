package com.example.kotlineatitv2client

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Model.UserModel
import com.example.kotlineatitv2client.Remote.ICloudFunctions
import com.example.kotlineatitv2client.Remote.RetrofitCloudClient
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.layout_register.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var listener:FirebaseAuth.AuthStateListener
    private lateinit var dialog: AlertDialog
    private val compositeDisposable = CompositeDisposable()
    private lateinit var cloudFunctions:ICloudFunctions

    private lateinit var userRef:DatabaseReference
    private var providers :List<AuthUI.IdpConfig>? = null

    companion object {
        private var APP_REQUEST_CODE = 3012
    }

    override fun onStart(){
        super.onStart()
        firebaseAuth.addAuthStateListener (listener)
    }

    override fun onStop(){
        if (listener != null)
            firebaseAuth.removeAuthStateListener (listener)
        compositeDisposable.clear()
        super.onStop()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init ()
    }

    private fun init() {

        providers = Arrays.asList<AuthUI.IdpConfig>(AuthUI.IdpConfig.PhoneBuilder().build())
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        listener = FirebaseAuth.AuthStateListener {   firebaseAuth ->
            val user = firebaseAuth.currentUser
            if(user != null){

                checkUserFromFirebase(user!!)
            }
            else{

                phoneLogin()
            }

        }

    }

    private fun checkUserFromFirebase(user : FirebaseUser) {
        dialog!!.show()
        userRef!!.child(user!!.uid)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(this@MainActivity, ""+p0.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        val userModel = p0.getValue(UserModel::class.java)
                        goToHomeActivity(userModel)
                    }else{
                        showRegisterDialog(user!!)
                    }
                    dialog!!.dismiss()
                }
            })
    }

    private fun showRegisterDialog(user : FirebaseUser) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("REGISTER")
        builder.setMessage("Rellena todos los campos")

        val itemView = LayoutInflater.from(this@MainActivity)
            .inflate(R.layout.layout_register, null)

        val edt_name = itemView.findViewById<EditText>(R.id.edt_name)
        val edt_address = itemView.findViewById<EditText>(R.id.edt_address)
        val edt_phone = itemView.findViewById<EditText>(R.id.edt_phone)

        //set
        edt_phone.setText(user!!.phoneNumber)

        builder.setView(itemView)
        builder.setNegativeButton("CANCELAR") {dialogInterface, i -> dialogInterface.dismiss()  }
        builder.setPositiveButton("REGISTRAR"){dialogInterface, i ->
            if(TextUtils.isDigitsOnly(edt_name.text.toString())){
                Toast.makeText(this@MainActivity, "Ingresa tu nombre por fis", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }else if (TextUtils.isDigitsOnly(edt_address.text.toString())){
                Toast.makeText(this@MainActivity, "Ingresa tu dirección por fis", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val userModel = UserModel()
            userModel.uid = user!!.uid
            userModel.name = edt_name.text.toString()
            userModel.address = edt_address.text.toString()
            userModel.phone = edt_phone.text.toString()

            userRef!!.child(user!!.uid)
                .setValue(userModel)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        dialogInterface.dismiss()
                        Toast.makeText(this@MainActivity, "Registrado con Exito !!", Toast.LENGTH_SHORT).show()
                        goToHomeActivity(userModel)
                    }
                }
        }

        //Importante mostrar dialog
        val dialog = builder.create()
        dialog.show()
    }

    private fun goToHomeActivity(userModel: UserModel?) {
        Common.currentUser = userModel!!
        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
        finish()
    }

    private fun phoneLogin() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers!!).build(), APP_REQUEST_CODE)

    }

   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == APP_REQUEST_CODE)
        {
            val response = IdpResponse.fromResultIntent(data)
            if(resultCode == Activity.RESULT_OK){
                val user = FirebaseAuth.getInstance().currentUser
            }else{
                Toast.makeText(this, "Fallo al logear", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
