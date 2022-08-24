package cn.xihan.qdds

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.annotation.Keep
import cn.xihan.qdds.HookEntry.Companion.isEnableOption
import cn.xihan.qdds.HookEntry.Companion.isNeedShield
import com.alibaba.fastjson2.parseObject
import com.alibaba.fastjson2.toJSONString
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.android.ActivityClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.*
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File


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

            safeRun {
                optionEntity?.let {
                    if (it.mainOption.enableAutoSign) {
                        autoSignIn(versionCode)
                    }
                    if (it.mainOption.enableOldLayout) {
                        enableOldLayout(versionCode)
                    }
                    if (it.mainOption.enableLocalCard) {
                        enableLocalCard(versionCode)
                    }
                    if (it.mainOption.enableHideBottomDot) {
                        hideBottomRedDot(versionCode)
                    }
                    if (it.mainOption.enableDisableQSNModeDialog) {
                        removeQSNYDialog(versionCode)
                    }

                    if (it.advOption.enableRemoveBookshelfFloat) {
                        removeBookshelfFloatWindow(versionCode)
                    }
                    if (it.advOption.enableRemoveBookshelfBottomAd) {
                        removeBottomNavigationCenterAd(versionCode)
                    }
                    if (it.advOption.enableDisableCheckUpdate) {
                        removeUpdate(versionCode)
                    }
                    if (it.advOption.enableDisableAdv) {
                        disableAd(versionCode)
                    }

                    if (it.splashOption.enableSplash) {
                        if (it.splashOption.enableCustomSplash) {
                            // TODO: 自定义闪屏页暂不支持
                        }
                    } else {
                        disableSplash(versionCode)
                    }
                }
            }

            if (prefs.getBoolean("isEnableAutoSign")) {
                autoSignIn(versionCode)
            }

            if (prefs.getBoolean("isEnableOldLayout")) {
                enableOldLayout(versionCode)
            }

            if (prefs.getBoolean("isEnableLocalCard")) {
                enableLocalCard(versionCode)
            }

            if (prefs.getBoolean("isEnableRemoveBookshelfFloat", BuildConfig.DEBUG)) {
                removeBookshelfFloatWindow(versionCode)
            }

            if (prefs.getBoolean("isEnableRemoveBookshelfBottomAd", BuildConfig.DEBUG)) {
                removeBottomNavigationCenterAd(versionCode)
            }

            if (prefs.getBoolean("isEnableDisableAd", BuildConfig.DEBUG)) {
                disableAd(versionCode)
            }

            splashPage(versionCode)

            if (prefs.getBoolean("isEnableHideBottomDot")) {
                hideBottomRedDot(versionCode)
            }

            if (prefs.getBoolean("isEnableCloseQSNModeDialog")) {
                removeQSNYDialog(versionCode)
            }

            if (prefs.getBoolean("isEnableRemoveUpdate", BuildConfig.DEBUG)) {
                removeUpdate(versionCode)
            }

            if (optionList.isNotEmpty()) {
                shieldOption(versionCode, optionList)
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

        }


    }


    companion object {

        /**
         * 起点包名
         */
        val QD_PACKAGE_NAME by lazy {
            getPref()?.getString("packageName", "com.qidian.QDReader") ?: "com.qidian.QDReader"
        }

        val versionCode by lazy { getApplicationVersionCode(QD_PACKAGE_NAME) }

        /**
         * 需要屏蔽的作者列表
         */
        val authorList by lazy {
            val s = getPref()?.getString("AuthorListData", "")
            if (s.isNullOrBlank()) {
                return@lazy optionEntity?.shieldOption?.authorList ?: emptyList()
            }
            if (s.contains(";")) {
                return@lazy s.split(";")
            } else {
                listOf(s)
            }
        }

        /**
         * 需要屏蔽的书名关键词列表
         */
        val bookNameList by lazy {
            val s = getPref()?.getString("BookNameListData", "")
            if (s.isNullOrBlank()) {
                return@lazy optionEntity?.shieldOption?.bookNameList ?: emptyList()
                //return@lazy emptyList()
            }
            if (s.contains(";")) {
                return@lazy s.split(";")
            } else {
                listOf(s)
            }
        }

        /**
         * 需要屏蔽的书籍类型列表
         */
        val bookTypeList by lazy {
            val s = getPref()?.getString("BookTypeListData", "")
            if (s.isNullOrBlank()) {
                return@lazy optionEntity?.shieldOption?.bookTypeList ?: emptyList()
                //return@lazy emptyList()
            }
            if (s.contains(";")) {
                return@lazy s.split(";")
            } else {
                listOf(s)
            }
        }

        /**
         * 配置相关的选项
         */
        val optionList by lazy {
            getPref()?.getStringSet("optionListData", setOf())
                ?: optionEntity?.shieldOption?.shieldOptionValueSet ?: setOf()
        }

        /**
         * 判断是否启用了该选项
         * @param optionValue 选项的值
         */
        fun isEnableOption(optionValue: String) = optionList.any { it == optionValue }

        /**
         * 判断是否需要屏蔽
         * @param bookName 书名-可空
         * @param authorName 作者名-可空
         * @param bookType 书类型-可空
         */
        fun isNeedShield(bookName: String?, authorName: String?, bookType: Set<String>?): Boolean {
            //loggerE(msg = "bookName: $bookName\nauthorName:$authorName\nbookType:$bookType")
            if (!bookName.isNullOrBlank() && bookNameList.any { it in bookName }) {
                return true
            }
            if (!authorName.isNullOrBlank() && authorList.any { authorName == it }) {
                return true
            }
            if (!bookType.isNullOrEmpty() && bookType.any { it in bookTypeList }) {
                return true
            }
            return false
        }

        fun getPref(): SharedPreferences? {
            val pref = XSharedPreferences(BuildConfig.APPLICATION_ID)
            return if (pref.file.canRead()) pref else null
        }

        val optionEntity = readOptionEntity()

    }

}


