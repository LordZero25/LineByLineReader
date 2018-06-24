package com.sample.zero.linebylinereader

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.content_main.*
import android.widget.SeekBar.OnSeekBarChangeListener
import org.jetbrains.anko.selector
import java.io.File
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {

    private var  _loadedUri: Uri = Uri.EMPTY
    private var _lines: List<String> = ArrayList()
    private var _currentLine: Int = 0

    private val _encoding = hashMapOf(
            "UTF8" to Charsets.UTF_8,
            "UTF16" to Charsets.UTF_16,
            "UTF16LE" to Charsets.UTF_16LE,
            "UTF16BE" to Charsets.UTF_16BE,
            "UTF32" to Charsets.UTF_32,
            "UTF32LE" to Charsets.UTF_32LE,
            "UTF32BE" to Charsets.UTF_32BE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.ThemeOverlay_Material_Dark)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        intent.apply {
            if (action == Intent.ACTION_VIEW || action == null) {
                loadFile(data)
                intent.removeCategory(Intent.CATEGORY_DEFAULT)
                intent.action = null
            }
        }

        btnNext.setOnClickListener {
            var temp = _currentLine + 1

            while(_lines[temp].isBlank()) {
                temp = temp + 1
            }

            if (_lines.size - 1 >= temp) {
                _currentLine = temp
                txtMain.setText(_lines[_currentLine])

                seekBar.max = 0
                seekBar.max = _lines.count()
                seekBar.progress = _currentLine

                temp = _currentLine + 1
                lblStatus.text = temp.toString()
            }
        }

        btnPrev.setOnClickListener {
            var temp = _currentLine - 1

            while(_lines[temp].isBlank()) {
                temp = temp - 1

                if(temp == 0) {
                    break
                }
            }

            if (temp >= 0) {
                _currentLine = temp
                txtMain.setText(_lines[_currentLine])

                seekBar.max = 0
                seekBar.max = _lines.count()
                seekBar.progress = _currentLine

                temp = _currentLine + 1
                lblStatus.text = temp.toString()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                _currentLine = seekBar.progress
                try {
                    txtMain.setText(_lines[_currentLine])
                } catch (e: IndexOutOfBoundsException) {
                    txtMain.setText(_lines.last())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (seekBar.progress < 0) {
                    txtMain.setText(_lines[0])
                }
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                lblStatus.text = (progress + 1).toString()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.action_open) {
//            Toast.makeText(applicationContext, "This is my Toast message!", Toast.LENGTH_LONG).show()
            settingOpen()
            return true
        } else if (item.itemId == R.id.action_encoding) {
            reEncodeLines()
            return true
        } else {
            return false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val selectedfile = data.data//The uri with the location of the file
//            Toast.makeText(applicationContext, selectedfile.path, Toast.LENGTH_LONG).show()

            loadFile(selectedfile)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        if(newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val height = scrollViewMain.height
        }
        else if(newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }

    }

    private fun settingOpen() {
        val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123)
    }

    private fun loadFile(uri: Uri) {
        val fileName = File(uri.path).name
        _loadedUri = uri
        _lines = contentResolver.openInputStream(uri).bufferedReader().readLines()

        txtMain.setText(_lines[0])
        seekBar.max = _lines.count()

        lblStatus.text = "1"
        lblStatus.visibility = View.VISIBLE
    }

    private fun reEncodeLines() {
        var tempNames = _encoding.map { x -> x.key }.sorted()
        selector("Select Encoding", tempNames, { _, i ->
            //            toast("So you're living in ${tempNames[i]}, right?")
            val tempEncoding = _encoding[tempNames[i]] as Charset
            _lines = contentResolver.openInputStream(_loadedUri).bufferedReader(tempEncoding).readLines()

            txtMain.setText(_lines[_currentLine])
        })
    }

}
