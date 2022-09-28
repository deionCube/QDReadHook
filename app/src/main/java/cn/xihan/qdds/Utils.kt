package cn.xihan.qdds

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.view.View
import com.highcapable.yukihookapi.hook.log.loggerE
import de.robv.android.xposed.XposedHelpers
import java.io.File
import kotlin.system.exitProcess

/**
 * @项目名 : QDReadHook
 * @作者 : MissYang
 * @创建时间 : 2022/8/28 16:13
 * @介绍 :
 */
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