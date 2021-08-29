package com.gowtham.varadharaj.onlineexmguid

import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.gowtham.varadharaj.onlineexmguid.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val REQUEST_CODE_GALARY = 100
    private val REQUEST_IMAGE_CAPTURE = 101
    //binding
    lateinit var binding: ActivityMainBinding
    private val fb = FireBaseStorageService()

    lateinit var currentPhotoPath: String
    lateinit var imageUri: Uri
    lateinit var uploadDB: Button
    lateinit var selectimage: Button
    lateinit var homepage:Button
    lateinit var question:EditText
    lateinit var question_numberEt:EditText
    private lateinit var answer: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        spinner()
        question_numberEt = binding.questionNumberEt
        question = binding.getQuestion
        uploadDB = binding.uploadDb
        homepage = binding.Home
        selectimage = binding.selectImageFromGalary
        val homePageIntent = Intent(this@MainActivity, Home::class.java)
        //Button To Take A PIC
        selectimage.setOnClickListener {
            //selectImage()
            dispatchTakePictureIntent()
        }
        //BUTTON TO START ACTIVITY HOME FOR ANSWER
        homepage.setOnClickListener (){ this@MainActivity.startActivity(homePageIntent)
            exitProcess(0)
        }
        //BUTTON TO UPLOAD DATA TO FIREBASE
        uploadDB.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.dialogTitleUpdate)
            builder.setMessage(R.string.dialogMessageUpdate)
            //performing positive action
            builder.setPositiveButton("Exit"){dialogInterface, which ->
                //Toast.makeText(applicationContext,"clicked yes",Toast.LENGTH_LONG).show()
                exitProcess(0)

            }
            builder.setNegativeButton("Answers"){dialogInterface, which ->
                //Toast.makeText(applicationContext,"clicked yes",Toast.LENGTH_LONG).show()
                //myIntent.putExtra("key", value) //Optional parameters
                this@MainActivity.startActivity(homePageIntent)
                exitProcess(0)
            }

            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false)

            val getQuestion = question.text.toString().trim()
            val question_number = question_numberEt.text.toString().trim()
            //data upload to FB
            fb.uploadImage(this,
                question = getQuestion,
                imageUri = imageUri,
                answerFromSpinner = answer,
                questionNumber = question_number,
                alertbox = alertDialog,

            )
            Log.d("db", "onCreate:button clicked ")
        }
    }

    //SPINNER TO SELECT ANSWER
    private fun spinner() {
        // access the items of the list
        val answerOptionsItems = resources.getStringArray(R.array.answer_options)
        //we created a spinner object
        val answerOptions = binding.answerOptions
        //Spinner
        if (answerOptions != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item,
                answerOptionsItems)
            answerOptions.adapter = adapter

            answerOptions.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View,
                                            position: Int,
                                            id: Long) {
                    Toast.makeText(this@MainActivity, answerOptionsItems[position], Toast.LENGTH_SHORT).show()
                    answer = answerOptionsItems[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action

                }
            }
        }
    }
    //METHOD TO SELECT IMAGE FROM GALARY
    private fun selectImage() {
        val intent = Intent()
        intent.type="image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,REQUEST_CODE_GALARY)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult: This method is called now ____(the data is _ $data)")
        if(requestCode == REQUEST_CODE_GALARY || resultCode == RESULT_OK) {
            val f = File(currentPhotoPath)
            binding.capturedImageImgView.setImageURI(Uri.fromFile(f))
            Log.d(TAG, "onActivityResult: url path:${Uri.fromFile(f)}")

            imageUri = Uri.fromFile(f)
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                mediaScanIntent.data = Uri.fromFile(f)
                sendBroadcast(mediaScanIntent)
            }

           // binding.capturedImageImgView.setImageURI(imageUri)
        }
    }

    //METHOD TO CREATE IMAGE FILE
    @Throws(IOException::class)
    private fun createImageFile(): File {
        Log.d(TAG, "createImageFile: createImageFile is called")
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }

    }

    fun bitmapToFile(bitmap: Bitmap): File? { // File name like "image.png"
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(Environment.getExternalStorageDirectory().toString() + File.separator + timeStamp)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    //TAKE A PICTURE FROM CAMERA
    private fun dispatchTakePictureIntent() {
        Log.d(TAG, "dispatchTakePictureIntent: This function is called")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    //bitmapToFile(imageUri)
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {file->
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.gowtham.varadharaj.android.fileprovider",
                        file
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }
}