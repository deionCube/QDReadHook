package cn.xihan.qdds

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.view.View
import com.highcapable.yukihookapi.hook.log.loggerE
import de.robv.android.xposed.XposedHelpers
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.exitProcess

/**
 * @项目名 : QDReadHook
 * @作者 : MissYang
 * @创建时间 : 2022/8/28 16:13
 * @介绍 :
 */
/**
 * 通过反射获取控件
 * @param name 字段名
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T : View> Any.getView(name: String): T? = getParam<T>(name)

/**
 * 反射获取任何类型
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T> Any.getParam(name: String): T? = javaClass.getDeclaredField(name).apply {
    isAccessible = true
}[this] as? T

/**
 * 利用 Reflection 获取当前的系统 Context
 */
fun getSystemContext(): Context {
    val activityThreadClass = XposedHelpers.findClass("android.app.ActivityThread", null)
    val activityThread =
        XposedHelpers.callStaticMethod(activityThreadClass, "currentActivityThread")
    val context = XposedHelpers.callMethod(activityThread, "getSystemContext") as? Context
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
 * 重启当前应用
 */
fun Activity.restartApplication() {
    // https://stackoverflow.com/a/58530756
    val pm = packageManager
    val intent = pm.getLaunchIntentForPackage(packageName)
    finishAffinity()
    startActivity(intent)
    exitProcess(0)
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

fun Any.printCallStack() {
    printCallStack(this.javaClass.name)
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
            "${Environment.getExternalStorageDirectory().path}/MT2/apks/起点",
            "$fileName-$index.txt"
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

/**
 * dp 转 px
 */
fun Context.dp2px(dp: Float): Int {
    val scale = resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}

/**
 * 隐藏应用图标
 */
fun Activity.hideAppIcon() {
    val componentName = ComponentName(this, MainActivity::class.java.name)
    if (packageManager.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}

/**
 * 显示应用图标
 */
fun Activity.showAppIcon() {
    val componentName = ComponentName(this, MainActivity::class.java.name)
    if (packageManager.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}

/**
 * 检查模块更新
 */
@Throws(Exception::class)
fun Context.checkModuleUpdate() {
    // 创建一个子线程
    Thread {
        Looper.prepare()
        // Java 原生网络请求
        val url = URL("https://api.github.com/repos/xihan123/QDReadHook/releases/latest")
        val connection = url.openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "GET"
            connectTimeout = 5000
            readTimeout = 5000
            doInput = true
            useCaches = false
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36"
            )
        }
        try {
            connection.connect()
            if (connection.responseCode == 200) {
                val inputStream = connection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                bufferedReader.forEachLine {
                    stringBuilder.append(it)
                }
                val jsonObject = JSONObject(stringBuilder.toString())
                val versionName = jsonObject.getString("tag_name")
                val downloadUrl = jsonObject.getJSONArray("assets").getJSONObject(0)
                    .getString("browser_download_url")
                val releaseNote = jsonObject.getString("body")
                if (versionName != BuildConfig.VERSION_NAME) {
                    alertDialog {
                        title = "发现新版本: $versionName"
                        message = "更新内容:\n$releaseNote"
                        positiveButton("下载更新") {
                            startActivity(Intent(Intent.ACTION_VIEW).also {
                                it.data = Uri.parse(downloadUrl)
                            })
                        }
                        negativeButton("返回") {
                            it.dismiss()
                        }
                        build()
                        show()
                    }
                }
            }
        } catch (e: Exception) {
            loggerE(msg = "checkModuleUpdate 报错: ${e.message}")
        } finally {
            connection.disconnect()
        }
        Looper.loop()
    }.start()
}

/**
 * 容错的根据正则修改字符串返回字符串
 * @param enableRegex 启用正则表达式
 * @param regex 正则表达式
 * @param replacement 替换内容
 */
fun String.safeReplace(
    enableRegex: Boolean = false,
    regex: String = "",
    replacement: String = ""
): String {
    return try {
        if (enableRegex) {
            this.replace(regex.toRegex(), replacement)
        } else {
            this.replace(regex, replacement)
        }
    } catch (e: Exception) {
        loggerE(msg = "safeReplace 报错: ${e.message}")
        this
    }
}

/**
 * 根据 ReplaceRuleOption 中 replaceRuleList 修改返回字符串
 * @param replaceRuleList 替换规则列表
 */
fun String.replaceByReplaceRuleList(replaceRuleList: List<OptionEntity.ReplaceRuleOption.ReplaceItem>): String =
    try {
        var result = this
        replaceRuleList.forEach {
            result =
                result.safeReplace(it.enableRegularExpressions, it.replaceRuleRegex, it.replaceWith)
        }
        result
    } catch (e: Exception) {
        loggerE(msg = "replaceByReplaceRuleList 报错: ${e.message}")
        this
    }


