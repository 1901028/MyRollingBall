package jp.ac.asojuku.st.myrollingball

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener, SurfaceHolder.Callback {

    private var surfaceWidth: Int = 0 // サーフェスビューの幅
    private var surfaceHeight: Int = 0 // サーフェスビューの高さ

    private val radius = 50.0f
    private val coef = 1000.0f

    private var ballX: Float = 0f
    private var ballY: Float = 0f
    private var vx: Float = 0f
    private var vy: Float = 0f
    private var time: Long = 0L

    private var hantei: Boolean = true;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)
        val holder = surfaceView.holder
        holder.addCallback(this)
    }

    override fun onResume() {
        super.onResume()
//        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
//        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        sensorManager.unregisterListener(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        ballX = (width / 2).toFloat()
        ballY = (height / 2).toFloat()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null) return

        if(time == 0L) time = System.currentTimeMillis()
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            val x = -event.values[0]
            val y = event.values[1]

            var t = (System.currentTimeMillis() - time).toFloat()
            time = System.currentTimeMillis()
            t /= 1000.0f

            val dx = vx * t + x * t * t / 2.0f
            val dy = vy * t + x * t * t / 2.0f
            ballX += dx * coef
            ballY += dy * coef
            vx += x * t
            vy += y * t

            if(ballX - radius < 0 && vx < 0){
                vx = -vx / 1.5f
                ballX = radius
            } else if(ballX + radius > surfaceWidth && vx > 0){
                vx = -vx / 1.5f
                ballX = surfaceWidth - radius
            }
            if(ballY - radius < 0 && vy < 0){
                vy = -vy /1.5f
                ballY = radius
            } else if(ballY + radius > surfaceHeight && vy > 0){
                vy = -vy / 1.5f
                ballY = surfaceHeight - radius
            }

            // 当たり判定
            // 失敗
            if(ballX + 25 > 300.0f && ballX - 25 < 600.0f && ballY + 25 > 200.0f && ballY - 25 < 600.0f){
                failed()
            }
            if(ballX + 25 > 600.0f && ballX - 25 < 700.0f && ballY + 25 > 0.0f && ballY - 25 < 600.0f){
                failed()
            }
            if(ballX + 25 > 0.0f && ballX - 25 < 50.0f && ballY + 25 > 0.0f && ballY - 25 < 600.0f){
                failed()
            }
            if(ballX + 25 > 50.0f && ballX - 25 < 400.0f && ballY + 25 > 0.0f && ballY - 25 < 50.0f){
                failed()
            }
            if(ballX + 25 > 0.0f && ballX - 25 < 1500.0f && ballY + 25 > 1550.0f && ballY - 25 < 1700.0f){
                failed()
            }

            // 成功
            if(ballX + 25 > 500.0f && ballX - 25 < 600.0f && ballY + 25 > 0.0f && ballY - 25 < 200.0f){
                success()
            }
            if(ballX + 25 > 700.0f && ballX - 25 < 1500.0f && ballY + 25 > 0.0f && ballY - 25 < 50.0f){
                success()
            }

            if(hantei){
                drawCanvas()
//                textView.setText(ballX.toString())
//                textView2.setText(ballY.toString())
            }

            button.setOnClickListener { resetbtn() }
        }

    }

    private fun drawCanvas(){
        val canvas = surfaceView.holder.lockCanvas()
        canvas.drawColor(Color.YELLOW)
        canvas.drawCircle(ballX, ballY, radius, Paint().apply{
            color = Color.MAGENTA
        })
        // 障害物の座標
        canvas.drawRect(300.0f, 200.0f, 600.0f, 600.0f, Paint().apply{
            color = Color.BLUE
        })
        canvas.drawRect(600.0f, 0.0f, 700.0f, 600.0f, Paint().apply{
            color = Color.BLUE
        })
        canvas.drawRect(0.0f, 0.0f, 50.0f, 600.0f, Paint().apply{
            color = Color.BLUE
        })
        canvas.drawRect(50.0f, 0.0f, 400.0f, 50.0f, Paint().apply{
            color = Color.BLUE
        })
        canvas.drawRect(0.0f, 1550.0f, 1500.0f, 1700.0f, Paint().apply{
            color = Color.BLUE
        })

        // ゴールの座標
        canvas.drawRect(500.0f, 0.0f, 600.0f, 200.0f, Paint().apply{
            color = Color.RED
        })
        canvas.drawRect(700.0f, 0.0f, 1500.0f, 50.0f, Paint().apply{
            color = Color.RED
        })

        surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    private fun success(){
        if(hantei){
            imageView.setImageResource(R.drawable.banzai)
        }
        hantei = false;
    }

    private fun failed(){
        if(hantei){
            imageView.setImageResource(R.drawable.ochikomu)
        }
        hantei = false;
    }
    private fun resetbtn(){
        ballX = (surfaceWidth / 2).toFloat()
        ballY = (surfaceHeight / 2).toFloat()
        vx = 0f
        vy = 0f
        time = 0L
        imageView.setImageResource(R.drawable.nayami)
        hantei = true
    }
}