/**
 * 通过反射获取控件
 * @param param 参数
 * @param name 字段名
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T : View> getView(param: Any, name: String): T? {
    return getParam<T>(param, name)
}

/**
 * 反射获取任何类型
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T> getParam(param: Any, name: String): T? {
    val clazz: Class<*> = param.javaClass
    val field = clazz.getDeclaredField(name)
    field.isAccessible = true
    return field[param] as? T
}

/**
 * 利用 Reflection 获取当前的系统 Context
 */
fun getSystemContext(): Context {
    val activityThreadClass = findClass("android.app.ActivityThread", null)
    val activityThread = callStaticMethod(activityThreadClass, "currentActivityThread")
    val context = callMethod(activityThread, "getSystemContext") as? Context
    return context ?: throw Error("Failed to get system context.")
}

/**
 * 获取指定应用的 APK 路径
 */
fun getApplicationApkPath(packageName: String): String {
    val pm = getSystemContext().packageManager
    val apkPath = pm.getApplicationInfo(packageName, 0).publicSourceDir
    return apkPath ?: throw Error("Failed to get the APK path of $packageName")
}

/**
 * 获取指定应用的版本号
 */
fun getApplicationVersionCode(packageName: String): Int {
    val pm = getSystemContext().packageManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        pm.getPackageInfo(packageName, 0).longVersionCode.toInt()
    } else {
        pm.getPackageInfo(packageName, 0).versionCode
    }
}

/**
 * 打印当前调用栈
 */
fun printCallStack(className: String = "") {
    loggerE(msg = "className: $className")
    loggerE(msg = "Dump Stack: ---------------start----------------")
    val ex = Throwable()
    val stackElements = ex.stackTrace
    stackElements.forEachIndexed { index, stackTraceElement ->
        loggerE(msg = "Dump Stack: $index: $stackTraceElement")
    }
    loggerE(msg = "Dump Stack: ---------------end----------------")
}

/**
 * 容错安全运行方法
 */
fun safeRun(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        loggerE(msg = "safeRun 报错: ${e.message}")
    }
}

/**
 * 写入测试文本
 */
fun String.write(fileName: String = "test") {
    // 如果文件名已存在 则文件名 + 1
    var index = 0
    while (File(
            "${Environment.getExternalStorageDirectory().path}/MT2/apks/起点", "$fileName-$index.txt"
        ).exists()
    ) {
        index++
    }
    File(
        "${Environment.getExternalStorageDirectory().path}/MT2/apks/起点", "$fileName-$index.txt"
    ).writeText(this)
}

fun String.writeTextFile(fileName: String = "test") {
    var index = 0
    while (File(
            "${Environment.getExternalStorageDirectory().absolutePath}/QDReader",
            "$fileName-$index.txt"
        ).exists()
    ) {
        index++
    }
    File(
        "${Environment.getExternalStorageDirectory().absolutePath}/QDReader", "$fileName-$index.txt"
    ).writeText(this)
}

fun PackageParam.autoSignIn(versionCode: Int) {
    if (prefs.getBoolean("isEnableOldLayout")) {
        oldAutoSignIn(versionCode)
    } else {
        newAutoSignIn(versionCode)
    }
}

/**
 * 老版布局自动签到
 */
