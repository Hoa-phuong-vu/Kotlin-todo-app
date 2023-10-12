package com.example.customproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class LogInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)



        auth = FirebaseAuth.getInstance()
        registerEvents()
    }

    private fun registerEvents(){
        val nextbtn = findViewById<ImageView>(R.id.next);
        val emailText = findViewById<TextInputEditText>(R.id.emailHint)
        val passText = findViewById<TextInputEditText>(R.id.passwordHint)
        val signup = findViewById<TextView>(R.id.SignUpClick)
        val progressbar = findViewById<ProgressBar>(R.id.progressBar2)

        signup.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        nextbtn.setOnClickListener{
            val email = emailText.text.toString().trim()
            val pass = passText.text.toString().trim()

            if ( email.isNotEmpty() && pass.isNotEmpty()) {
                    progressbar.visibility = View.VISIBLE
                    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(
                        OnCompleteListener {
                            if(it.isSuccessful){
                                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                            } else {
                                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                            progressbar.visibility = View.GONE
                        }
                    )
            } else {
                Toast.makeText(this, "Empty field not allowed", Toast.LENGTH_SHORT).show()
            }
        }

    }
}