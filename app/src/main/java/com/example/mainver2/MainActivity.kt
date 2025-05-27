package com.example.mainver2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.view.View
import android.widget.TextView
import android.graphics.Color
import androidx.core.view.GravityCompat
import androidx.core.view.children
import android.util.Log
import android.graphics.Rect
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.transition.Visibility
import com.google.android.material.floatingactionbutton.FloatingActionButton

@SuppressLint("ClickableViewAccessibility")
class MainActivity : AppCompatActivity() {
    private lateinit var rootLayout: RelativeLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var mainContainer: RelativeLayout
    private lateinit var trashBin: ImageView
    private lateinit var console: TextView
    private lateinit var runButton: FloatingActionButton
    private lateinit var stopButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.rootLayout)

        drawerLayout = findViewById(R.id.drawer_layout)
        val drawerContent: LinearLayout = findViewById(R.id.menu)
        for (child in drawerContent.children) {
            child.setOnTouchListener {v, event -> createNewBlock(v, event)}
        }

        mainContainer = findViewById(R.id.main)
        mainContainer.setOnTouchListener {v, event -> handleMove(v, event)}
        trashBin = findViewById(R.id.trashBin)
        console = findViewById(R.id.console)
        runButton = findViewById(R.id.runButton)
        runButton.setOnClickListener {run()}
        stopButton = findViewById(R.id.stopButton)
        stopButton.setOnClickListener {stop()}

        val buttonBack = findViewById<ImageButton>(R.id.imageButtonBack)
        buttonBack.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
        }
    }

    private fun run(): Boolean {
        console.visibility = View.VISIBLE
        runButton.visibility = View.GONE
        stopButton.visibility = View.VISIBLE

        return true
    }

    private fun stop(): Boolean {
        console.visibility = View.GONE
        runButton.visibility = View.VISIBLE
        stopButton.visibility = View.GONE

        return true
    }

    private var dx = 0f
    private var dy = 0f
    private var slot: TextView? = null
    private fun handleMove(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.performClick()
                slot = null
                dx = event.rawX - v.x
                dy = event.rawY - v.y
                if (v in mainContainer.children) {
                    mainContainer.removeView(v)
                    for (child in mainContainer.children) {
                        val params = child.layoutParams as RelativeLayout.LayoutParams
                        if (params.getRule(RelativeLayout.BELOW) == v.id) {
                            params.removeRule(RelativeLayout.BELOW)
                            val vParams = v.layoutParams as RelativeLayout.LayoutParams
                            params.addRule(RelativeLayout.BELOW, vParams.getRule(RelativeLayout.BELOW))
                            child.layoutParams = params
                            break
                        }
                    }
                    rootLayout.addView(v)
                    v.x = 500f
                    v.y = 1500f
                    return false
                }
                v.x = event.rawX - dx
                v.y = event.rawY - dy
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                v.x = event.rawX - dx
                v.y = event.rawY - dy
                if (v.id == R.id.main) return true
                if (isViewIntersect(v, trashBin)){
                    if (slot != null){
                        (slot as TextView).visibility = View.GONE
                        slot = null
                    }
                    return true
                }
                if (!isViewInsideLayout(v, mainContainer)) {
                    if (slot != null){
                        (slot as TextView).visibility = View.GONE
                        slot = null
                    }
                    return true
                }
                if(slot == null) {
                    for (child in mainContainer.children) {
                        if (isViewInsideLayout(v, child as RelativeLayout)) {
                            slot = child.children.last() as TextView
                            slot!!.visibility = View.VISIBLE
                            return true
                        }
                    }
                }
                if (isViewInsideLayout(v, slot?.parent as RelativeLayout)) return true
                slot!!.visibility = View.GONE
                for (child in mainContainer.children) {
                    if (isViewInsideLayout(v, child as RelativeLayout)) {
                        slot = child.children.last() as TextView
                        slot!!.visibility = View.VISIBLE
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (v.id == R.id.main) return true
                if (isViewIntersect(v, trashBin)) {
                    (v.parent as ViewGroup).removeView(v)
                    return false
                }
                if (slot?.isVisible == true) {
                    (v.parent as ViewGroup).removeView(v)
                    var connection: RelativeLayout? = null
                    for (child in mainContainer.children) {
                        val params = child.layoutParams as RelativeLayout.LayoutParams
                        if (params.getRule(RelativeLayout.BELOW) == (slot!!.parent as RelativeLayout).id) {
                            connection = child as RelativeLayout
                            break
                        }
                    }
                    val new = getNewBlock(v as RelativeLayout, slot!!.parent as RelativeLayout)
                    mainContainer.addView(new)
                    slot!!.visibility = View.GONE
                    if (connection != null) {
                        val params = connection.layoutParams as RelativeLayout.LayoutParams
                        params.removeRule(RelativeLayout.BELOW)
                        params.addRule(RelativeLayout.BELOW, new.id)
                        connection.layoutParams = params
                    }
                }
                slot = null
                return false
            }
        }
        return false
    }
    private fun isViewIntersect(view1: View, view2: View): Boolean {
        val rect1 = Rect()
        view1.getGlobalVisibleRect(rect1)
        val rect2 = Rect()
        view2.getGlobalVisibleRect(rect2)
        return Rect.intersects(rect1, rect2)
    }

    private fun isViewInsideLayout(v: View, layout: ViewGroup): Boolean {
        val rect = Rect()
        layout.getGlobalVisibleRect(rect)
        return rect.contains(v.x.toInt(), v.y.toInt())
    }

    private fun createNewBlock(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            drawerLayout.closeDrawer(GravityCompat.START)

            val new =
                RelativeLayout(this).apply {
                    id = View.generateViewId()
                    layoutParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                    )
                }

            val block = TextView(this).apply {
                layoutParams = RelativeLayout.LayoutParams(dpToPx(250f), dpToPx(50f))
                background = v.background
                setTextColor("#FFFFFF".toColorInt())
                text = (v as TextView).text
                setPadding(dpToPx(16f), dpToPx(16f), dpToPx(16f), dpToPx(16f))
            }
            new.addView(block)

            val slot = TextView(this).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(50f)
                )
                setBackgroundColor("#909090".toColorInt())
                visibility = View.GONE
            }
            new.addView(slot)

            val rootLayout = findViewById<RelativeLayout>(R.id.rootLayout)
            rootLayout.addView(new)

            new.x = event.rawX
            new.y = event.rawY

            new.setOnTouchListener { newV, newEvent -> handleMove(newV, newEvent) }
            return true
        }
        return false
    }

    private fun dpToPx(dp: Float): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun getNewBlock(old: RelativeLayout, top: RelativeLayout): RelativeLayout {
        val new =
            RelativeLayout(this).apply {
                id = View.generateViewId()
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                ).apply { addRule(RelativeLayout.BELOW, top.id) }
            }

        val block = old.children.first()
        old.removeView(block)
        block.id = View.generateViewId()
        new.addView(block)

        val slot = old.children.last()
        old.removeView(slot)
        val params = slot.layoutParams as RelativeLayout.LayoutParams
        params.addRule(RelativeLayout.BELOW, block.id)
        slot.layoutParams = params
        new.addView(slot)

        new.setOnTouchListener {v, event -> handleMove(v, event)}

        return new
    }


//    program.add {compare(1, 2, "<")}
}