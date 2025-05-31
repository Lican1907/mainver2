package com.example.mainver2

import Parser
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
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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


        val visualBlocks = extractVisualBlocks(mainContainer)

        val dslCode = generateDslCodeFromVisualBlocks(visualBlocks)
        //Log.d("DSL-Code", "DSL Code:\n$dslCode")
        //Toast.makeText(this, dslCode, Toast.LENGTH_LONG).show()

        val parser = Parser()
        val astBlocks = parser.parseProgram(dslCode)

        val rpnString = parser.compileProgramToRpnString(astBlocks)
        //Log.d("RPN String", "RPN:\n$rpnString")
        //console.text = "Final RPN string:\n$rpnString"

        executeRpnCode(rpnString)

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

            val newBlockContainer = RelativeLayout(this).apply {
                id = View.generateViewId()
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(100f)
                )
                tag = "dynamicBlock"
            }

            val blockView = createBlockById(v.id)
            blockView?.let {
                newBlockContainer.addView(it)
            }

            val slot = TextView(this).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(50f)
                )
                setBackgroundColor("#909090".toColorInt())
                visibility = View.GONE
            }
            newBlockContainer.addView(slot)

            val rootLayout = findViewById<RelativeLayout>(R.id.rootLayout)

            newBlockContainer.tag = "dynamicBlock"
            rootLayout.addView(newBlockContainer)
            newBlockContainer.bringToFront()

            //mainContainer.addView(newBlockContainer)

            val containerLocation = IntArray(2)
            rootLayout.getLocationOnScreen(containerLocation)
            val localX = event.rawX - containerLocation[0]
            val localY = event.rawY - containerLocation[1]
            newBlockContainer.x = localX
            newBlockContainer.y = localY


            newBlockContainer.setOnTouchListener { newV, newEvent -> handleMove(newV, newEvent) }

            return true
        }
        return false
    }

    private fun createBlockById(id: Int): View? {
        return when (id) {
            R.id.block_init -> {
                LinearLayout(this).apply {
                    layoutParams = RelativeLayout.LayoutParams(
                        dpToPx(250f),
                        dpToPx(50f)
                    )
                    orientation = LinearLayout.HORIZONTAL
                    setBackgroundColor("#FFA500".toColorInt())
                    setPadding(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))
                    gravity = Gravity.CENTER_VERTICAL

                    TextView(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        text = "init"
                        setTextColor(Color.WHITE)
                        textSize = 16f
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(0, 0, dpToPx(4f), 0)
                    }.also { addView(it) }

                    EditText(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                        hint = "variable"
                        setBackgroundColor(Color.WHITE)
                        setTextColor(Color.BLACK)
                        maxLines = 1
                        isSingleLine = true
                        inputType = InputType.TYPE_CLASS_TEXT
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                        tag = "init_variable"
                    }.also { addView(it) }

                    TextView(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        text = "="
                        setTextColor(Color.WHITE)
                        textSize = 16f
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                    }.also { addView(it) }

                    EditText(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                        hint = "value"
                        setBackgroundColor(Color.WHITE)
                        setTextColor(Color.BLACK)
                        maxLines = 1
                        isSingleLine = true
                        inputType = InputType.TYPE_CLASS_TEXT
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                        tag = "init_value"
                    }.also { addView(it) }
                }
            }
            R.id.block_assign -> {
                LinearLayout(this).apply {
                    layoutParams = RelativeLayout.LayoutParams(
                        dpToPx(250f),
                        dpToPx(50f)
                    )
                    orientation = LinearLayout.HORIZONTAL
                    setBackgroundColor("#00AA00".toColorInt())
                    setPadding(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))

                    val inputFirst = EditText(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            2f
                        )
                        setBackgroundColor(Color.WHITE)
                        setTextColor(Color.BLACK)
                        hint = "varName"
                        maxLines = 1
                        isSingleLine = true
                        setLines(1)
                        inputType = InputType.TYPE_CLASS_TEXT
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                        tag = "assign_input_first"
                    }

                    val label = TextView(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1f
                        )
                        text = " = "
                        setTextColor(Color.WHITE)
                        textSize = 16f
                    }

                    val inputSecond = EditText(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            2f
                        )
                        setBackgroundColor(Color.WHITE)
                        setTextColor(Color.BLACK)
                        hint = "value"
                        maxLines = 1
                        isSingleLine = true
                        setLines(1)
                        inputType = InputType.TYPE_CLASS_TEXT
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                        tag = "assign_input_second"
                    }

                    addView(inputFirst)
                    addView(label)
                    addView(inputSecond)
                }
            }
            R.id.block_if -> {
                LinearLayout(this).apply {
                    layoutParams = RelativeLayout.LayoutParams(
                        dpToPx(320f),
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor("#AA00AA".toColorInt())
                    setPadding(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))

                    LinearLayout(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL

                        TextView(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(30f),
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            text = "if"
                            setTextColor(Color.WHITE)
                            textSize = 16f
                            gravity = Gravity.CENTER_VERTICAL
                        }.also { addView(it) }

                        EditText(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            hint = "condition"
                            setBackgroundColor(Color.WHITE)
                            setTextColor(Color.BLACK)
                            maxLines = 1
                            inputType = InputType.TYPE_CLASS_TEXT
                            setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                            tag = "if_condition"
                        }.also { addView(it) }
                    }.also { addView(it) }

                    LinearLayout(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(dpToPx(16f), dpToPx(4f), 0, 0)

                        TextView(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(30f),
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            text = "then"
                            setTextColor(Color.WHITE)
                            textSize = 16f
                            gravity = Gravity.CENTER_VERTICAL
                        }.also { addView(it) }

                        EditText(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            hint = "action"
                            setBackgroundColor(Color.WHITE)
                            setTextColor(Color.BLACK)
                            maxLines = 1
                            inputType = InputType.TYPE_CLASS_TEXT
                            setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                            tag = "if_then"
                        }.also { addView(it) }
                    }.also { addView(it) }

                    LinearLayout(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(dpToPx(16f), dpToPx(4f), 0, 0)

                        TextView(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(30f),
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            text = "else"
                            setTextColor(Color.WHITE)
                            textSize = 16f
                            gravity = Gravity.CENTER_VERTICAL
                        }.also { addView(it) }

                        EditText(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            hint = "alternative action"
                            setBackgroundColor(Color.WHITE)
                            setTextColor(Color.BLACK)
                            maxLines = 1
                            inputType = InputType.TYPE_CLASS_TEXT
                            setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                            tag = "if_else"
                        }.also { addView(it) }
                    }.also { addView(it) }
                }
            }
            R.id.block_while -> {
                LinearLayout(this).apply {
                    layoutParams = RelativeLayout.LayoutParams(
                        dpToPx(300f),
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor("#880088".toColorInt())
                    setPadding(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))

                    LinearLayout(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL

                        TextView(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(50f),
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            text = "while"
                            setTextColor(Color.WHITE)
                            textSize = 16f
                            gravity = Gravity.CENTER_VERTICAL
                        }.also { addView(it) }

                        EditText(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            hint = "condition"
                            setBackgroundColor(Color.WHITE)
                            setTextColor(Color.BLACK)
                            maxLines = 1
                            isSingleLine = true
                            inputType = InputType.TYPE_CLASS_TEXT
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                            tag = "while_condition"
                        }.also { addView(it) }
                    }.also { addView(it) }

                    LinearLayout(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(dpToPx(16f), dpToPx(8f), 0, 0)

                        TextView(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(50f),
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            text = "do"
                            setTextColor(Color.WHITE)
                            textSize = 16f
                            gravity = Gravity.CENTER_VERTICAL
                        }.also { addView(it) }

                        EditText(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            hint = "action"
                            setBackgroundColor(Color.WHITE)
                            setTextColor(Color.BLACK)
                            maxLines = 1
                            isSingleLine = true
                            inputType = InputType.TYPE_CLASS_TEXT
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                            tag = "while_action"
                        }.also { addView(it) }
                    }.also { addView(it) }
                }
            }
            R.id.block_init_array -> {
                LinearLayout(this).apply {
                    layoutParams = RelativeLayout.LayoutParams(
                        dpToPx(300f),
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor("#0099CC".toColorInt())
                    setPadding(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))

                    LinearLayout(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL

                        TextView(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(80f),
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            text = "initArray"
                            setTextColor(Color.WHITE)
                            textSize = 16f
                            gravity = Gravity.CENTER_VERTICAL
                        }.also { addView(it) }

                        EditText(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            hint = "arrayName"
                            setBackgroundColor(Color.WHITE)
                            setTextColor(Color.BLACK)
                            maxLines = 1
                            inputType = InputType.TYPE_CLASS_TEXT
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                            tag = "array_name"
                        }.also { addView(it) }
                    }.also { addView(it) }

                    LinearLayout(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(dpToPx(16f), dpToPx(4f), 0, 0)

                        TextView(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(80f),
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            text = "size"
                            setTextColor(Color.WHITE)
                            textSize = 16f
                            gravity = Gravity.CENTER_VERTICAL
                        }.also { addView(it) }

                        EditText(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            hint = "arraySize"
                            setBackgroundColor(Color.WHITE)
                            setTextColor(Color.BLACK)
                            maxLines = 1
                            inputType = InputType.TYPE_CLASS_NUMBER
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                            tag = "array_size"
                        }.also { addView(it) }
                    }.also { addView(it) }

                    LinearLayout(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(dpToPx(16f), dpToPx(4f), 0, 0)

                        TextView(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(80f),
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            text = "data"
                            setTextColor(Color.WHITE)
                            textSize = 16f
                            gravity = Gravity.CENTER_VERTICAL
                        }.also { addView(it) }

                        EditText(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            hint = "1,2,3,..."
                            setBackgroundColor(Color.WHITE)
                            setTextColor(Color.BLACK)
                            maxLines = 1
                            inputType = InputType.TYPE_CLASS_TEXT
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(dpToPx(4f), 0, dpToPx(4f), 0)
                            tag = "array_data"
                        }.also { addView(it) }
                    }.also { addView(it) }
                }
            }
            R.id.block_print -> {
                LinearLayout(this).apply {
                    layoutParams = RelativeLayout.LayoutParams(
                        dpToPx(250f),
                        dpToPx(50f)
                    )
                    orientation = LinearLayout.HORIZONTAL
                    setBackgroundColor("#FF8800".toColorInt())
                    setPadding(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))
                    gravity = Gravity.CENTER_VERTICAL

                    TextView(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        text = "print"
                        setTextColor(Color.WHITE)
                        textSize = 16f
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(0, 0, dpToPx(8f), 0)
                    }.also { addView(it) }

                    EditText(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                        hint = "variable"
                        setBackgroundColor(Color.WHITE)
                        setTextColor(Color.BLACK)
                        maxLines = 1
                        isSingleLine = true
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(dpToPx(8f), dpToPx(4f), dpToPx(8f), dpToPx(4f))
                        tag = "print_value"
                    }.also { addView(it) }
                }
            }
            else -> null
        }
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

    private fun extractVisualBlocks(container: RelativeLayout): List<VisualBlock> {
        val blocks = mutableListOf<VisualBlock>()
        val childrenList = container.children.toList()
        Log.d("DEBUG", "Container child count: ${childrenList.size}")

        for (child in childrenList) {
            if(child is RelativeLayout) {
                Log.d("DEBUG", "Child: $child, tag: ${child.tag}")
                val block = createVisualBlockFromLayout(child)
                Log.d("ExtractBlock", "Child id = ${child.id}, block = $block")
                if (block != null) {
                    blocks.add(block)
                }
            }
        }
        Toast.makeText(this, "Blocks count: ${blocks.size}", Toast.LENGTH_LONG).show()
        return blocks
    }




    private fun createVisualBlockFromLayout(layout: RelativeLayout): VisualBlock? {
        val firstChild = layout.children.firstOrNull()
        Log.d("VisualBlock", "First child id: ${firstChild?.id}")
        return when (firstChild?.id) {
            R.id.block_init -> VisualInitBlock(layout)
            R.id.block_assign -> VisualAssignBlock(layout)
            R.id.block_if -> VisualIfElseBlock(layout)
            R.id.block_while -> VisualWhileBlock(layout)
            R.id.block_init_array -> VisualInitArrayBlock(layout)
            R.id.block_print -> VisualPrintBlock(layout)
            else -> {
                Log.d("VisualBlock", "Неопределенный тип блока")
                null
            }
        }
    }


    private fun generateDslCodeFromVisualBlocks(blocks: List<VisualBlock>): String {
        return blocks.joinToString("\n") { it.toDslString() }
    }

    private fun executeRpnCode(rpnString: String) {
        val tokens: MutableList<String> = rpnString.split(" ").toMutableList()

        val interpreter = Interpreter(tokens, console)

        interpreter.readReversePolishString()

        val output = "Interpreter output:\n" + interpreter.program.vars.toString()
        Log.d("Reverse Polish String", tokens.toString())
        Log.d("Interpreter", output)
        console.text = output
    }


//    program.add {compare(1, 2, "<")}
}