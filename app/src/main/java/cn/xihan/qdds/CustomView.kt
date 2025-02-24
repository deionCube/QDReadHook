package cn.xihan.qdds

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import androidx.core.widget.addTextChangedListener


/**
 * @项目名 : QDReadHook
 * @作者 : MissYang
 * @创建时间 : 2022/8/25 16:30
 * @介绍 :
 */
/**
 * 创建一个 自定义LinearLayout
 * @param context 上下文
 * @param isAutoWidth 是否宽度自适应
 * @param isAutoHeight 是否高度自适应
 */
open class CustomLinearLayout(
    context: Context,
    isAutoWidth: Boolean = true,
    isAutoHeight: Boolean = true
) : LinearLayout(context) {

    /**
     * 设定自定义 LinearLayout 的布局参数
     */
    init {
        apply {
            layoutParams = LayoutParams(
                if (isAutoWidth) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT,
                if (isAutoHeight) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT
            )
            orientation = VERTICAL
            setPadding(
                context.dp2px(10F), context.dp2px(10F), context.dp2px(10F), context.dp2px(10F)
            )
        }
    }
}


/**
 * 创建一个自定义Switch
 * @param context 上下文
 * @param title 标题
 * @param isEnable 是否选中
 * @param isAvailable 是可用
 * @param isAutoWidth 是否宽度自适应
 * @param isAutoHeight 是否高度自适应
 * @param block 事件执行方法
 */
