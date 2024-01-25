package com.puzzytigres.joguers.game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.puzzytigres.joguers.R
import com.puzzytigres.joguers.databinding.MyGameActivityBinding

class MyPlayGameActivity: AppCompatActivity() {
    private lateinit var bindingPlayGame: MyGameActivityBinding
    private var isStart = true
    private val firstRound = listOf(
        R.drawable.i1, R.drawable.i2,
        R.drawable.i3, R.drawable.i4,
        R.drawable.i5, R.drawable.i6,
        R.drawable.i7, R.drawable.i8,
    )
    private val secondRound = mutableListOf<Int>()
    private val thirdRound = mutableListOf<Int>()
    private val retired = mutableListOf<Int>()
    private val curImages = mutableListOf<Int>()
    private var currentImage = 0
    private var finalImage = 0
    private var countRound = 1
    private var countPair = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        if (isStart) {
            setContentView(R.layout.hello_layout)

            var startBt = findViewById<Button>(R.id.startGame)
            startBt.setOnClickListener { startGame() }
        }
    }

    private fun startGame() {
        bindingPlayGame = MyGameActivityBinding.inflate(layoutInflater)
        setContentView(bindingPlayGame.root)

        setImages()
        bindingPlayGame.gameNextButton.setOnClickListener { myNextPicture(false) }
        bindingPlayGame.firstPicture.setOnClickListener { v -> select1Picture(v as ImageView) }
        bindingPlayGame.secondPicture.setOnClickListener { v -> select1Picture(v as ImageView) }
    }

    private fun select1Picture(v: ImageView) {
        currentImage = v.tag as Int
        when (countPair) {
            1 -> {
                if (secondRound.size == 0) {
                    secondRound.add(0, currentImage)
                } else {
                    secondRound[0] = currentImage
                }
            }
            2 -> {
                if (secondRound.size == 1) {
                    secondRound.add(1, currentImage)
                } else {
                    secondRound[1] = currentImage
                }
            }
            3 -> {
                if (secondRound.size == 2) {
                    secondRound.add(2, currentImage)
                } else {
                    secondRound[2] = currentImage
                }
            }
            4 -> {
                if (secondRound.size == 3) {
                    secondRound.add(3, currentImage)
                } else {
                    secondRound[3] = currentImage
                }
            }
            5 -> {
                if (thirdRound.size == 0) {
                    thirdRound.add(0, currentImage)
                } else {
                    thirdRound[0] = currentImage
                }
            }
            6 -> {
                if (thirdRound.size == 1) {
                    thirdRound.add(1, currentImage)
                } else {
                    thirdRound[1] = currentImage
                }
            }
            7 -> { finalImage = currentImage }
        }
    }

    private var isSec = false

    private fun myNextPicture(isFinal: Boolean) {
        retired.addAll(curImages)
        retired.removeAll(secondRound)
        retired.removeAll(thirdRound)
        for (i in retired) {
            Log.d("retired", "$i")
        }
        for (i in curImages) {
            Log.d("curImages", "$i")
        }
        curImages.clear()
        Log.d("Next Pic", "$countPair - $countRound - ${secondRound.size}")
        if (!isFinal) {
            if (currentImage != 0) {
                countPair++
                if (secondRound.size == 4 && !isSec) {
                    countRound++
                    isSec = true
                }
                if (thirdRound.size == 2) {
                    countRound++
                }
                if (finalImage != 0) {
                    bindingPlayGame.firstPicture.visibility = View.INVISIBLE
                    bindingPlayGame.secondPicture.visibility = View.INVISIBLE
                    bindingPlayGame.finalPicture.setImageResource(finalImage)
                    bindingPlayGame.finalPicture.visibility = View.VISIBLE

                    bindingPlayGame.gameNextButton.apply {
                        text = "Play again"
                        setOnClickListener { myNextPicture(true) }
                    }
                }
                setImages()
                currentImage = 0
            } else {
                Toast.makeText(this, "You need to choose a picture", Toast.LENGTH_LONG).show()
            }
        } else {
            this@MyPlayGameActivity.finish()
            startActivity(Intent(this, MyPlayGameActivity::class.java))
        }
    }

    private fun setImages() {
        if (countRound == 1) {
            val fP = firstRound.random()
            val sP = firstRound.random()

            if (fP == sP || retired.contains(fP) || retired.contains(sP) ||
                    secondRound.contains(fP) || secondRound.contains(sP)) {
                setImages()
            } else {
                bindingPlayGame.firstPicture.setImageResource(fP)
                bindingPlayGame.firstPicture.tag = fP
                bindingPlayGame.secondPicture.setImageResource(sP)
                bindingPlayGame.secondPicture.tag = sP
                curImages.add(fP)
                curImages.add(sP)
                Log.d("fP 1", "$fP")
                Log.d("sP 1", "$sP")
            }
        } else if (countRound == 2) {

            bindingPlayGame.textTitleGame.text = "Second Round"

            val fP = secondRound.random()
            val sP = secondRound.random()

            if (fP == sP || retired.contains(fP) || retired.contains(sP) ||
                thirdRound.contains(fP) || thirdRound.contains(sP)) {
                setImages()
            } else {
                bindingPlayGame.firstPicture.setImageResource(fP)
                bindingPlayGame.firstPicture.tag = fP
                bindingPlayGame.secondPicture.setImageResource(sP)
                bindingPlayGame.secondPicture.tag = sP
                curImages.add(fP)
                curImages.add(sP)
                Log.d("fP 2", "$fP")
                Log.d("sP 2", "$sP")
            }
        } else if (countRound == 3) {
            bindingPlayGame.textTitleGame.text = "Final Round"
            bindingPlayGame.firstPicture.setImageResource(thirdRound[0])
            bindingPlayGame.firstPicture.tag = thirdRound[0]
            bindingPlayGame.secondPicture.setImageResource(thirdRound[1])
            bindingPlayGame.firstPicture.tag = thirdRound[1]
            Log.d("fP 1", "${thirdRound[0]}")
            Log.d("sP 1", "${thirdRound[1]}")
        }
    }
}