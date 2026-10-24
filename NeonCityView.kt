package com.rayan.neoncity

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class NeonCityView(context: Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG)
    private var screen = 0 // 0 title, 1 story, 2 game
    private var playerX = 520f
    private var playerY = 540f
    private var targetX = 980f
    private var targetY = 270f
    private var signalFound = false
    private var joystickX = 0f
    private var joystickY = 0f
    private var joystickActive = false
    private var lastTime = System.nanoTime()
    private var message = "MISSION: Follow the mysterious signal."
    private val worldW = 2200f
    private val worldH = 1300f

    private val buildings = listOf(
        RectF(90f,120f,430f,360f), RectF(540f,90f,850f,330f),
        RectF(1040f,110f,1350f,390f), RectF(1510f,80f,1870f,350f),
        RectF(120f,600f,410f,920f), RectF(560f,650f,900f,980f),
        RectF(1030f,590f,1320f,900f), RectF(1480f,620f,1880f,940f),
        RectF(360f,1040f,720f,1220f), RectF(1160f,1010f,1510f,1230f)
    )

    init {
        text.typeface = Typeface.create("sans", Typeface.NORMAL)
        isFocusable = true
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if (screen == 0) drawTitle(c)
        else if (screen == 1) drawStory(c)
        else drawGame(c)
        postInvalidateOnAnimation()
    }

    private fun drawTitle(c: Canvas) {
        c.drawColor(Color.rgb(5,7,13))
        p.shader = LinearGradient(0f,0f,width.toFloat(),height.toFloat(),
            Color.rgb(0,229,255), Color.rgb(255,43,214), Shader.TileMode.CLAMP)
        c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p)
        p.shader = null
        p.color = Color.argb(220,5,7,13); c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p)
        centerText(c,"RAYAN",height*0.36f,Color.WHITE,58f,Typeface.BOLD)
        centerText(c,"NEON CITY",height*0.47f,Color.rgb(0,229,255),38f,Typeface.BOLD)
        centerText(c,"FUTURE SHADOWS",height*0.56f,Color.rgb(255,43,214),20f,Typeface.BOLD)
        roundButton(c,width*0.36f,height*0.70f,width*0.64f,height*0.83f,"START CHAPTER 1")
    }

    private fun drawStory(c: Canvas) {
        c.drawColor(Color.rgb(6,9,17))
        centerText(c,"CHAPTER 1",height*0.20f,Color.rgb(0,229,255),28f,Typeface.BOLD)
        centerText(c,"THE SIGNAL",height*0.29f,Color.WHITE,42f,Typeface.BOLD)
        val lines = listOf(
            "12:47 AM. Neon City never sleeps.",
            "Rayan's phone suddenly receives an",
            "encrypted signal from the future district.",
            "",
            "MISSION: Find the source of the signal."
        )
        var y = height*0.43f
        for (s in lines) {
            centerText(c,s,y,Color.LTGRAY,20f,Typeface.NORMAL); y += 34f
        }
        roundButton(c,width*0.39f,height*0.78f,width*0.61f,height*0.90f,"ENTER CITY")
    }

    private fun drawGame(c: Canvas) {
        c.drawColor(Color.rgb(9,12,20))
        val sx = width / worldW
        val sy = height / worldH
        val scale = min(sx,sy) * 1.18f
        val camX = width/2f - playerX*scale
        val camY = height/2f - playerY*scale

        c.save()
        c.translate(camX,camY)
        p.color = Color.rgb(15,19,29); c.drawRect(0f,0f,worldW,worldH,p)

        // Roads
        p.color = Color.rgb(28,34,47)
        c.drawRect(0f,450f,worldW,570f,p)
        c.drawRect(930f,0f,1050f,worldH,p)
        c.drawRect(0f,970f,worldW,1070f,p)

        // Neon road lines
        p.color = Color.rgb(0,130,160)
        for (x in 0..2200 step 110) c.drawRect(x.toFloat(),505f,(x+55).toFloat(),510f,p)
        for (y in 0..1300 step 110) c.drawRect(990f,y.toFloat(),995f,(y+55).toFloat(),p)

        // Buildings
        for (b in buildings) {
            p.color = Color.rgb(24,30,44); c.drawRoundRect(b,18f,18f,p)
            p.style = Paint.Style.STROKE; p.strokeWidth=5f; p.color=Color.rgb(0,120,150)
            c.drawRoundRect(b,18f,18f,p); p.style=Paint.Style.FILL
        }

        // Future district glow
        p.color = Color.argb(45,255,43,214)
        c.drawCircle(1600f,500f,330f,p)

        // Signal marker
        if (!signalFound) {
            p.color = Color.rgb(255,43,214)
            c.drawCircle(targetX,targetY,26f + 8f*sin(System.currentTimeMillis()/180.0).toFloat(),p)
            p.color = Color.WHITE; c.drawCircle(targetX,targetY,7f,p)
        }

        // Player
        p.color = Color.rgb(0,229,255); c.drawCircle(playerX,playerY,27f,p)
        p.color = Color.WHITE; c.drawCircle(playerX-7,playerY-5,5f,p); c.drawCircle(playerX+7,playerY-5,5f,p)

        c.restore()

        // HUD
        p.color = Color.argb(190,5,7,13); c.drawRoundRect(18f,18f,width-18f,90f,18f,18f,p)
        leftText(c,"RAYAN",36f,51f,Color.rgb(0,229,255),22f,Typeface.BOLD)
        leftText(c, if(signalFound) "MISSION COMPLETE" else message,36f,78f,Color.WHITE,15f,Typeface.NORMAL)

        // joystick
        p.color = Color.argb(80,255,255,255); c.drawCircle(105f,height-105f,72f,p)
        p.color = Color.argb(150,0,229,255)
        c.drawCircle(105f + joystickX*42f,height-105f + joystickY*42f,30f,p)

        // action button
        p.color = Color.argb(155,255,43,214); c.drawCircle(width-105f,height-105f,62f,p)
        centerTextAt(c,width-105f,height-98f,"A",Color.WHITE,30f,Typeface.BOLD)

        // objective arrow
        if (!signalFound) {
            val dx=targetX-playerX; val dy=targetY-playerY
            leftText(c,"Signal: ${sqrt(dx*dx+dy*dy).toInt()} m",width-220f,45f,Color.WHITE,14f,Typeface.BOLD)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when(e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (screen == 0 && e.action == MotionEvent.ACTION_DOWN) {
                    screen = 1; return true
                }
                if (screen == 1 && e.action == MotionEvent.ACTION_DOWN) {
                    screen = 2; return true
                }
                if (screen == 2) {
                    if (e.x < width*0.38f && e.y > height*0.60f) {
                        joystickActive=true
                        val dx=e.x-105f; val dy=e.y-(height-105f)
                        val len=max(1f,sqrt(dx*dx+dy*dy))
                        joystickX=(dx/len).coerceIn(-1f,1f)
                        joystickY=(dy/len).coerceIn(-1f,1f)
                    }
                    if (e.x > width*0.78f && e.y > height*0.65f && e.action==MotionEvent.ACTION_DOWN) {
                        checkSignal()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                joystickActive=false; joystickX=0f; joystickY=0f
            }
        }
        return true
    }

    private fun checkSignal() {
        val d=hypot(targetX-playerX,targetY-playerY)
        if(d<150f && !signalFound) {
            signalFound=true
            message="You found the signal. A hidden door opens..."
        } else if(!signalFound) {
            message="Too far from the signal. Keep exploring."
        }
    }

    override fun computeScroll() {
        val now=System.nanoTime()
        val dt=((now-lastTime)/1e9).toFloat().coerceAtMost(0.05f)
        lastTime=now
        if(screen==2 && joystickActive) {
            playerX=(playerX+joystickX*260f*dt).coerceIn(35f,worldW-35f)
            playerY=(playerY+joystickY*260f*dt).coerceIn(35f,worldH-35f)
            if(hypot(targetX-playerX,targetY-playerY)<75f && !signalFound)
                message="You're near it! Press A to inspect."
        }
    }

    private fun centerText(c:Canvas,s:String,y:Float,color:Int,size:Float,style:Int){
        text.color=color; text.textSize=size; text.typeface=Typeface.create("sans",style)
        c.drawText(s,(width-text.measureText(s))/2f,y,text)
    }
    private fun centerTextAt(c:Canvas,x:Float,y:Float,s:String,color:Int,size:Float,style:Int){
        text.color=color; text.textSize=size; text.typeface=Typeface.create("sans",style)
        c.drawText(s,x-text.measureText(s)/2f,y,text)
    }
    private fun leftText(c:Canvas,s:String,x:Float,y:Float,color:Int,size:Float,style:Int){
        text.color=color; text.textSize=size; text.typeface=Typeface.create("sans",style)
        c.drawText(s,x,y,text)
    }
    private fun roundButton(c:Canvas,l:Float,t:Float,r:Float,b:Float,s:String){
        p.color=Color.argb(235,8,15,25); c.drawRoundRect(l,t,r,b,24f,24f,p)
        p.style=Paint.Style.STROKE; p.strokeWidth=4f; p.color=Color.rgb(0,229,255)
        c.drawRoundRect(l,t,r,b,24f,24f,p); p.style=Paint.Style.FILL
        centerText(c,s,(t+b)/2f+8f,Color.WHITE,18f,Typeface.BOLD)
    }
}