class CustomSwitch(
    context: Context,
    title: String = "",
    isEnable: Boolean = false,
    isAvailable: Boolean = true,
    isAutoWidth: Boolean = true,
    isAutoHeight: Boolean = true,
    block: (Boolean) -> Unit
) : Switch(context) {

    init {
        apply {
            text = title
            isChecked = isEnable
            isEnabled = isAvailable
            setOnCheckedChangeListener { _, b -> block(b) }
            layoutParams = LinearLayout.LayoutParams(
                if (isAutoWidth) LinearLayout.LayoutParams.WRAP_CONTENT else LinearLayout.LayoutParams.MATCH_PARENT,
                if (isAutoHeight) LinearLayout.LayoutParams.WRAP_CONTENT else LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(
                context.dp2px(10F), context.dp2px(10F), context.dp2px(10F), context.dp2px(10F)
            )
        }
    }
}

/**
 * 创建一个自定义EditText
 * @param context 上下文
 * @param title 标题
 * @param message 提示信息
 * @param value 值
 * @param isAvailable 是可用
 * @param isAutoWidth 是否宽度自适应
 * @param isAutoHeight 是否高度自适应
 * @param block 事件执行方法
 */
@SuppressLint("SetTextI18n")
class CustomEditText(
    context: Context,
    title: String = "",
    message: String = "",
    value: String? = "",
    mHint: String = "",
    isAvailable: Boolean = true,
    isAutoWidth: Boolean = false,
    isAutoHeight: Boolean = true,
    block: (String) -> Unit
) : CustomLinearLayout(context, isAutoWidth, isAutoHeight) {

    var editText: EditText

    init {
        if (title.isNotBlank() && message.isNotBlank()) {
            val textView = TextView(context).apply {
                text = "$title\n$message"
                textSize = 16F
                setTypeface(typeface, Typeface.BOLD)
            }
            addView(textView)
        }
        editText = EditText(context).apply {
            isEnabled = isAvailable
            if (value.isNullOrBlank() && mHint.isNotBlank()) {
                hint = mHint
            } else {
                setText(value)
            }
            addTextChangedListener {
                block(it.toString())
            }
        }
        apply {
            addView(editText)
            setPadding(
                context.dp2px(10F), context.dp2px(10F), context.dp2px(10F), context.dp2px(10F)
            )
        }
    }

    /**
     * 获取输入框的值
     */
    fun getText(): String = editText.text.toString()
}

/**
 * 创建一个自定义TextView
 * @param context 上下文
 * @param mText 标题
 * @param mTextSize 文本大小
 * @param isBold 是否加粗
 * @param isAutoWidth 是否宽度自适应
 * @param isAutoHeight 是否高度自适应
 * @param onClickAction 事件执行方法
 */
@SuppressLint("AppCompatCustomView")
class CustomTextView(
    context: Context,
    mText: String = "",
    mTextSize: Float = 16F,
    isBold: Boolean = false,
    isAutoWidth: Boolean = false,
    isAutoHeight: Boolean = true,
    onClickAction: () -> Unit
) : CustomLinearLayout(context, isAutoWidth, isAutoHeight) {

    init {
        val textView = TextView(context).apply {
            text = mText
            textSize = mTextSize
            if (isBold) {
                setTypeface(typeface, Typeface.BOLD)
            }
            setOnClickListener {
                onClickAction()
            }
        }
        apply {
            addView(textView)
            layoutParams = LayoutParams(
                if (isAutoWidth) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT,
                if (isAutoHeight) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT
            )
            setPadding(
                context.dp2px(10F), context.dp2px(10F), context.dp2px(10F), context.dp2px(10F)
            )
        }
    }

}

/**
 * 创建一个自定义Button
 * @param context 上下文
 * @param mText 标题
 * @param mTextSize 文本大小
 * @param isBold 是否加粗
 * @param isAutoWidth 是否宽度自适应
 * @param isAutoHeight 是否高度自适应
 * @param onClickAction 事件执行方法
 */
class CustomButton(
    context: Context,
    mText: String = "",
    mTextSize: Float = 16F,
    isBold: Boolean = false,
    isAutoWidth: Boolean = false,
    isAutoHeight: Boolean = true,
    onClickAction: () -> Unit
) : CustomLinearLayout(context, isAutoWidth, isAutoHeight) {

    init {
        val button = Button(context).apply {
            text = mText
            textSize = mTextSize
            if (isBold) {
                setTypeface(typeface, Typeface.BOLD)
            }
            setOnClickListener {
                onClickAction()
            }
        }
        apply {
            addView(button)
            layoutParams = LayoutParams(
                if (isAutoWidth) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT,
                if (isAutoHeight) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT
            )
            setPadding(
                context.dp2px(10F), context.dp2px(10F), context.dp2px(10F), context.dp2px(10F)
            )
        }
    }

}

/**
 * 创建一个自定义 ListView
 * @param context 上下文
 * @param isAutoWidth 是否宽度自适应
 * @param isAutoHeight 是否高度自适应
 * @param onItemClickListener item点击使劲
 * @param onItemLongClickListener item长按事件
 * @param listData 数据
 */
class CustomListView(
    context: Context,
    isAutoWidth: Boolean = false,
    isAutoHeight: Boolean = true,
    onItemClickListener: (CustomListView, position: Int) -> Unit,
    onItemLongClickListener: (CustomListView, position: Int) -> Unit,
    listData: List<String>
) : CustomLinearLayout(context, isAutoWidth, isAutoHeight) {

    var listView: ListView
    var mAdapter: ArrayAdapter<String>

    init {
        mAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, listData)
        listView = ListView(context).apply {
            adapter = mAdapter

            setOnItemClickListener { _, _, i, _ ->
                onItemClickListener(this@CustomListView, i)
            }
            setOnItemLongClickListener { _, _, i, _ ->
                onItemLongClickListener(this@CustomListView, i)
                true
            }
        }
        apply {
            addView(listView)
            layoutParams = LayoutParams(
                if (isAutoWidth) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT,
                if (isAutoHeight) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT
            )
            setPadding(
                context.dp2px(10F), context.dp2px(10F), context.dp2px(10F), context.dp2px(10F)
            )
        }
    }

    /**
     * 更新数据
     */
    fun updateListData(listData: List<String>) {
        mAdapter.clear()
        mAdapter.addAll(listData)
        mAdapter.notifyDataSetChanged()
    }

    /**
     * 删除指定Item
     * @param position 位置
     */
    fun removeItem(position: Int) {
        mAdapter.remove(mAdapter.getItem(position))
        mAdapter.notifyDataSetChanged()
    }
}

