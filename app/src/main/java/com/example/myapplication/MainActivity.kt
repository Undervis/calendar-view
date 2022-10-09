package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.shuhart.materialcalendarview.CalendarDay
import com.shuhart.materialcalendarview.MaterialCalendarView
import com.squareup.picasso.Picasso
import com.shuhart.materialcalendarview.OnDateSelectedListener
import com.shuhart.materialcalendarview.indicator.MonthIndicator
import com.shuhart.materialcalendarview.indicator.pager.PagerIndicator


class MainActivity : AppCompatActivity(), OnDateSelectedListener {

    private var tvTitle: TextView? = null
    private var tvDescription: TextView? = null
    private var tvDate: TextView? = null
    private var imPhoto: ImageView? = null

    val dates = ArrayList<String>()
    val tittle = ArrayList<String>()
    val descriptions = ArrayList<String>()
    val imgs = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cal: MaterialCalendarView = findViewById(R.id.calendarView)
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        tvDate = findViewById(R.id.tvDate)
        imPhoto = findViewById(R.id.imPhoto)
        imPhoto!!.clipToOutline = true

        tvTitle!!.visibility = View.INVISIBLE
        tvDate!!.visibility = View.INVISIBLE
        tvDescription!!.visibility = View.INVISIBLE
        imPhoto!!.visibility = View.INVISIBLE

        val database = Firebase.database
        val myRef = database.getReference("date")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (dates.size>0)
//                    dates.clear()
                for (i in dataSnapshot.children) {
                    val fbe = i.getValue(fb().javaClass)
                    dates.add(fbe?.getDates().toString())
                    tittle.add(fbe?.getTitles().toString())
                    descriptions.add(fbe?.getDescription().toString())
                    imgs.add(fbe?.getImage().toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        Log.i("abobus", cal.monthIndicator.toString())

        cal.selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE
        cal.addOnDateChangedListener(this)
    }

    override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {

        val m = date.month + 1

        tvTitle!!.visibility = View.INVISIBLE
        tvDate!!.visibility = View.INVISIBLE
        tvDescription!!.visibility = View.INVISIBLE
        imPhoto!!.visibility = View.INVISIBLE

        if (!isOnline(this@MainActivity)) {
            Toast.makeText(this, "Подключитесь к интернету", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            getDates("${date.day}.$m", dates, tittle, descriptions, imgs)
        } catch (E: IndexOutOfBoundsException) {
            Toast.makeText(this@MainActivity, "Ошибка: $E", Toast.LENGTH_SHORT)
                .show()
            return
        }

        try {

            for (i in m..12) {
                for (k in date.day + 1..31) {
                    if (getAdvice("$k.$m", dates)) {
                        Toast.makeText(this@MainActivity, "Ближайшее событие: $k.$m", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }
                }
            }
        } catch (E: IndexOutOfBoundsException) {
            return
        }
    }

    fun getAdvice(
        key: String,
        list: ArrayList<String>
    ): Boolean {
        for (i in 0..list.size - 1) {
            val open = list[i].dropLastWhile { it != '.' }.dropLast(1)
            if (open == key)
                return true
        }
        return false
    }

    fun getDates(
        key: String,
        dates: ArrayList<String>,
        title: ArrayList<String>,
        desc: ArrayList<String>,
        imgs: ArrayList<String>
    ) {
        for (i in 0..dates.size - 1) {
            val open = dates[i].dropLastWhile { it != '.' }.dropLast(1)
            if (open == key) {
                val str = sentData(dates[i].dropLastWhile { it != '.' }.dropLast(1)
                    .dropLastWhile { it != '.' }.dropLast(1).toInt(),
                    dates[i].dropLastWhile { it != '.' }.dropLast(1).dropWhile { it != '.' }.drop(1)
                        .toInt(),
                    dates[i].dropWhile { it != '.' }.drop(1).dropWhile { it != '.' }.drop(1).toInt()
                )
                tvTitle!!.visibility = View.VISIBLE
                tvDate!!.visibility = View.VISIBLE
                tvDescription!!.visibility = View.VISIBLE

                tvTitle!!.text = title[i].capitalize()
                tvDescription!!.text = desc[i].capitalize()
                tvDate!!.text = str

                Log.i("aboba", imgs[i])

                if (imgs[i] != "null") {
                    imPhoto!!.visibility = View.VISIBLE
                    try {
                        Picasso.get().load(imgs[i]).placeholder(R.drawable.img)
                            .error(R.drawable.img).into(imPhoto)
                    } catch(E: java.lang.IllegalArgumentException) {
                        imPhoto!!.visibility = View.INVISIBLE
                        Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
                        return
                    }

                }
            }
        }
    }

    fun sentData(day: Int, mounth: Int, year: Int): String {
        var str = ""
        if (day < 10)
            str += "0${day.toString()}."
        else
            str += "$day."

        if (mounth < 10)
            str += "0${mounth.toString()}."
        else
            str += "$mounth."

        str += year.toString()

        return str
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var activeNetworkInfo: NetworkInfo? = null
        activeNetworkInfo = cm.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }
}
