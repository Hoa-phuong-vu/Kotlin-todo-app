package com.example.customproject

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        registerEvents()
    }

    private fun registerEvents(){
        val nextbtn = findViewById<ImageView>(R.id.next);
        val emailText = findViewById<TextInputEditText>(R.id.emailHint)
        val passText = findViewById<TextInputEditText>(R.id.passwordHint)
        val retypeText = findViewById<TextInputEditText>(R.id.retypeHint)
        val login = findViewById<TextView>(R.id.LogInClick)
        val progressbar = findViewById<ProgressBar>(R.id.progressBar)

        login.setOnClickListener{
            startActivity(Intent(this, LogInActivity::class.java))
        }

        nextbtn.setOnClickListener{
            val email = emailText.text.toString().trim()
            val pass = passText.text.toString().trim()
            val verifyPass = retypeText.text.toString().trim()

            if ( email.isNotEmpty() && pass.isNotEmpty() && verifyPass.isNotEmpty()) {
                if (pass == verifyPass) {
                    progressbar.visibility = View.VISIBLE
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(
                       OnCompleteListener {
                            if(it.isSuccessful){
                                Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                            } else {
                                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                           progressbar.visibility = View.GONE
                        }
                    )
                }
                else {
                    Toast.makeText(this, "passwords don't match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty field not allowed", Toast.LENGTH_SHORT).show()
            }

        }


    }

}