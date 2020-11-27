package com.example.kotlinartbook

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.ByteArrayOutputStream
import java.util.jar.Manifest

class Main2Activity : AppCompatActivity() {

    val PERMISSION_GALERY = 1

    var selectedPicture : Uri? = null
    var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val intent = intent
        val info = intent.getStringExtra("info")


        if (info.equals("new")){

            artNameEditText.setText("")
            artistNameEditText.setText("")
            yearEditText.setText("")
            saveButton.visibility = View.VISIBLE
            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.select_image)
            imageView.setImageBitmap(selectedImageBackground)
        }else{


            val selectedId = intent.getIntExtra("id",1)
            saveButton.visibility = View.INVISIBLE

            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))
            val artNameIx = cursor.getColumnIndex("artname")
            val artistNameIx = cursor.getColumnIndex("artistname")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                artNameEditText.setText(cursor.getString(artNameIx))
                artistNameEditText.setText(cursor.getString(artistNameIx))
                yearEditText.setText(cursor.getString(yearIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                imageView.setImageBitmap(bitmap)
                


            }
        }

    }

    fun save (view: View){

        val artName = artNameEditText.text.toString()
        val artistName = artistNameEditText.text.toString()
        val year = yearEditText.text.toString()

        if (selectedBitmap != null){

            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()


            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR,image BLOB)")

            val sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?,?,?,?) "
            val statement = database.compileStatement(sqlString)
            statement.bindString(1,artName)
            statement.bindString(2,artistName)
            statement.bindString(3,year)
            statement.bindBlob(4,byteArray)

            statement.execute()


            val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }


    }

    fun makeSmallerBitmap (image: Bitmap, maximumSize : Int) : Bitmap{
        var width = image.width
        var height =image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1){
            width = maximumSize
            val scaleHight = width / bitmapRatio
            height = scaleHight.toInt()
        }else {
            height = maximumSize
            val scaleWidth = height * bitmapRatio
            width = scaleWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }


    fun selectImage (view: View){

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),PERMISSION_GALERY)
        }else{
            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentToGallery,2)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode==PERMISSION_GALERY){

            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentToGallery,2)

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode==2 && resultCode == Activity.RESULT_OK && data != null){

            selectedPicture = data.data



                if (Build.VERSION.SDK_INT >= 28){

                    val source = ImageDecoder.createSource(this.contentResolver,selectedPicture!!)
                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                    imageView.setImageBitmap(selectedBitmap)

                }else{
                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,selectedPicture)
                    imageView.setImageBitmap(selectedBitmap)
                }




        }

        super.onActivityResult(requestCode, resultCode, data)
    }

}
