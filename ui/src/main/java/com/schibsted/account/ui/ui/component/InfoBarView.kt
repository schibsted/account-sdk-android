package com.schibsted.account.ui.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.schibsted.account.ui.R

class InfoBarView: LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var infoBarMessage: TextView


    init {
        val view = LayoutInflater.from(context).inflate(R.layout.schacc_info_bar_widget, this)
        infoBarMessage = view.findViewById(R.id.info_bar_message)
    }

    fun setMessage(text: Int) = infoBarMessage.setText(text)

}