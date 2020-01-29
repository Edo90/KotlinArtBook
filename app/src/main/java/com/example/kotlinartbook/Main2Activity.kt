package com.example.kotlinartbook

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.decodeBitmap
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class Main2Activity : AppCompatActivity() {

    var bitmapImage : Bitmap? = null
    var info : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        info = intent.getStringExtra("info")

        if(info.equals("new")){
            val background = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.select_image)
            imageView.setImageBitmap(background)
            button.visibility = View.VISIBLE
            editText.setText("")
        }else{
            val name = intent.getStringExtra("name")
            editText.setText(name)
            button.visibility = View.INVISIBLE

            val chosen = Globals.Chosen
            val bitmap = chosen.returnImage()

            imageView.setImageBitmap(bitmap)
        }
    }

    fun select(view: View){
        if(info.equals("new")){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }else{
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent,2)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1){
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent,2)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            val image = data.data
            try {
                val selectedImage = ImageDecoder.createSource(contentResolver, image!!)
                bitmapImage = ImageDecoder.decodeBitmap(selectedImage)
                imageView.setImageBitmap(bitmapImage)
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun save(view: View){
        val name = editText.text.toString()
        val outputStream = ByteArrayOutputStream()
        bitmapImage?.compress(Bitmap.CompressFormat.PNG,50,outputStream)
        val byteArray = outputStream.toByteArray()

        try {
            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)
            database.execSQL("CREATE TABLE IF NOT EXISTS arts(name VARCHAR, image BLOB)")


            val sqlString = "INSERT INTO arts(name,image) VALUES (?,?)"
            val statement = database.compileStatement(sqlString)
            statement.bindString(1,name)
            statement.bindBlob(2, byteArray)
            statement.execute()
        }catch (e: Exception){
            e.printStackTrace()
        }
        val intent = Intent(applicationContext,MainActivity::class.java)
        startActivity(intent)
    }
}
