// Copyright (c) 2024, Circle Internet Financial, LTD. All rights reserved.
//
// SPDX-License-Identifier: Apache-2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.circle.w3s.sample.wallet.ui

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.circle.w3s.sample.wallet.R

class NecessaryTextView : AppCompatTextView {
    private var necessary = false

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NecessaryTextView)
        necessary = typedArray.getBoolean(R.styleable.NecessaryTextView_necessary, false)
        typedArray.recycle()
        setText(text, null)
    }

    fun setNecessary(necessary: Boolean) {
        this.necessary = necessary
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (necessary) {
            val span = SpannableString("$text *")
            if (text != null && !TextUtils.isEmpty(text)) {
                span.setSpan(
                    ForegroundColorSpan(Color.RED),
                    text.length + 1,
                    text.length + 2,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            super.setText(span, type)
        } else {
            super.setText(text, type)
        }
    }
}