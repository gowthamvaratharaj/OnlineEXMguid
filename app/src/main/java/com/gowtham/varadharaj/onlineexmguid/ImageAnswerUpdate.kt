package com.gowtham.varadharaj.onlineexmguid

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlin.system.exitProcess

class ImageAnswerUpdate : AppCompatActivity() {
    private val TAG = "ImageAnswerUpdate"

    lateinit var answerFrmSpn: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_answer_update)
        Log.d(TAG, "onCreate: method started")
        val fb = FireBaseStorageService()
        val imageUrl:ImageView = findViewById(R.id.imageView)
        val updateBtn:Button = findViewById(R.id.update_btn)
        val answerTV : TextView = findViewById(R.id.answer_display)
        val questionCpy:Button = findViewById(R.id.question_cpy)
        val questionNumber:TextView = findViewById(R.id.textView)

        spinner()

        val bundle:Bundle? =intent.extras
        val image = bundle!!.getString("imageUrl")
        val ans = bundle!!.getString("option")
        val id = bundle!!.getString("id")
        val ques = bundle!!.getString("question")
        val quesNum = bundle!!.getString("questionNumber")

        Picasso.get().load(image).into(imageUrl)
        answerTV.text = ans
        questionNumber.text = quesNum
        questionCpy.setOnClickListener {
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("text", ques)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
        }
        updateBtn.setOnClickListener {
            Log.d(TAG, "onCreate: updateBTN called ")

            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.dialogTitle)
            builder.setMessage(R.string.dialogMessage)
            //performing positive action
            builder.setPositiveButton("Go Back"){dialogInterface, which ->
                //Toast.makeText(applicationContext,"clicked yes",Toast.LENGTH_LONG).show()
                exitProcess(0)
            }
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false)


                fb.update(
                    context = this,
                    id = id!!,
                    answer = answerFrmSpn,
                    question = ques!!,
                    imageUrl = image!!,
                    questionNumber = quesNum!!,
                    alertbox = alertDialog
                )

        }
    }

    private fun spinner() {
        Log.d(TAG, "spinner: successfully spinner called")
        // access the spinner
        val languages: Array<String> = resources.getStringArray(R.array.answer_options)
        val spinner = findViewById<Spinner>(R.id.spinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, languages)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View,
                                            position: Int,
                                            id: Long) {
                    answerFrmSpn = languages[position]
                    Toast.makeText(this@ImageAnswerUpdate,languages[position], Toast.LENGTH_SHORT).show()
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
    }
}