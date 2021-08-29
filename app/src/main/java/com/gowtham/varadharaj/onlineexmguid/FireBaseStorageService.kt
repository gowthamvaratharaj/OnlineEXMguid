package com.gowtham.varadharaj.onlineexmguid

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class FireBaseStorageService {
    private val TAG = "FireBaseStorageService"
    //timeFormat for image name
    private val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
    private val now = Date()
    private val fileName: String = formatter.format(now)
    
    //realtime database Reference
    private  val dataBaseRef:DatabaseReference =FirebaseDatabase.getInstance().getReference("Questions")
    private val dbId = dataBaseRef.push().key.toString()

    //realtime Storage Reference
    private val storageReference = FirebaseStorage.getInstance().getReference("Questions/$fileName")

    //properties that hold Data Globally
    lateinit var questionData:String
    lateinit var questionNumberGlobal:String
    lateinit var answerData:String
    lateinit var imageURL: String

    fun uploadImage(context: Context,
                    question:String,
                    imageUri:Uri,
                    answerFromSpinner:String,
                    questionNumber:String,
                    alertbox: AlertDialog,
    ) {
        //progress bar
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Uploading Files ...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        //Store Image on CloudStorage
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                Toast.makeText(context, "Successfully Uploaded", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "uploadImage: Image Uploaded Successfully")
                if(progressDialog.isShowing)
                    progressDialog.dismiss()
                storageReference.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "uploadImage: $it")
                    saveDB(
                        question = question,
                        answer = answerFromSpinner,
                        imageUri = it.toString(),
                        questionNumber = questionNumber,
                        alertbox = alertbox
                    )
                }
            }.addOnCanceledListener {
                Log.d(TAG, "uploadImage: downloadUrl not fetched")
                if(progressDialog.isShowing)progressDialog.dismiss()
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveDB(question: String,
                       answer: String,
                       imageUri: String,
                       questionNumber: String,
                       alertbox: AlertDialog
    ) {
        Log.d(TAG, "method called ")
        val db = Db(
            id = dbId,
            question = question,
            answer = answer,
            imageUrl = imageUri,
            questionNumber = questionNumber)
        dataBaseRef.child(dbId).setValue(db).addOnCompleteListener{
                questionData = question
                answerData = answer
                imageURL = imageUri
            questionNumberGlobal = questionNumber
            alertbox.show()
        }
    }
     fun getDataDB(
         context: Context,
         question: ArrayList<Db>,
         recyclerView: RecyclerView,
         tempArrayList: ArrayList<Db>
     ){
        dataBaseRef
            .addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (dbSnapshot in snapshot.children){
                        val dbsnap = dbSnapshot.getValue(Db::class.java)
                        question.add(dbsnap!!)
                    }
                    //search
                    tempArrayList.addAll(question)
                    //this to call Recycler Adapter
                    val adapter = MyRecyclerAdapter(tempArrayList)
                    recyclerView.adapter = adapter
                    adapter.setOnItemClickListener(object:MyRecyclerAdapter.OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            val intent = Intent(context,ImageAnswerUpdate::class.java)
                            intent.putExtra("imageUrl",tempArrayList[position].imageUrl)
                            intent.putExtra("option",tempArrayList[position].answer)
                            intent.putExtra("id",tempArrayList[position].id)
                            intent.putExtra("question",tempArrayList[position].question)
                            intent.putExtra("questionNumber",tempArrayList[position].questionNumber)
                            context.startActivity(intent)
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    fun update(
        context: Context,
        answer: String,
        id:String,
        question: String,
        imageUrl:String,
        questionNumber: String,
        alertbox: AlertDialog
    ) {
        Log.d("update", " Update method called")
        val updateStr = mapOf<String,String>(
            "id" to id ,
        "question" to question ,
        "answer" to answer,
        "imageUrl" to imageUrl,
            "questionNumber" to questionNumber
        )
        dataBaseRef
            .child(id)
            .updateChildren(updateStr)
            .addOnSuccessListener {
                Log.d("update", " Update method success")
                Toast.makeText(context, "Successfully Updated!", Toast.LENGTH_SHORT).show()
                alertbox.show()
        }.addOnCanceledListener {
            Log.d("update", " Update method failed")
        }
    }
}