fun PackageParam.oldAutoSignIn(versionCode: Int) {
    when (versionCode) {
        in 758..788 -> {
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
        in 758..788 -> {
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
        in 758..800 -> {
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
        in 758..800 -> {

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
        }
        else -> loggerE(msg = "启用本地至尊卡不支持的版本号为: $versionCode")
    }

}

/**
 * Hook 移除书架右下角浮窗
 */
fun PackageParam.removeBookshelfFloatWindow(versionCode: Int) {
    when (versionCode) {
        in 758..768 -> {
            findClass("com.qidian.QDReader.ui.fragment.QDBookShelfPagerFragment").hook {
                injectMember {
                    method {
                        name = "loadBookShelfAd"
                    }
                    intercept()
                }

                injectMember {
                    method {
                        name = "onViewInject"
                        param(View::class.java)
                    }
                    afterHook {
                        val imgAdIconClose = getView<ImageView>(
                            instance, "imgAdIconClose"
                        )
                        imgAdIconClose?.visibility = View.GONE
                        val layoutImgAdIcon = getView<LinearLayout>(
                            instance, "layoutImgAdIcon"
                        )
                        layoutImgAdIcon?.visibility = View.GONE

                        val imgBookShelfActivityIcon = getView<ImageView>(
                            instance, "imgBookShelfActivityIcon"
                        )
                        imgBookShelfActivityIcon?.visibility = View.GONE
                    }
                }
            }
        }
        in 772..800 -> {
            findClass("com.qidian.QDReader.ui.fragment.QDBookShelfPagerFragment").hook {
                injectMember {
                    method {
                        name = "loadBookShelfAd"
                    }
                    intercept()
                }

                injectMember {
                    method {
                        name = "showBookShelfHoverAd"
                    }
                    intercept()
                }

                injectMember {
                    method {
                        name = "onViewInject"
                        param(View::class.java)
                    }
                    afterHook {
                        val layoutImgAdIcon = getView<LinearLayout>(
                            instance, "layoutImgAdIcon"
                        )
                        layoutImgAdIcon?.visibility = View.GONE
                    }
                }

            }
        }
        else -> {
            loggerE(msg = "移除书架右下角浮窗不支持的版本号为: $versionCode")
        }
    }
}

/**
 * Hook 移除底部导航栏中心广告
 * 上级调用位置:com.qidian.QDReader.ui.activity.MainGroupActivity.checkAdTab()
 */
fun PackageParam.removeBottomNavigationCenterAd(versionCode: Int) {
    when (versionCode) {
        in 758..800 -> {
            findClass("com.qidian.QDReader.ui.activity.MainGroupActivity\$t").hook {
                injectMember {
                    method {
                        name = "c"
                    }
                    intercept()
                }
            }
        }
        else -> loggerE(msg = "移除底部导航栏中心广告不支持的版本号为: $versionCode")
    }
}

/**
 * Hook 禁用广告
 */
fun PackageParam.disableAd(versionCode: Int) {
    when (versionCode) {
        in 758..800 -> {
            findClass("com.qq.e.comm.constants.CustomPkgConstants").hook {
                injectMember {
                    method {
                        name = "getAssetPluginName"
                    }
                    replaceTo("")
                }
            }

            findClass("com.qq.e.comm.b").hook {
                injectMember {
                    method {
                        name = "a"
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.start.AsyncMainGDTTask").hook {
                injectMember {
                    method {
                        name = "create"
                        returnType = StringType
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.start.AsyncMainGameADSDKTask").hook {
                injectMember {
                    method {
                        name = "create"
                        returnType = StringType
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.component.api.a").hook {
                injectMember {
                    method {
                        name = "b"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            // TODO 首页横幅广告:/argus/api/v2/adv/getadvlistbatch

        }
        else -> loggerE(msg = "禁用广告不支持的版本号为: $versionCode")
    }
}

/**
 * Hook 闪屏页相关
 */
fun PackageParam.splashPage(versionCode: Int) {
    if (prefs.getBoolean("isEnableSplash")) {
        if (prefs.getBoolean("isEnableCustomSplash")) {
            enableCustomSplash(versionCode)
        }
    } else {
        disableSplash(versionCode)
    }
}

/**
 * 关闭闪屏页
 */
fun PackageParam.disableSplash(versionCode: Int) {
    when (versionCode) {
        in 758..800 -> {
            findClass("com.qidian.QDReader.bll.splash.SplashManager").hook {
                injectMember {
                    method {
                        name = "k"
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.ui.activity.SplashImageActivity").hook {
                injectMember {
                    method {
                        name = "showSplashImage"
                        param(StringType)
                    }
                    afterHook {
                        val mSplashHelper = getParam<Any>(instance, "mSplashHelper")
                        mSplashHelper?.current {
                            method {
                                name = "e"
                            }.call()

                        }
                    }
                }
            }
        }
        else -> loggerE(msg = "闪屏页不支持的版本号为: $versionCode")
    }
}

/**
 * 启用自定义闪屏页
 */
fun PackageParam.enableCustomSplash(versionCode: Int) {
    when (versionCode) {
        in 758..800 -> {
            findClass("com.qidian.QDReader.ui.activity.SplashImageActivity").hook {
                if (!prefs.getBoolean("isEnableCustomSplashImageShowAllButton")) {
                    injectMember {
                        method {
                            name = "onCreate"
                        }
                        afterHook {
                            val btnSkip = getView<Button>(instance, "btnSkip")
                            btnSkip?.visibility = View.GONE
                            val ivTop = getView<ImageView>(instance, "ivTop")
                            ivTop?.visibility = View.GONE
                            val layoutShadow = getParam<RelativeLayout>(instance, "layoutShadow")
                            layoutShadow?.visibility = View.GONE
                            val mGotoActivityShimmer =
                                getView<FrameLayout>(instance, "mGotoActivityShimmer")
                            mGotoActivityShimmer?.visibility = View.GONE
                        }
                    }
                }
                injectMember {
                    method {
                        name = "start"
                        param(
                            "com.qidian.QDReader.ui.activity.SplashActivity".clazz,
                            StringType,
                            StringType,
                            LongType,
                            IntType
                        )
                    }

                    beforeHook {
                        val customSplashImageFilePath = prefs.getString("customSplashImageFilePath")
                        if (customSplashImageFilePath.isNotBlank()) {
                            args(index = 1).set(customSplashImageFilePath)
                        }
                        val customBookId = prefs.getString("customBookId")
                        if (customBookId.isNotBlank()) {
                            args(index = 2).set("QDReader://ShowBook/$customBookId")
                        }

                        args(index = 4).set(prefs.getInt("customSplashImageType", 0))
                    }

                    afterHook {
                        // 打印传入的参数
                        //loggerE(msg = " \nargs[1]: ${args[1] as String} \nargs[2]: ${args[2] as String} \nargs[3]: ${args[3] as Long} \nargs[4]: ${args[4] as Int}")
                    }


                }
            }
        }
        else -> loggerE(msg = "闪屏页不支持的版本号为: $versionCode")
    }
}

/**
 * Hook 启用隐藏底部小红点
 * 上级调用位置:com.qidian.QDReader.ui.widget.maintab.PagerSlidingTabStrip.s()
 */
fun PackageParam.hideBottomRedDot(versionCode: Int) {
    when (versionCode) {
        in 758..768 -> {
            findClass("com.qidian.QDReader.ui.widget.maintab.a").hook {
                injectMember {
                    method {
                        name = "h"
                        returnType = IntType
                    }
                    replaceTo(1)
                }
            }
        }
        in 772..800 -> {
            findClass("com.qidian.QDReader.ui.widget.maintab.e").hook {
                injectMember {
                    method {
                        name = "h"
                        returnType = IntType
                    }
                    replaceTo(1)
                }
            }
        }
        else -> loggerE(msg = "隐藏底部小红点不支持的版本号为: $versionCode")
    }
}

/**
 * 移除青少年模式弹框
 */
fun PackageParam.removeQSNYDialog(versionCode: Int) {
    findClass("com.qidian.QDReader.bll.manager.QDTeenagerManager").hook {
        injectMember {
            method {
                name = "isTeenLimitShouldShow"
                param(IntType)
                returnType = BooleanType
            }
            replaceToFalse()
        }

        injectMember {
            method {
                name = "judgeTeenUserTimeLimit\$lambda-3\$lambda-2"
                param(ActivityClass)
                returnType = UnitType
            }
            intercept()
        }
    }
    /**
     * 上级调用位置:com.qidian.QDReader.bll.manager.QDTeenagerManager.teenWorkDialog
     */
    val dialogClassName: String? = when (versionCode) {
        in 758..768 -> "com.qidian.QDReader.bll.helper.v1"
        772 -> "com.qidian.QDReader.bll.helper.w1"
        in 776..788 -> "com.qidian.QDReader.bll.helper.t1"
        else -> null
    }
    dialogClassName?.hook {
        injectMember {
            method {
                name = "show"
                superClass()
            }
            intercept()
        }
    } ?: loggerE(msg = "移除青少年模式弹框不支持的版本号为: $versionCode")
}

/**
 * 禁用检查更新
 */
fun PackageParam.removeUpdate(versionCode: Int) {
    when (versionCode) {
        in 758..788 -> {

            findClass("w4.h").hook {
                injectMember {
                    method {
                        name = "l"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.ui.activity.MainGroupActivity").hook {
                injectMember {
                    method {
                        name = "checkUpdate"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.ui.fragment.QDFeedListPagerFragment").hook {
                injectMember {
                    method {
                        name = "checkAppUpdate"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            findClass("com.tencent.upgrade.core.UpdateCheckProcessor").hook {
                injectMember {
                    method {
                        name = "checkAppUpgrade"
                        returnType = Unit
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.util.z4").hook {
                injectMember {
                    method {
                        name = "b"
                        returnType = UnitType
                    }
                    intercept()
                }

                injectMember {
                    method {
                        name = "a"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            findClass("com.tencent.upgrade.core.UpgradeManager").hook {
                injectMember {
                    method {
                        name = "init"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.ui.activity.AboutActivity").hook {
                injectMember {
                    method {
                        name = "updateVersion"
                        returnType = UnitType
                    }
                    intercept()
                }

                injectMember {
                    method {
                        name = "getVersionNew"
                        returnType = UnitType
                    }
                    intercept()
                }


            }


        }
        else -> loggerE(msg = "禁用检查更新不支持的版本号为: $versionCode")
    }
}

/**
 * 屏蔽选项
 * @param versionCode 版本号
 * @param optionValueSet 屏蔽选项值
 */
fun PackageParam.shieldOption(versionCode: Int, optionValueSet: Set<String>) {
    // 遍历 optionValueSet 包含的值 执行指定方法
    optionValueSet.forEach {
        when (it) {
            "0" -> shieldSearchFind(versionCode)
            "3" -> shieldSearchRecommend(versionCode)
            "4" -> shieldChoice(versionCode)
            "5" -> shieldCategory(versionCode)
            "6" -> shieldCategoryAllBook(versionCode)
            "7" -> shieldFreeRecommend(versionCode)
            "8" -> shieldFreeNewBook(versionCode)
            "9" -> shieldHotAndRecommend(versionCode)
            "10" -> shieldNewBookAndRecommend(versionCode)
            "11" -> shieldDailyReading(versionCode)
        }
    }
    shieldSearch(versionCode, isEnableOption("1"), isEnableOption("2"))
}

/**
 * 屏蔽每日导读指定的书籍
 */
fun PackageParam.shieldDailyReading(versionCode: Int) {
    when (versionCode) {
        788 -> {
            findClass("com.qidian.QDReader.component.api.b1").hook {
                injectMember {
                    method {
                        name = "j"
                        emptyParam()
                        returnType = ArrayListClass
                    }

                    afterHook {
                        val list = result as? ArrayList<*>
                        list?.let {
                            safeRun {
                                val iterator = it.iterator()
                                while (iterator.hasNext()) {
                                    val item = iterator.next().toJSONString()
                                    val jb = item.parseObject()
                                    val bookName = jb.getString("BookName")
                                    val authorName = jb.getString("AuthorName")
                                    val categoryName = jb.getString("CategoryName")
                                    val subCategoryName = jb.getString("SubCategoryName")
                                    val array = jb.getJSONArray("AuthorTags")
                                    val bookTypeArray = mutableSetOf(categoryName, subCategoryName)
                                    if (!array.isNullOrEmpty()) {
                                        for (i in array.indices) {
                                            array += array.getString(i)
                                        }
                                    }
                                    val isNeedShield = isNeedShield(
                                        bookName = bookName,
                                        authorName = authorName,
                                        bookType = bookTypeArray
                                    )
                                    if (isNeedShield) {
                                        iterator.remove()
                                    }
                                }
                            }
                            result = list
                        }

                    }
                }
            }
        }
        else -> loggerE(msg = "屏蔽每日导读不支持的版本号为: $versionCode")
    }
}

/**
 * 屏蔽精选主页面
 */
fun PackageParam.shieldChoice(versionCode: Int) {
    when (versionCode) {
        788 -> {
            /**
             * 精选主页面
             */
            findClass("com.qidian.QDReader.repository.entity.BookListData").hook {
                injectMember {
                    method {
                        name = "getItems"
                        returnType = ListClass
                    }
                    afterHook {
                        val list = getParam<MutableList<*>>(instance, "items")
                        list?.let {
                            val iterator = it.iterator()
                            while (iterator.hasNext()) {
                                val item = iterator.next().toJSONString()
                                val jb = item.parseObject()
                                val authorName = jb.getString("authorName")
                                val bookName = jb.getString("bookName")
                                val categoryName = jb.getString("categoryName")
                                val subCategoryName = jb.getString("subCategoryName")
                                val array = jb.getJSONArray("tags")
                                val bookTypeArray = mutableSetOf(categoryName, subCategoryName)
                                if (!array.isNullOrEmpty()) {
                                    for (i in array.indices) {
                                        array += array.getString(i)
                                    }
                                }
                                val isNeedShield = isNeedShield(
                                    bookName = bookName,
                                    authorName = authorName,
                                    bookType = bookTypeArray
                                )
                                if (isNeedShield) {
                                    iterator.remove()
                                }
                            }
                            result = it
                        }
                    }
                }
            }
        }
        else -> loggerE(msg = "屏蔽精选主页面不支持的版本号为: $versionCode")
    }
}

/**
 * 屏蔽分类
 */
fun PackageParam.shieldCategory(versionCode: Int) {
    when (versionCode) {
        788 -> {
            /**
             * 分类
             */
            findClass("com.qidian.QDReader.ui.adapter.x6\$a").hook {
                injectMember {
                    constructor {
                        param(
                            "com.qidian.QDReader.ui.adapter.x6".clazz,
                            ContextClass,
                            IntType,
                            ListClass
                        )
                    }
                    beforeHook {
                        val list = args[3] as? MutableList<*>
                        list?.let {
                            safeRun {
                                val iterator = it.iterator()
                                while (iterator.hasNext()) {
                                    val item = iterator.next().toJSONString()
                                    val jb = item.parseObject()
                                    val categoryName = jb.getString("categoryName")
                                    val subCategoryName = jb.getString("subCategoryName")
                                    val bookTypeArray = mutableSetOf<String>()
                                    if (!categoryName.isNullOrBlank()) {
                                        bookTypeArray += categoryName
                                    }
                                    if (!subCategoryName.isNullOrBlank()) {
                                        bookTypeArray += subCategoryName
                                    }
                                    val isNeedShield = isNeedShield(
                                        bookName = null, authorName = null, bookType = bookTypeArray
                                    )
                                    if (isNeedShield) {
                                        iterator.remove()
                                    }
                                }
                            }
                            args(3).set(it)
                        }
                    }
                }
            }
        }
        else -> loggerE(msg = "屏蔽分类不支持的版本号为: $versionCode")
    }
}

/**
 * 屏蔽免费-免费推荐
 */
fun PackageParam.shieldFreeRecommend(versionCode: Int) {
    when (versionCode) {
        788 -> {
            /**
             * 免费-免费推荐
             */
            findClass("la.a").hook {
                injectMember {
                    method {
                        name = "n"
                        param(
                            "com.qidian.QDReader.repository.entity.BookStoreDynamicItem".clazz,
                            IntType,
                            IntType
                        )
                    }
                    beforeHook {
                        val item = args[0]?.let { getParam<ArrayList<*>>(it, "BookList") }
                        item?.let {
                            safeRun {
                                val iterator = it.iterator()
                                while (iterator.hasNext()) {
                                    val item = iterator.next().toJSONString()
                                    val jb = item.parseObject()
                                    val authorName = jb.getString("AuthorName")
                                    val bookName = jb.getString("BookName")
                                    val categoryName = jb.getString("CategoryName")
                                    val subCategoryName = jb.getString("SubCategoryName")
                                    val array = jb.getJSONArray("tagList")
                                    val bookTypeArray = mutableSetOf<String>()
                                    categoryName?.let { bookTypeArray += categoryName }
                                    subCategoryName?.let { bookTypeArray += subCategoryName }
                                    array?.let { it1 ->
                                        it1.forEach { it2 ->
                                            bookTypeArray += it2.toString()
                                        }
                                    }
                                    val isNeedShield = isNeedShield(
                                        bookName = bookName,
                                        authorName = authorName,
                                        bookType = bookTypeArray
                                    )
                                    if (isNeedShield) {
                                        iterator.remove()
                                    }
                                }
                            }
                        }

                    }

                }
            }
        }
        else -> loggerE(msg = "屏蔽免费-免费推荐不支持的版本号: $versionCode")
    }
}

/**
 * 屏蔽免费-新书入库
 */
fun PackageParam.shieldFreeNewBook(versionCode: Int) {
    when (versionCode) {
        788 -> {
            /**
             * 免费-新书入库
             */
            findClass("com.qidian.QDReader.ui.fragment.QDNewBookInStoreFragment").hook {
                injectMember {
                    method {
                        name = "loadData\$lambda-6"
                        param(
                            "com.qidian.QDReader.ui.fragment.QDNewBookInStoreFragment".clazz,
                            "com.qidian.QDReader.repository.entity.NewBookInStore".clazz
                        )
                    }
                    beforeHook {
                        args[1]?.let {
                            val categoryIdList = getParam<MutableList<*>>(it, "CategoryIdList")
                            val itemList = getParam<MutableList<*>>(it, "ItemList")
                            categoryIdList?.let { list ->
                                safeRun {
                                    val iterator = list.iterator()
                                    while (iterator.hasNext()) {
                                        val item = iterator.next().toJSONString()
                                        val jb = item.parseObject()
                                        val categoryName = jb.getString("CategoryName")
                                        val isNeedShield = isNeedShield(
                                            bookName = null,
                                            authorName = null,
                                            bookType = setOf(categoryName)
                                        )
                                        if (isNeedShield) {
                                            iterator.remove()
                                        }
                                    }
                                }
                            }
                            itemList?.let { list ->
                                safeRun {
                                    val iterator = list.iterator()
                                    while (iterator.hasNext()) {
                                        val item = iterator.next().toJSONString()
                                        val jb = item.parseObject()
                                        val authorName = jb.getString("AuthorName")
                                        val bookName = jb.getString("BookName")
                                        val categoryName = jb.getString("CategoryName")
                                        val subCategoryName = jb.getString("SubCategoryName")
                                        val array = jb.getJSONArray("tagList")
                                        val bookTypeArray = mutableSetOf<String>()
                                        categoryName?.let { bookTypeArray += categoryName }
                                        subCategoryName?.let { bookTypeArray += subCategoryName }
                                        array?.let { jsonArray ->
                                            for (i in jsonArray.indices) {
                                                bookTypeArray += jsonArray[i].toString()
                                            }
                                        }
                                        val isNeedShield = isNeedShield(
                                            bookName = bookName,
                                            authorName = authorName,
                                            bookType = bookTypeArray
                                        )
                                        if (isNeedShield) {
                                            iterator.remove()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> loggerE(msg = "屏蔽免费-新书入库不支持的版本号: $versionCode")
    }
}

/**
 * 屏蔽畅销精选、主编力荐等更多
 */
fun PackageParam.shieldHotAndRecommend(versionCode: Int) {
    when (versionCode) {
        788 -> {
            /**
             * 畅销精选、主编力荐等更多
             */
            findClass("com.qidian.QDReader.ui.adapter.s").hook {
                injectMember {
                    method {
                        name = "n"
                        returnType = UnitType
                    }
                    beforeHook {
                        val list = getParam<MutableList<*>>(instance, "b")
                        list?.let {
                            safeRun {
                                val iterator = it.iterator()
                                while (iterator.hasNext()) {
                                    val item = iterator.next().toJSONString()
                                    val jb = item.parseObject()
                                    val authorName = jb.getString("AuthorName")
                                    val bookName = jb.getString("BookName")
                                    val categoryName = jb.getString("CategoryName")
                                    val subCategoryName = jb.getString("SubCategoryName")
                                    val array = jb.getJSONArray("Tags")
                                    val bookTypeArray = mutableSetOf(categoryName, subCategoryName)
                                    if (!array.isNullOrEmpty()) {
                                        for (i in array.indices) {
                                            array += array.getString(i)
                                        }
                                    }
                                    val isNeedShield = isNeedShield(
                                        bookName = bookName,
                                        authorName = authorName,
                                        bookType = bookTypeArray
                                    )
                                    if (isNeedShield) {
                                        iterator.remove()
                                    }
                                }
                            }
                            safeRun {
                                instance.javaClass.method {
                                    name = "setList"
                                    param(ListClass)
                                }.get(instance).call(it)
                            }
                        }

                    }
                }
            }
        }
        else -> loggerE(msg = "屏蔽畅销精选、主编力荐等更多不支持的版本号: $versionCode")
    }
}

/**
 * 屏蔽新书强推、三江推荐
 */
fun PackageParam.shieldNewBookAndRecommend(versionCode: Int) {
    when (versionCode) {
        788 -> {
            /**
             * 新书强推、三江推荐
             */
            findClass("com.qidian.QDReader.ui.fragment.SanJiangPagerFragment").hook {
                injectMember {
                    method {
                        name = "r"
                        param("com.qidian.QDReader.ui.fragment.SanJiangPagerFragment".clazz)
                        returnType = ListClass
                    }
                    afterHook {
                        val list = result as? MutableList<*>
                        list?.let {
                            safeRun {
                                val iterator = it.iterator()
                                while (iterator.hasNext()) {
                                    val item = iterator.next().toJSONString()
                                    val json = item.parseObject()
                                    val jb = json.getJSONObject("BookStoreItem")
                                    if (jb != null) {
                                        val authorName = jb.getString("AuthorName")
                                        val bookName = jb.getString("BookName")
                                        val categoryName = jb.getString("CategoryName")
                                        val array = jb.getJSONArray("tagList")
                                        val bookTypeArray = mutableSetOf(categoryName)
                                        if (!array.isNullOrEmpty()) {
                                            for (i in array.indices) {
                                                array += array.getString(i)
                                            }
                                        }
                                        val isNeedShield = isNeedShield(
                                            bookName = bookName,
                                            authorName = authorName,
                                            bookType = bookTypeArray
                                        )
                                        if (isNeedShield) {
                                            iterator.remove()
                                        }
                                    }
                                }
                            }
                            result = it
                        }
                    }
                }
            }
        }
        else -> loggerE(msg = "屏蔽新书强推、三江推荐不支持的版本号: $versionCode")
    }
}

/**
 * 屏蔽分类-全部作品
 */
fun PackageParam.shieldCategoryAllBook(versionCode: Int) {
    when (versionCode) {
        788 -> {
            /**
             * 分类-全部作品
             */
            findClass("com.qidian.QDReader.ui.activity.BookLibraryActivity").hook {
                injectMember {
                    method {
                        name = "M"
                        param("com.qidian.QDReader.ui.activity.BookLibraryActivity".clazz)
                        returnType = ArrayListClass
                    }
                    beforeHook {
                        args[0]?.let {
                            val list = getParam<ArrayList<*>>(it, "mBookList")
                            list?.let {
                                safeRun {
                                    val iterator = it.iterator()
                                    while (iterator.hasNext()) {
                                        val item = iterator.next().toJSONString()
                                        val jb = item.parseObject()
                                        val authorName = jb.getString("authorName")
                                        val bookName = jb.getString("bookName")
                                        val categoryName = jb.getString("categoryName")
                                        val isNeedShield = isNeedShield(
                                            bookName = bookName,
                                            authorName = authorName,
                                            bookType = setOf(categoryName)
                                        )
                                        if (isNeedShield) {
                                            iterator.remove()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> loggerE(msg = "屏蔽分类-全部作品不支持的版本号: $versionCode")
    }
}

/**
 * 屏蔽搜索发现(热词)
 */
fun PackageParam.shieldSearchFind(versionCode: Int) {
    when (versionCode) {
        788 -> {
            /**
             * 搜索发现(热词)
             */
            findClass("com.qidian.QDReader.repository.entity.search.SearchHotWordBean").hook {
                injectMember {
                    method {
                        name = "getWordList"
                        emptyParam()
                        returnType = ListClass
                    }
                    afterHook {
                        val list = result as? MutableList<*>
                        list?.clear()
                        result = list
                    }
                }
            }
        }
        else -> loggerE(msg = "屏蔽搜索发现(热词)不支持的版本号: $versionCode")
    }
}

/**
 * 屏蔽搜索-热门作品榜、人气标签榜
 * @param versionCode 版本号
 * @param isNeedShieldBookRank 屏蔽热门作品榜
 * @param isNeedShieldTagRank 屏蔽人气标签榜
 */
fun PackageParam.shieldSearch(
    versionCode: Int, isNeedShieldBookRank: Boolean, isNeedShieldTagRank: Boolean
) {
    when (versionCode) {
        788 -> {
            if (isNeedShieldBookRank) {
                /**
                 * 屏蔽热搜作品榜更多
                 */
                findClass("o9.d").hook {
                    injectMember {
                        method {
                            name = "o"
                        }
                        beforeHook {
                            val list = args[0] as? MutableList<*>
                            list?.let {
                                safeRun {
                                    val iterator = it.iterator()
                                    while (iterator.hasNext()) {
                                        val item = iterator.next().toJSONString()
                                        val jb = item.parseObject()
                                        val authorName = jb.getString("AuthorName")
                                        val bookName = jb.getString("BookName")
                                        val categoryName = jb.getString("BookCategory")
                                        val isNeedShield = isNeedShield(
                                            bookName = bookName,
                                            authorName = authorName,
                                            bookType = setOf(categoryName)
                                        )
                                        if (isNeedShield) {
                                            iterator.remove()
                                        }

                                    }
                                }
                                args(0).set(it)
                            }
                        }
                    }
                }
            }
            findClass("com.qidian.QDReader.ui.view.search.SearchHomePageRankView").hook {
                if (isNeedShieldBookRank) {
                    /**
                     * 热门作品榜
                     */
                    injectMember {
                        method {
                            name = "setBookRank"
                            param("com.qidian.QDReader.repository.entity.search.SearchBookRankBean".clazz)
                        }
                        beforeHook {
                            args[0]?.let {
                                val bookList = getParam<MutableList<*>>(it, "BookList")
                                bookList?.let {
                                    safeRun {
                                        val iterator = it.iterator()
                                        while (iterator.hasNext()) {
                                            val item = iterator.next().toJSONString()
                                            val jb = item.parseObject()
                                            val bookName = jb.getString("bookName")
                                            val isNeedShield = isNeedShield(
                                                bookName = bookName,
                                                authorName = null,
                                                bookType = null
                                            )
                                            if (isNeedShield) {
                                                iterator.remove()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isNeedShieldTagRank) {
                    /**
                     * 人气标签榜
                     */
                    injectMember {
                        method {
                            name = "setTagRank"
                            param("com.qidian.QDReader.repository.entity.search.SearchTagRankBean".clazz)
                        }
                        beforeHook {
                            args[0]?.let {
                                val list = getParam<MutableList<*>>(it, "TagList")
                                list?.let {
                                    safeRun {
                                        val iterator = it.iterator()
                                        while (iterator.hasNext()) {
                                            val item = iterator.next().toJSONString()
                                            val jb = item.parseObject()
                                            val tagName = jb.getString("tagName")
                                            val isNeedShield = isNeedShield(
                                                bookName = null,
                                                authorName = null,
                                                bookType = setOf(tagName)
                                            )
                                            if (isNeedShield) {
                                                iterator.remove()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> loggerE(msg = "屏蔽搜索-热门作品榜、人气标签榜不支持的版本号: $versionCode")
    }
}

/**
 * 屏蔽搜索-为你推荐
 */
fun PackageParam.shieldSearchRecommend(versionCode: Int) {
    when (versionCode) {
        788 -> {
            /**
             * 搜索-为你推荐
             */
            findClass("com.qidian.QDReader.repository.entity.search.SearchHomeCombineBean").hook {
                injectMember {
                    constructor {
                        param(ListClass)
                    }
                    beforeHook {
                        val list = args[0] as? MutableList<*>
                        list?.clear()
                        args(0).set(list)
                    }
                }
            }
        }
        else -> loggerE(msg = "屏蔽搜索-为你推荐不支持的版本号: $versionCode")
    }
}

/**
 * 配置模型
 * @param mainOption 主配置
 * @param advOption 广告配置
 * @param shieldOption 屏蔽配置
 * @param splashOption 启动配置
 */
@Keep
@Serializable
data class OptionEntity(
    @SerialName("advOption") var advOption: AdvOption = AdvOption(),
    @SerialName("mainOption") var mainOption: MainOption = MainOption(),
    @SerialName("shieldOption") var shieldOption: ShieldOption = ShieldOption(),
    @SerialName("splashOption") var splashOption: SplashOption = SplashOption()
) {
    /**
     * 广告配置
     * @param enableRemoveBookshelfFloat 是否开启移除书架浮窗
     * @param enableRemoveBookshelfBottomAd 是否开启移除书架底部导航栏广告
     * @param enableDisableCheckUpdate 是否开启禁止检查更新
     * @param enableDisableAdv 是否开启禁止广告
     */
    @Keep
    @Serializable
    data class AdvOption(
        @SerialName("enableDisableAdv") var enableDisableAdv: Boolean = false,
        @SerialName("enableDisableCheckUpdate") var enableDisableCheckUpdate: Boolean = false,
        @SerialName("enableRemoveBookshelfBottomAd") var enableRemoveBookshelfBottomAd: Boolean = false,
        @SerialName("enableRemoveBookshelfFloat") var enableRemoveBookshelfFloat: Boolean = false
    )

    /**
     * 主配置
     * @param packageName 包名
     * @param enableAutoSign 是否开启自动签到
     * @param enableOldLayout 是否开启旧版布局
     * @param enableLocalCard 是否开启本地至尊卡
     * @param enableHideBottomDot 是否开启隐藏底部小红点
     * @param enableDisableQSNModeDialog 是否开启关闭青少年模式弹框
     */
    @Keep
    @Serializable
    data class MainOption(
        @SerialName("enableAutoSign") var enableAutoSign: Boolean = false,
        @SerialName("enableDisableQSNModeDialog") var enableDisableQSNModeDialog: Boolean = false,
        @SerialName("enableHideBottomDot") var enableHideBottomDot: Boolean = false,
        @SerialName("enableLocalCard") var enableLocalCard: Boolean = false,
        @SerialName("enableOldLayout") var enableOldLayout: Boolean = false,
        @SerialName("packageName") var packageName: String = ""
    )

    /**
     * 屏蔽配置
     * @param shieldOptionValueSet 屏蔽配置值集合
     * @param authorList 屏蔽作者集合
     * @param bookTypeList 屏蔽书类集合
     * @param bookNameList 屏蔽书名集合
     */
    @Keep
    @Serializable
    data class ShieldOption(
        @SerialName("authorList") var authorList: List<String> = listOf(),
        @SerialName("bookNameList") var bookNameList: List<String> = listOf(),
        @SerialName("bookTypeList") var bookTypeList: List<String> = listOf(),
        @SerialName("shieldOptionValueSet") var shieldOptionValueSet: Set<String> = setOf()
    )

    /**
     * 闪屏页配置
     * @param enableSplash 是否开启闪屏页
     * @param enableCustomSplash 是否开启自定义闪屏页
     * @param enableCustomSplashAllButton 是否开启自定义闪屏页全部按钮
     * @param customBookId 自定义闪屏页书籍id
     * @param customSplashType 自定义闪屏页类型
     * @param customSplashImageFilePath 自定义闪屏页图片路径
     */
    @Keep
    @Serializable
    data class SplashOption(
        @SerialName("customBookId") var customBookId: String = "",
        @SerialName("customSplashImageFilePath") var customSplashImageFilePath: String = "",
        @SerialName("customSplashType") var customSplashType: Int = 0,
        @SerialName("enableCustomSplash") var enableCustomSplash: Boolean = false,
        @SerialName("enableCustomSplashAllButton") var enableCustomSplashAllButton: Boolean = false,
        @SerialName("enableSplash") var enableSplash: Boolean = false
    )
}

/**
 *读取配置文件模型
 */
fun readOptionEntity(): OptionEntity? {
    val file = readOptionFile() ?: return null
    return if (file.readText().isNotEmpty()) {
        try {
            val kJson = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
            kJson.decodeFromString<OptionEntity>(file.readText())
        } catch (e: Exception) {
            loggerE(msg = "readOptionFile: ${e.message}")
            defaultOptionEntity()
        }
    } else {
        defaultOptionEntity()
    }
}

/**
 * 读取配置文件
 */
fun readOptionFile(): File? {
    try {
        val file =
            File("${Environment.getExternalStorageDirectory().absolutePath}/QDReader", "option.json")
        return if (!file.exists()) {
            null
        } else file
    }catch (e: Exception) {
        loggerE(msg = "readOptionFile: ${e.message}")
    }
    return null
}

/**
 * 返回一个默认的配置模型
 */
fun defaultOptionEntity(): OptionEntity = OptionEntity(
    mainOption = OptionEntity.MainOption(
        packageName = "com.qidian.QDReader",
        enableAutoSign = true,
        enableOldLayout = false,
        enableLocalCard = true,
        enableHideBottomDot = true,
        enableDisableQSNModeDialog = true
    ), advOption = OptionEntity.AdvOption(
        enableRemoveBookshelfFloat = true,
        enableRemoveBookshelfBottomAd = true,
        enableDisableCheckUpdate = true,
        enableDisableAdv = true
    ), shieldOption = OptionEntity.ShieldOption(
        shieldOptionValueSet = mutableSetOf(),
        authorList = mutableListOf(),
        bookTypeList = mutableListOf(),
        bookNameList = mutableListOf()
    ), splashOption = OptionEntity.SplashOption(
        enableSplash = false,
        enableCustomSplash = false,
        enableCustomSplashAllButton = false,
        customBookId = "",
        customSplashType = 0,
        customSplashImageFilePath = ""
    )
)






