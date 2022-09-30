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
     * @param enableDisableBookshelfActivityPopup 启用移除书架活动弹框
     * @param enableDisableBookshelfFloat 启用禁用书架浮窗
     * @param enableDisableBookshelfBottomAd 启用禁用书架底部导航栏广告
     * @param enableDisableAccountCenterAd 启用移除我-中间广告
     * @param enableDisableCheckUpdate 启用禁止检查更新
     * @param enableDisableAdv 启用禁止广告
     */
    @Keep
    @Serializable
    data class AdvOption(
        @SerialName("enableDisableBookshelfActivityPopup") var enableDisableBookshelfActivityPopup: Boolean = false,
        @SerialName("enableDisableAdv") var enableDisableAdv: Boolean = false,
        @SerialName("enableDisableCheckUpdate") var enableDisableCheckUpdate: Boolean = false,
        @SerialName("enableDisableAccountCenterAd") var enableDisableAccountCenterAd: Boolean = false,
        @SerialName("enableDisableBookshelfBottomAd") var enableDisableBookshelfBottomAd: Boolean = false,
        @SerialName("enableDisableBookshelfFloat") var enableDisableBookshelfFloat: Boolean = false
    )

    /**
     * 主配置
     * @param packageName 包名
     * @param enableAutoSign 启用自动签到
     * @param enableOldLayout 启用旧版布局
     * @param enableLocalCard 启用本地至尊卡
     */
    @Keep
    @Serializable
    data class MainOption(
        @SerialName("packageName") var packageName: String = "",
        @SerialName("enableAutoSign") var enableAutoSign: Boolean = false,
        @SerialName("enableLocalCard") var enableLocalCard: Boolean = false,
        @SerialName("enableOldLayout") var enableOldLayout: Boolean = false
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
        @SerialName("authorList") var authorList: Set<String> = emptySet(),
        @SerialName("bookNameList") var bookNameList: Set<String> = emptySet(),
        @SerialName("bookTypeList") var bookTypeList: Set<String> = emptySet(),
        @SerialName("shieldOptionValueSet") var shieldOptionValueSet: MutableSet<Int> = mutableSetOf(),
        @SerialName("enableBookTypeEnhancedBlocking") var enableBookTypeEnhancedBlocking: Boolean = false
    )

    /**
     * 闪屏页配置
     * @param enableSplash 启用闪屏页
     * @param enableCustomSplash 启用自定义闪屏页
     * @param enableCustomSplashAllButton 启用自定义闪屏页全部按钮
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
     * @param enableHideBookshelfDailyReading 启用隐藏书架每日导读
     * @param enableHideMainBottomNavigationBarFind 启用隐藏主页底部导航栏发现
     * @param enableSearchHideAllView 启用隐藏搜索全部控件
     * @param enableHideMainBottomNavigationRedDot 启用隐藏主页底部导航栏红点
     * @param enableDisableQSNModeDialog 启用关闭青少年模式弹框
     * @param accountOption 用户页面配置
     * @param bookDetailOptions 书籍详情配置
     */
    @Keep
    @Serializable
    data class ViewHideOption(
        @SerialName("enableHideBookshelfDailyReading") var enableHideBookshelfDailyReading: Boolean = false,
        @SerialName("enableHideMainBottomNavigationBarFind") var enableHideMainBottomNavigationBarFind: Boolean = false,
        @SerialName("enableSearchHideAllView") var enableSearchHideAllView: Boolean = false,
        @SerialName("enableDisableQSNModeDialog") var enableDisableQSNModeDialog: Boolean = false,
        @SerialName("enableHideMainBottomNavigationRedDot") var enableHideMainBottomNavigationRedDot: Boolean = false,
        @SerialName("AccountOption") var accountOption: AccountOption = AccountOption(),
        @SerialName("BookDetailOptions") var bookDetailOptions: BookDetailOptions = BookDetailOptions()
    ) {

        /**
         * 用户页面配置
         * @param enableHideAccount 启用开启隐藏用户页面
         * @param enableHideAccountRightTopRedDot 启用隐藏用户页面右上角红点
         * @param configurationsOptionList 可用配置集合
         * @param configurationsSelectedOptionList 已选配置集合
         */
        @Keep
        @Serializable
        data class AccountOption(
            @SerialName("enableHideAccount") var enableHideAccount: Boolean = false,
            @SerialName("enableHideAccountRightTopRedDot") var enableHideAccountRightTopRedDot: Boolean = false,
            @SerialName("configurationsOptionList") var configurationsOptionList: MutableSet<String> = mutableSetOf(),
            @SerialName("configurationsSelectedOptionList") var configurationsSelectedOptionList: MutableSet<String> = mutableSetOf()
        )

        /**
         * 书籍详情页面配置
         * @param enableHideBookDetail 启用隐藏书籍详情页面
         * @param configurationsOptionList 可用配置集合
         * @param configurationsSelectedOptionList 已选配置集合
         */
        @Keep
        @Serializable
        data class BookDetailOptions(
            @SerialName("enableHideBookDetail") var enableHideBookDetail: Boolean = false,
            @SerialName("configurationsOptionList") var configurationsOptionList: List<String> = listOf(
                "出圈指数",
                "荣誉标签",
                "QQ群",
                "书友圈",
                "书友榜",
                "月票金主",
                "本书看点|中心广告",
                "浮窗广告",
                "同类作品推荐",
                "看过此书的人还看过"
            ),
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
            file.parentFile?.mkdirs()
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
fun writeOptionFile(optionEntity: OptionEntity): Boolean =
    try {
        readOptionFile()?.writeText(Json.encodeToString(optionEntity))
        true
    } catch (e: Exception) {
        loggerE(msg = "writeOptionFile: ${e.message}")
        false
    }

/**
 * 返回一个默认的配置模型
 */
fun defaultOptionEntity(): OptionEntity = OptionEntity(
    mainOption = OptionEntity.MainOption(
        packageName = "com.qidian.QDReader",
        enableAutoSign = true,
        enableOldLayout = false,
        enableLocalCard = true
    ), advOption = OptionEntity.AdvOption(
        enableDisableBookshelfFloat = true,
        enableDisableBookshelfBottomAd = true,
        enableDisableAccountCenterAd = true,
        enableDisableCheckUpdate = true,
        enableDisableAdv = true
    ), shieldOption = OptionEntity.ShieldOption(
        shieldOptionValueSet = mutableSetOf(),
        authorList = emptySet(),
        bookTypeList = emptySet(),
        bookNameList = emptySet()
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
        enableHideMainBottomNavigationRedDot = true,
        enableDisableQSNModeDialog = true,
        accountOption = OptionEntity.ViewHideOption.AccountOption(
            enableHideAccount = true,
            enableHideAccountRightTopRedDot = true,
            configurationsOptionList = mutableSetOf(),
            configurationsSelectedOptionList = mutableSetOf()
        )
    )
)

/**
 * 更新配置
 */
fun updateOptionEntity(): Boolean = writeOptionFile(HookEntry.optionEntity)