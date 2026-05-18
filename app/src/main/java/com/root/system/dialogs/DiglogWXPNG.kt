package com.root.system.dialogs

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toUri
import com.root.system.R

class DialogWXPNG(var context: Activity) {
    fun showWXPNGMenu() {
        val view = context.layoutInflater.inflate(R.layout.dialog_wxpng_operation, null)

        // Function to execute power operation


        // Adding support for wxpng image
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        imageView.setImageResource(R.drawable.wxpng)
        imageView.setOnClickListener {
            // Implement image click functionality here
            // For example, you can open an image viewer to display the wxpng image
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType("android.resource://${context.packageName}/${R.drawable.wxpng}".toUri(), "image/*")
            context.startActivity(intent)
            Toast.makeText(context, "谢谢", Toast.LENGTH_LONG).show()
        }
    }
}
