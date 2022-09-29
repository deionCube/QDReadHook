package cn.xihan.qdds

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import cn.xihan.qdds.HookEntry.Companion.NOT_SUPPORT_OLD_LAYOUT_VERSION_CODE
import cn.xihan.qdds.HookEntry.Companion.optionEntity
import cn.xihan.qdds.HookEntry.Companion.versionCode
import com.alibaba.fastjson2.contains
import com.alibaba.fastjson2.parseObject
import com.alibaba.fastjson2.toJSONString
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit


/**
 * @项目名 : BaseHook
 * @作者 : MissYang
 * @创建时间 : 2022/7/4 16:32
 * @介绍 :
 */
@InjectYukiHookWithXposed(modulePackageName = "cn.xihan.qdds", entryClassName = "HookEntryInit")
class HookEntry : IYukiHookXposedInit {

    override fun onInit() {
        YukiHookAPI.configs {
            debugTag = "yuki"
            isDebug = BuildConfig.DEBUG
        }

    }

    override fun onHook() = YukiHookAPI.encase {

        loadApp(name = QD_PACKAGE_NAME) {

            //loggerE(msg = "authorList: ${authorList}\nbookNameList: ${bookNameList}\nbookTypeList:${bookTypeList}")

            if (optionEntity.mainOption.enableAutoSign) {
                autoSignIn(versionCode, optionEntity.mainOption.enableOldLayout)
            }

            if (optionEntity.mainOption.enableOldLayout && versionCode < NOT_SUPPORT_OLD_LAYOUT_VERSION_CODE) {
                enableOldLayout(versionCode)
            }

            if (optionEntity.mainOption.enableLocalCard) {
                enableLocalCard(versionCode)
            }

            if (optionEntity.mainOption.enableHideBottomDot) {
                hideBottomRedDot(versionCode)
            }

            if (optionEntity.mainOption.enableDisableQSNModeDialog) {
                removeQSNYDialog(versionCode)
            }

            if (optionEntity.advOption.enableDisableBookshelfActivityPopup) {
                disableBookshelfActivityPopup(versionCode)
            }

            if (optionEntity.advOption.enableDisableBookshelfFloat) {
                disableBookshelfFloatWindow(versionCode)
            }

            if (optionEntity.advOption.enableDisableBookshelfBottomAd) {
                disableBottomNavigationCenterAd(versionCode)
            }

            if (optionEntity.advOption.enableDisableAccountCenterAd) {
                disableAccountCenterAd(versionCode)
            }

            if (optionEntity.advOption.enableDisableCheckUpdate) {
                disableUpdate(versionCode)
            }

            if (optionEntity.advOption.enableDisableAdv) {
                disableAd(versionCode)
            }

            if (optionEntity.viewHideOption.enableHideBookshelfDailyReading) {
                hideBookshelfDailyReading(versionCode)
            }

            if (optionEntity.viewHideOption.enableSearchHideAllView) {
                hideSearchAllView(versionCode)
            }

            if (optionEntity.viewHideOption.enableHideMainBottomNavigationBarFind) {
                hideBottomNavigationFind(versionCode)
            }

            if (optionEntity.viewHideOption.accountOption.enableHideAccount) {
                accountViewHide(versionCode)
            }

            if (optionEntity.viewHideOption.bookDetailOptions.enableHideBookDetail) {
                bookDetailHide(versionCode)
            }


            splashPage(
                versionCode = versionCode,
                isEnableSplash = optionEntity.splashOption.enableSplash,
                isEnableCustomSplash = optionEntity.splashOption.enableCustomSplash
            )

            if (optionEntity.shieldOption.shieldOptionValueSet.isNotEmpty()) {
                shieldOption(versionCode, optionEntity.shieldOption.shieldOptionValueSet)
            }

            /**
             * 开启OkHttp3 日志拦截器
             */
            /*
            findClass("com.qidian.QDReader.framework.network.common.QDHttpLogInterceptor").hook {
                injectMember {
                    method {
                        name = "c"
                        param(BooleanType)
                    }
                    beforeHook{
                        args(0).setTrue()
                    }
                }
            }

             */

            /**
             * 调试-查看跳转关键词
             */
            /*
                        findClass("com.qidian.QDReader.other.ActionUrlProcess").hook {
                            /*
                            injectMember {
                                method {
                                    name = "processOpenBookListReborn"
                                    param(ContextClass, JSONObjectClass)
                                }
                                afterHook {
                                    printCallStack(instanceClass.name)
                                    val s = args[1] as? JSONObject
                                    loggerE(msg = "s: $s")
                                }
                            }

                             */

                            injectMember {
                                method {
                                    name = "processSinceV650"
                                    param(ContextClass, StringType, JSONObjectClass)
                                }
                                afterHook {
                                    //printCallStack(instance.javaClass.name)
                                    val s = args[1] as? String
                                    val jb = args[2] as? JSONObject
                                    loggerE(msg = "s: $s\njb: $jb")
                                }
                            }
                        }

             */

            findClass("com.qidian.QDReader.ui.activity.MoreActivity").hook {
                injectMember {
                    method {
                        name = "initWidget"
                        emptyParam()
                        returnType = UnitType
                    }
                    afterHook {
                        safeRun {
                            val readMoreSetting =
                                getView<RelativeLayout>(instance, "readMoreSetting")
                            // 获取 readMoreSetting 子控件
                            val readMoreSettingChild = readMoreSetting?.getChildAt(0) as? TextView
                            readMoreSettingChild?.text = "阅读设置/模块设置(长按)"

                            readMoreSetting?.setOnLongClickListener {
                                instance<Activity>().apply {
                                    safeRun {
                                        val linearLayout = CustomLinearLayout(this)
                                        val mainOptionTextView = CustomTextView(
                                            context = this, mText = "主设置", isBold = true
                                        ) {
                                            showMainOptionDialog()
                                        }
                                        val advOptionTextView = CustomTextView(
                                            context = this, mText = "广告相关设置", isBold = true
                                        ) {
                                            showAdvOptionDialog()
                                        }
                                        val shieldOptionTextView = CustomTextView(
                                            context = this, mText = "屏蔽相关设置", isBold = true
                                        ) {
                                            showShieldOptionDialog()
                                        }
                                        val splashOptionTextView = CustomTextView(
                                            context = this, mText = "闪屏相关设置", isBold = true
                                        ) {
                                            showSplashOptionDialog()
                                        }
                                        val viewHideOptionTextView = CustomTextView(
                                            context = this,
                                            mText = "隐藏控件相关设置",
                                            isBold = true
                                        ) {
                                            showHideOptionDialog()
                                        }
                                        val openSourceryOptionTextView = CustomTextView(
                                            context = this,
                                            mText = "开源地址及详细说明",
                                            isBold = true
                                        ) {
                                            val intent = Intent(Intent.ACTION_VIEW)
                                            intent.data =
                                                Uri.parse("https://github.com/xihan123/QDReadHook")
                                            startActivity(intent)
                                        }
                                        linearLayout.addView(mainOptionTextView)
                                        linearLayout.addView(advOptionTextView)
                                        linearLayout.addView(shieldOptionTextView)
                                        linearLayout.addView(splashOptionTextView)
                                        linearLayout.addView(viewHideOptionTextView)
                                        linearLayout.addView(openSourceryOptionTextView)

                                        alertDialog {
                                            title = "模块版本: ${BuildConfig.VERSION_NAME}"
                                            customView = linearLayout

                                            positiveButton("确定并重启起点") {
                                                restartApplication()
                                            }

                                            build()
                                            show()
                                        }
                                    }
                                }
                                true
                            }
                        }
                    }
                }
            }

            /**
             * 调试-打印返回数据
             */
            /*
            findClass("com.qidian.QDReader.framework.network.qd.QDHttpResp").hook {
                injectMember {
                    constructor {
                        param(BooleanType, IntType, IntType, StringType, LongType)
                    }
                    afterHook {
                        val s = args[3] as? String
                        if (!s.isNullOrBlank()) {
                            loggerE(msg = "5 data: $s")
                        }
                    }

                }

                injectMember {
                    constructor {
                        param(BooleanType, BitmapClass, StringType)
                    }
                    afterHook {
                        val s = args[2] as? String
                        if (!s.isNullOrBlank()) {
                            loggerE(msg = "3 data: $s")
                        }
                    }
                }
            }
             */


        }
    }


    companion object {

        /**
         * 起点包名
         */
        val QD_PACKAGE_NAME by lazy {
            optionEntity.mainOption.packageName
        }

        val versionCode by lazy { getApplicationVersionCode(QD_PACKAGE_NAME) }

        /**
         * 不支持旧版布局的版本号
         */
        const val NOT_SUPPORT_OLD_LAYOUT_VERSION_CODE = 800

        /**
         * 需要屏蔽的作者列表
         */
        val authorList by lazy {
            optionEntity.shieldOption.authorList
        }

        /**
         * 需要屏蔽的书名关键词列表
         */
        val bookNameList by lazy {
            optionEntity.shieldOption.bookNameList
        }

        /**
         * 需要屏蔽的书籍类型列表
         */
        val bookTypeList by lazy {
            optionEntity.shieldOption.bookTypeList
        }

        private val enableBookTypeEnhancedBlocking by lazy {
            optionEntity.shieldOption.enableBookTypeEnhancedBlocking
        }

        /**
         * 判断是否启用了屏蔽配置的选项
         * @param optionValue 选项的值
         */
        fun isEnableShieldOption(optionValue: Int) =
            optionValue in optionEntity.shieldOption.shieldOptionValueSet

        /**
         * 判断是否启用了书籍详情配置的选项
         */
        fun isEnableBookDetailOption(optionValue: Int) =
            optionEntity.viewHideOption.bookDetailOptions.configurationsOptionList[optionValue] in optionEntity.viewHideOption.bookDetailOptions.configurationsSelectedOptionList


        /**
         * 判断是否需要屏蔽
         * @param bookName 书名-可空
         * @param authorName 作者名-可空
         * @param bookType 书类型-可空
         */
        fun isNeedShield(
            bookName: String? = null, authorName: String? = null, bookType: Set<String>? = null
        ): Boolean {
            /*
            if (BuildConfig.DEBUG) {
                loggerE(msg = "bookName: $bookName\nauthorName:$authorName\nbookType:$bookType")
            }

             */
            if (bookNameList.isNotEmpty()) {
                if (!bookName.isNullOrBlank() && bookNameList.any { it in bookName }) {
                    return true
                }
            }
            if (authorList.isNotEmpty()) {
                if (!authorName.isNullOrBlank() && authorList.any { authorName == it }) {
                    return true
                }
            }
            if (bookTypeList.isNotEmpty() && !bookType.isNullOrEmpty()) {
                if (enableBookTypeEnhancedBlocking) {
                    if (bookType.isNotEmpty() && bookType.any { bookTypeList.any { it1 -> it1 in it } }) {
                        return true
                    }
                } else {
                    if (bookType.isNotEmpty() && bookType.any { it in bookTypeList }) {
                        return true
                    }
                }
            }
            return false
        }

        /**
         * 解析关键词组
         * @param it 关键词组
         */
        fun parseKeyWordOption(it: String = ""): List<String> {
            return try {
                if (it.isBlank()) {
                    emptyList()
                } else if (it.contains(";")) {
                    if (it.endsWith(";")) {
                        it.substring(0, it.length - 1).split(";").toList()
                    } else {
                        it.split(";").toList()
                    }
                } else {
                    listOf(it)
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

        /**
         * 解析需要屏蔽的书籍列表
         */
        fun parseNeedShieldList(list: MutableList<*>): List<*> {
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next().toJSONString()
                val jb = item.parseObject()
                val bookName = jb.getString("BookName") ?: jb.getString("bookName")
                val authorName = jb.getString("AuthorName") ?: jb.getString("authorName")
                val categoryName = jb.getString("CategoryName") ?: jb.getString("categoryName")
                val subCategoryName =
                    jb.getString("SubCategoryName") ?: jb.getString("subCategoryName")
                val tagName = jb.getString("TagName") ?: jb.getString("tagName")
                val array = jb.getJSONArray("AuthorTags") ?: jb.getJSONArray("tags")
                ?: jb.getJSONArray("Tags") ?: jb.getJSONArray("tagList")
                val bookTypeArray = mutableSetOf<String>()
                if (categoryName != null) {
                    bookTypeArray += categoryName
                }
                if (subCategoryName != null) {
                    bookTypeArray += subCategoryName
                }
                if (tagName != null) {
                    bookTypeArray += tagName
                }
                if (!array.isNullOrEmpty()) {
                    for (i in array.indices) {
                        array += array.getString(i)
                    }
                }
                if (isNeedShield(bookName, authorName, bookTypeArray)) {
                    iterator.remove()
                }
            }
            return list
        }

        val optionEntity = readOptionEntity()

    }

}

fun PackageParam.autoSignIn(
    versionCode: Int, isEnableOldLayout: Boolean = false
) {
    when {
        versionCode >= NOT_SUPPORT_OLD_LAYOUT_VERSION_CODE -> {
            newAutoSignIn(versionCode)
        }

        else -> {
            if (isEnableOldLayout) {
                oldAutoSignIn(versionCode)
            } else {
                newAutoSignIn(versionCode)
            }
        }
    }

}

/**
 * 老版布局自动签到
 */
fun PackageParam.oldAutoSignIn(versionCode: Int) {
    when (versionCode) {
        in 758..800 -> {
            findClass("com.qidian.QDReader.ui.view.bookshelfview.CheckInReadingTimeView").hook {
                injectMember {
                    method {
                        name = "S"
                    }
                    afterHook {
                        val m = getView<TextView>(
                            instance, "m"
                        )
                        val l = getView<LinearLayout>(
                            instance, "l"
                        )
                        m?.let { mtv ->
                            if (mtv.text == "签到" || mtv.text == "签到领奖") {
                                l?.performClick()
                            }
                        }
                    }
                }
            }
        }

        else -> loggerE(msg = "自动签到不支持的版本号为: $versionCode")
    }
}

/**
 * 新版布局自动签到
 */
fun PackageParam.newAutoSignIn(versionCode: Int) {
    when (versionCode) {
        in 758..812 -> {
            findClass("com.qidian.QDReader.ui.view.bookshelfview.CheckInReadingTimeViewNew").hook {
                injectMember {
                    method {
                        name = "E"
                    }
                    afterHook {
                        val s = getView<LinearLayout>(
                            instance, "s"
                        )
                        val qd = getParam<Any>(
                            instance, "s"
                        )
                        qd?.let { qdv ->
                            val e = getView<TextView>(
                                qdv, "e"
                            )
                            e?.let { etv ->
                                if (etv.text == "签到") {
                                    s?.performClick()
                                }
                            }
                        }
                    }
                }
            }
        }

        else -> loggerE(msg = "自动签到不支持的版本号为: $versionCode")
    }
}

/**
 * Hook 启用旧版布局
 */
fun PackageParam.enableOldLayout(versionCode: Int) {
    when (versionCode) {
        in 758..799 -> {
            findClass("com.qidian.QDReader.component.config.QDAppConfigHelper\$Companion").hook {
                injectMember {
                    method {
                        name = "getBookShelfNewStyle"
                    }
                    replaceToFalse()
                }
            }
        }

        else -> loggerE(msg = "启用旧版布局不支持的版本号为: $versionCode")
    }
}

/**
 * Hook 启用本地至尊卡
 */
fun PackageParam.enableLocalCard(versionCode: Int) {
    when (versionCode) {
        in 758..850 -> {

            findClass("com.qidian.QDReader.repository.entity.UserAccountDataBean\$MemberBean").hook {
                injectMember {
                    method {
                        name = "getMemberType"
                    }
                    replaceTo(2)
                }

                injectMember {
                    method {
                        name = "getIsMember"
                    }
                    replaceTo(1)
                }
            }

            findClass("com.qidian.QDReader.repository.entity.config.MemberBean").hook {
                injectMember {
                    method {
                        name = "getMemberType"
                    }
                    replaceTo(2)
                }

                injectMember {
                    method {
                        name = "isMember"
                    }
                    replaceTo(1)
                }
            }
        }

        else -> loggerE(msg = "启用本地至尊卡不支持的版本号为: $versionCode")
    }
}

/**
 * 主要配置弹框
 */
fun Context.showMainOptionDialog() {
    val linearLayout = CustomLinearLayout(this, isAutoWidth = false)
    val packageNameOption = CustomEditText(
        context = this,
        title = "包名设置",
        message = "一般默认即可,不建议更改",
        value = optionEntity.mainOption.packageName
    ) {
        optionEntity.mainOption.packageName = it
    }
    val enableAutoSignOption = CustomSwitch(
        context = this, title = "启用自动签到", isEnable = optionEntity.mainOption.enableAutoSign
    ) {
        optionEntity.mainOption.enableAutoSign = it
    }
    val enableLocalCardOption = CustomSwitch(
        context = this, title = "启用本地至尊卡", isEnable = optionEntity.mainOption.enableLocalCard
    ) {
        optionEntity.mainOption.enableLocalCard = it
    }

    linearLayout.addView(packageNameOption)
    linearLayout.addView(enableAutoSignOption)
    if (versionCode < NOT_SUPPORT_OLD_LAYOUT_VERSION_CODE) {
        val enableOldLayoutOption = CustomSwitch(
            context = this,
            title = "启用旧版布局",
            isEnable = optionEntity.mainOption.enableOldLayout
        ) {
            optionEntity.mainOption.enableOldLayout = it
        }
        linearLayout.addView(enableOldLayoutOption)
    }
    linearLayout.addView(enableLocalCardOption)


    alertDialog {
        title = "主要配置"
        customView = linearLayout
        okButton {
            updateOptionEntity()
        }
        negativeButton("返回") {
            it.dismiss()
        }

        build()
        show()
    }
}




