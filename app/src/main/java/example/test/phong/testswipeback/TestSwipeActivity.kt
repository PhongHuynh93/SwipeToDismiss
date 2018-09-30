package example.test.phong.testswipeback

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_test_swipe.*

class TestSwipeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_swipe)
        imageView.setOnTouchListener { v, event -> true }
        seekBar.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                Log.e("TAG", "run ontouch imageview")
                // prevent parent from getting touch event
                v.parent.requestDisallowInterceptTouchEvent(true)
                return false
            }
        })
    }
}
