package cn.xihan.qdds

import android.os.Environment
import androidx.annotation.Keep
import com.highcapable.yukihookapi.hook.log.loggerE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * @项目名 : QDReadHook
 * @作者 : MissYang
 * @创建时间 : 2022/8/28 16:13
 * @介绍 :
 */

/**
 * 配置模型
 * @param mainOption 主配置
 * @param advOption 广告配置
 * @param shieldOption 屏蔽配置
 * @param splashOption 启动配置
 * @param viewHideOption 控件隐藏配置
 */
@Keep
@Serializable
data class OptionEntity(
    @SerialName("advOption") var advOption: AdvOption = AdvOption(),
    @SerialName("mainOption") var mainOption: MainOption = MainOption(),
    @SerialName("shieldOption") var shieldOption: ShieldOption = ShieldOption(),
    @SerialName("splashOption") var splashOption: SplashOption = SplashOption(),
    @SerialName("viewHideOption") var viewHideOption: ViewHideOption = ViewHideOption()
) {
    /**
     * 广告配置
     * @param enableRemoveBookshelfFloat 开启移除书架浮窗
     * @param enableRemoveBookshelfBottomAd 开启移除书架底部导航栏广告
     * @param enableRemoveAccountCenterAd 开启移除我-中间广告
     * @param enableDisableCheckUpdate 开启禁止检查更新
     * @param enableDisableAdv 开启禁止广告
     */
    @Keep
    @Serializable
    data class AdvOption(
        @SerialName("enableDisableAdv") var enableDisableAdv: Boolean = false,
        @SerialName("enableDisableCheckUpdate") var enableDisableCheckUpdate: Boolean = false,
        @SerialName("enableRemoveAccountCenterAd") var enableRemoveAccountCenterAd: Boolean = false,
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
     * @param enableBookTypeEnhancedBlocking 启用书类型增强屏蔽
     */
    @Keep
    @Serializable
    data class ShieldOption(
        @SerialName("authorList") var authorList: List<String> = listOf(),
        @SerialName("bookNameList") var bookNameList: List<String> = listOf(),
        @SerialName("bookTypeList") var bookTypeList: List<String> = listOf(),
        @SerialName("shieldOptionValueSet") var shieldOptionValueSet: MutableSet<Int> = mutableSetOf(),
        @SerialName("enableBookTypeEnhancedBlocking") var enableBookTypeEnhancedBlocking: Boolean = false
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

    /**
     * 控件隐藏配置
     * @param enableHideMainBottomNavigationBarFind 开启隐藏主页底部导航栏发现
     * @param enableSearchHideAllView 开启隐藏搜索全部控件
     * @param auccountOption 用户页面配置
     */
    @Keep
    @Serializable
    data class ViewHideOption(
        @SerialName("enableHideMainBottomNavigationBarFind") var enableHideMainBottomNavigationBarFind: Boolean = false,
        @SerialName("enableSearchHideAllView") var enableSearchHideAllView: Boolean = false,
        @SerialName("AccountOption") var accountOption: AccountOption = AccountOption()
    ) {

        /**
         * 用户页面配置
         * @param enableHideAccount 是否开启隐藏用户页面
         * @param configurationsOptionList 可用配置集合
         * @param configurationsSelectedOptionList 已选配置集合
         */
        @Keep
        @Serializable
        data class AccountOption(
            @SerialName("enableHideAccount") var enableHideAccount: Boolean = false,
            @SerialName("configurationsOptionList") var configurationsOptionList: MutableSet<String> = mutableSetOf(),
            @SerialName("configurationsSelectedOptionList") var configurationsSelectedOptionList: MutableSet<String> = mutableSetOf()
        )

    }
}

/**
 *读取配置文件模型
 */
fun readOptionEntity(): OptionEntity {
    val file = readOptionFile() ?: return defaultOptionEntity()
    return if (file.readText().isNotEmpty()) {
        try {
            val kJson = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
            kJson.decodeFromString(file.readText())
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
        val file = File(
            "${Environment.getExternalStorageDirectory().absolutePath}/QDReader", "option.json"
        )
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(Json.encodeToString(defaultOptionEntity()))
        }
        return file
    } catch (e: Exception) {
        loggerE(msg = "readOptionFile: ${e.message}")
    }
    return null
}

/**
 * 写入配置文件
 */
fun writeOptionFile(optionEntity: OptionEntity) {
    try {
        readOptionFile()?.writeText(Json.encodeToString(optionEntity))
    } catch (e: Exception) {
        loggerE(msg = "writeOptionFile: ${e.message}")
    }
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
        enableRemoveAccountCenterAd = true,
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
    ),
    viewHideOption = OptionEntity.ViewHideOption(
        enableHideMainBottomNavigationBarFind = false,
        accountOption = OptionEntity.ViewHideOption.AccountOption(
            enableHideAccount = true,
            configurationsOptionList = mutableSetOf(),
            configurationsSelectedOptionList = mutableSetOf()
        )
    )
)

/**
 * 更新配置
 */
fun updateOptionEntity(): Boolean = try {
    writeOptionFile(HookEntry.optionEntity)
    true
} catch (e: Exception) {
    false
}