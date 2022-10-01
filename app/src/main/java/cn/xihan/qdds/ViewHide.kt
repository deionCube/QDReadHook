package cn.xihan.qdds

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.alibaba.fastjson2.parseObject
import com.alibaba.fastjson2.toJSONString
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import de.robv.android.xposed.XposedHelpers

/**
 * @项目名 : QDReadHook
 * @作者 : MissYang
 * @创建时间 : 2022/8/28 16:12
 * @介绍 :
 */
/**
 * 隐藏主页面-顶部宝箱提示
 */
fun PackageParam.hideMainTopBox(versionCode: Int) {
    when (versionCode) {
        812 -> {
            findClass("com.qidian.QDReader.ui.activity.MainGroupActivity").hook {
                injectMember {
                    method {
                        name = "getGlobalMsg"
                        emptyParam()
                        returnType = UnitType
                    }
                    intercept()
                }
            }
        }

        else -> loggerE(msg = "主页面-顶部宝箱提示不支持的版本号: $versionCode")
    }
}

/**
 * 隐藏书架-每日导读
 * 上级调用:com.qidian.QDReader.ui.fragment.QDBookShelfPagerFragment.bindView()
 * bindGridAdapter()
 * bindListAdapter()
 */
fun PackageParam.hideBookshelfDailyReading(versionCode: Int) {
    when (versionCode) {
        in 804..812 -> {
            findClass("com.qidian.QDReader.ui.adapter.j0").hook {
                injectMember {
                    method {
                        name = "getHeaderItemCount"
                        emptyParam()
                        returnType = IntType
                    }
                    replaceTo(0)
                }
            }

            findClass("com.qidian.QDReader.ui.adapter.h0").hook {
                injectMember {
                    method {
                        name = "getHeaderItemCount"
                        emptyParam()
                        returnType = IntType
                    }
                    replaceTo(0)
                }
            }
        }

        else -> loggerE(msg = "隐藏书架-每日导读不支持版本号:$versionCode")
    }
}

/**
 * 搜索页面一刀切
 */
fun PackageParam.hideSearchAllView(versionCode: Int) {
    when (versionCode) {
        in 788..812 -> {
            /**
             * 搜索页面一刀切
             */
            findClass("com.qidian.QDReader.ui.fragment.serach.NewSearchHomePageFragment").hook {
                injectMember {
                    method {
                        name = "loadData"
                        returnType = UnitType
                    }
                    intercept()
                }
            }
        }

        else -> loggerE(msg = "屏蔽搜索页面一刀切不支持的版本号: $versionCode")
    }
}

/**
 * 隐藏底部导航栏红点
 * 上级调用位置:com.qidian.QDReader.ui.widget.maintab.PagerSlidingTabStrip.s()
 */
fun PackageParam.hideBottomRedDot(versionCode: Int) {
    val needHookClass = when (versionCode) {
        in 758..768 -> "com.qidian.QDReader.ui.widget.maintab.a"
        in 772..850 -> "com.qidian.QDReader.ui.widget.maintab.e"
        else -> null
    }
    needHookClass?.hook {
        injectMember {
            method {
                name = "h"
                returnType = IntType
            }
            replaceTo(1)
        }
    } ?: loggerE(msg = "隐藏底部导航栏红点不支持的版本号为: $versionCode")
}

/**
 * 隐藏底部导航栏-发现
 */
fun PackageParam.hideBottomNavigationFind(versionCode: Int) {
    when (versionCode) {
        in 792..850 -> {
            findClass("com.qidian.QDReader.ui.widget.maintab.PagerSlidingTabStrip").hook {
                injectMember {
                    method {
                        name = "s"
                        emptyParam()
                        returnType = UnitType
                    }
                    afterHook {
                        val i = instance.getView<LinearLayout>("i")
                        i?.let {
                            val view = it.getChildAt(2)
                            view?.visibility = View.GONE
                        }
                    }
                }
            }
        }

        else -> loggerE(msg = "隐藏底部导航栏-发现不支持的版本号: $versionCode")
    }
}

/**
 * 我-隐藏控件
 */
fun PackageParam.accountViewHide(
    versionCode: Int
) {
    when (versionCode) {
        in 792..808 -> {
            /**
             * 我-隐藏控件
             */
            findClass("com.qidian.QDReader.ui.fragment.QDUserAccountFragment").hook {
                injectMember {
                    method {
                        name = "lambda\$loadData\$3"
                        param("com.qidian.QDReader.repository.entity.UserAccountDataBean".toClass())
                        returnType = UnitType
                    }
                    beforeHook {
                        args[0]?.let {
                            val items = it.getParam<MutableList<*>>("Items")
                            items?.let { list ->
                                safeRun {
                                    val iterator = list.iterator()
                                    while (iterator.hasNext()) {
                                        val item = iterator.next() as? MutableList<*>
                                        item?.let { list2 ->
                                            val iterator2 = list2.iterator()
                                            while (iterator2.hasNext()) {
                                                val item2 = iterator2.next().toJSONString()
                                                val jb = item2.parseObject()
                                                if (jb != null) {
                                                    val showName = jb.getString("showName")
                                                    HookEntry.optionEntity.viewHideOption.accountOption.configurationsOptionList += showName
                                                    if (HookEntry.optionEntity.viewHideOption.accountOption.enableHideAccount && HookEntry.optionEntity.viewHideOption.accountOption.configurationsSelectedOptionList.isNotEmpty()) {
                                                        if (showName in HookEntry.optionEntity.viewHideOption.accountOption.configurationsSelectedOptionList) {
                                                            iterator2.remove()
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
                }
            }
        }

        812 -> {
            findClass("com.qidian.QDReader.ui.fragment.QDUserAccountFragment").hook {
                injectMember {
                    method {
                        name = "renderUIByData"
                        param("com.qidian.QDReader.repository.entity.UserAccountDataBean".toClass())
                        returnType = UnitType
                    }
                    beforeHook {
                        args[0]?.let {
                            val items = it.getParam<MutableList<*>>("Items")
                            items?.let { list ->
                                safeRun {
                                    val iterator = list.iterator()
                                    while (iterator.hasNext()) {
                                        val item = iterator.next() as? MutableList<*>
                                        item?.let { list2 ->
                                            val iterator2 = list2.iterator()
                                            while (iterator2.hasNext()) {
                                                val item2 = iterator2.next().toJSONString()
                                                val jb = item2.parseObject()
                                                if (jb != null) {
                                                    val showName = jb.getString("showName")
                                                    HookEntry.optionEntity.viewHideOption.accountOption.configurationsOptionList += showName
                                                    if (HookEntry.optionEntity.viewHideOption.accountOption.enableHideAccount && HookEntry.optionEntity.viewHideOption.accountOption.configurationsSelectedOptionList.isNotEmpty()) {
                                                        if (showName in HookEntry.optionEntity.viewHideOption.accountOption.configurationsSelectedOptionList) {
                                                            iterator2.remove()
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
                }
            }
        }

        else -> loggerE(msg = "我-隐藏控件不支持的版本号: $versionCode")
    }
}

/**
 * 隐藏我-右上角消息红点
 */
fun PackageParam.accountRightTopRedDot(versionCode: Int) {
    when (versionCode) {
        812 -> {
            findClass("com.qidian.QDReader.component.config.QDAppConfigHelper").hook {
                injectMember {
                    method {
                        name = "F0"
                        emptyParam()
                        returnType = BooleanType
                    }
                    replaceToFalse()
                }
            }
        }

        else -> loggerE(msg = "我-右上角消息红点不支持的版本号: $versionCode")
    }
}

/**
 * 我-移除青少年模式弹框
 * 上级调用:com.qidian.QDReader.bll.helper.QDTeenagerHelper$Companion.h() new-instance v2, g1
 */
fun PackageParam.removeQSNYDialog(versionCode: Int) {
    when (versionCode) {
        in 758..850 -> {
            findClass("com.qidian.QDReader.bll.helper.g1").hook {
                injectMember {
                    method {
                        name = "run"
                        returnType = UnitType
                    }
                    intercept()
                }
            }
        }

        else -> loggerE(msg = "移除青少年模式弹框不支持的版本号为: $versionCode")
    }

    /*
        /**
         * 上级调用位置:com.qidian.QDReader.bll.manager.QDTeenagerManager.teenWorkDialog
         */
        val dialogClassName: String? = when (versionCode) {
            in 758..768 -> "com.qidian.QDReader.bll.helper.v1"
            772 -> "com.qidian.QDReader.bll.helper.w1"
            in 776..800 -> "com.qidian.QDReader.bll.helper.t1"
            else -> null
        }
        dialogClassName?.hook {
            injectMember {
                method {
                    name = "show"
                    superClass()
                }
                beforeHook {
                    printCallStack(instance.javaClass.name)
                }
                //intercept()
            }
        } ?: loggerE(msg = "移除青少年模式弹框不支持的版本号为: $versionCode")

     */

}

/**
 * 书籍详情-隐藏控件
 * @param versionCode 版本号
 * @param isNeedHideCqzs 是否需要隐藏出圈指数
 * @param isNeedHideRybq 是否需要隐藏荣誉标签
 * @param isNeedHideQqGroups 是否需要隐藏QQ群
 * @param isNeedHideSyq 是否需要隐藏书友圈
 * @param isNeedHideSyb 是否需要隐藏书友榜
 * @param isNeedHideYpjz 是否需要隐藏月票金主
 * @param isNeedHideCenterAd 是否需要隐藏本书看点|中心广告
 * @param isNeedHideFloatAd 是否需要隐藏浮窗广告
 * @param isNeedHideBookRecommend 是否需要隐藏同类作品推荐
 * @param isNeedHideBookRecommend2 是否需要隐藏看过此书的人还看过
 */
fun PackageParam.bookDetailHide(
    versionCode: Int,
    isNeedHideCqzs: Boolean = HookEntry.isEnableBookDetailOption(0),
    isNeedHideRybq: Boolean = HookEntry.isEnableBookDetailOption(1),
    isNeedHideQqGroups: Boolean = HookEntry.isEnableBookDetailOption(2),
    isNeedHideSyq: Boolean = HookEntry.isEnableBookDetailOption(3),
    isNeedHideSyb: Boolean = HookEntry.isEnableBookDetailOption(4),
    isNeedHideYpjz: Boolean = HookEntry.isEnableBookDetailOption(5),
    isNeedHideCenterAd: Boolean = HookEntry.isEnableBookDetailOption(6),
    isNeedHideFloatAd: Boolean = HookEntry.isEnableBookDetailOption(7),
    isNeedHideBookRecommend: Boolean = HookEntry.isEnableBookDetailOption(8),
    isNeedHideBookRecommend2: Boolean = HookEntry.isEnableBookDetailOption(9)
) {
    when (versionCode) {
        in 808..812 -> {

            findClass("com.qidian.QDReader.ui.activity.QDBookDetailActivity").hook {
                injectMember {
                    method {
                        name = "notifyData"
                        param(BooleanType)
                        returnType = UnitType
                    }
                    beforeHook {
                        val mBookDetail = instance.getParam<Any>("mBookDetail")
                        mBookDetail?.let {

                            val baseBookInfo = it.getParam<Any>("baseBookInfo")
                            baseBookInfo?.let {
                                /**
                                 * 荣誉标签
                                 */
                                if (isNeedHideRybq) {
                                    val honorTagList =
                                        baseBookInfo.getParam<MutableList<*>>("honorTagList")
                                    honorTagList?.clear()
                                }

                                /**
                                 * 月票金主
                                 */
                                if (isNeedHideYpjz) {
                                    val monthTopUser =
                                        baseBookInfo.getParam<MutableList<*>>("monthTopUser")
                                    monthTopUser?.clear()
                                }

                            }

                            /**
                             * QQ群
                             */
                            if (isNeedHideQqGroups) {
                                val qqGroup = it.getParam<MutableList<*>>("qqGroup")
                                qqGroup?.clear()
                            }

                            /**
                             * 同类作品推荐
                             */
                            if (isNeedHideBookRecommend) {
                                val sameRecommend = it.getParam<MutableList<*>>("sameRecommend")
                                sameRecommend?.clear()
                            }

                            /**
                             * 看过此书的人还看过
                             */
                            if (isNeedHideBookRecommend2) {
                                val bookFriendsRecommend =
                                    it.getParam<MutableList<*>>("bookFriendsRecommend")
                                bookFriendsRecommend?.clear()
                            }

                        }
                    }
                }

                if (isNeedHideCenterAd) {
                    injectMember {
                        method {
                            name = "getAD\$lambda-74\$lambda-73\$lambda-72"
                            returnType = UnitType
                        }
                        intercept()
                    }
                }

                if (isNeedHideFloatAd) {
                    injectMember {
                        method {
                            name = "getFloatingAd"
                            emptyParam()
                            returnType = UnitType
                        }
                        intercept()
                    }
                }

                if (isNeedHideCqzs) {
                    /**
                     * 出圈指数
                     */
                    injectMember {
                        method {
                            name = "addCircleMarkInfo"
                            param("com.qidian.QDReader.repository.entity.OutCircleIndexInfo".toClass())
                            returnType = UnitType
                        }
                        afterHook {
                            val view = XposedHelpers.callMethod(
                                instance,
                                "findViewById",
                                0x7F090442
                            ) as? View
                            view?.visibility = View.GONE
                        }
                    }
                }

            }

            if (isNeedHideSyb) {
                /**
                 * 隐藏书友榜
                 */
                findClass("com.qidian.QDReader.ui.view.BookFansModuleView").hook {
                    injectMember {
                        method {
                            name = "d"
                            param(
                                LongType,
                                StringType,
                                "com.qidian.QDReader.repository.entity.FansInfo".toClass(),
                                ListClass
                            )
                            returnType = UnitType
                        }
                        afterHook {
                            val bookFansModuleView = instance as? LinearLayout
                            bookFansModuleView?.visibility = View.GONE
                        }
                    }
                }
            }

            if (isNeedHideSyq) {
                /**
                 * 隐藏书友圈
                 */
                findClass("com.qidian.QDReader.ui.view.BookCircleModuleView").hook {
                    injectMember {
                        method {
                            name = "bind"
                            returnType = UnitType
                        }
                        afterHook {
                            val bookCircleModuleView = instance as? LinearLayout
                            bookCircleModuleView?.visibility = View.GONE
                        }
                    }
                }
            }
        }

        else -> loggerE(msg = "书籍详情-隐藏控件不支持的版本号: $versionCode")
    }
}

/**
 * 我-隐藏控件配置
 */
fun Context.showHideOptionDialog() {
    val linearLayout = CustomLinearLayout(this, isAutoWidth = false)

    val hideMainTopBoxOption =  CustomSwitch(
        context = this,
        title = "隐藏主页顶部宝箱提示",
        isEnable = HookEntry.optionEntity.viewHideOption.enableHideMainTopBox
    ) {
        HookEntry.optionEntity.viewHideOption.enableHideMainTopBox = it
    }
    val hideBookshelfDailyReadingOption = CustomSwitch(
        context = this,
        title = "隐藏书架-每日导读",
        isEnable = HookEntry.optionEntity.viewHideOption.enableHideBookshelfDailyReading
    ) {
        HookEntry.optionEntity.viewHideOption.enableHideBookshelfDailyReading = it
    }

    val searchHideAllViewOption = CustomSwitch(
        context = this,
        title = "搜索页面一刀切",
        isEnable = HookEntry.optionEntity.viewHideOption.enableSearchHideAllView
    ) {
        HookEntry.optionEntity.viewHideOption.enableSearchHideAllView = it
    }
    val enableHideBottomDotOption = CustomSwitch(
        context = this,
        title = "隐藏底部导航栏红点",
        isEnable = HookEntry.optionEntity.viewHideOption.enableHideMainBottomNavigationRedDot
    ) {
        HookEntry.optionEntity.viewHideOption.enableHideMainBottomNavigationRedDot = it
    }
    val mainBottomNavigationBarFindOptionSwitch = CustomSwitch(
        context = this,
        title = "隐藏底部导航栏-发现",
        isEnable = HookEntry.optionEntity.viewHideOption.enableHideMainBottomNavigationBarFind
    ) {
        HookEntry.optionEntity.viewHideOption.enableHideMainBottomNavigationBarFind = it
    }
    val enableDisableQSNModeDialogOption = CustomSwitch(
        context = this,
        title = "关闭青少年模式弹框",
        isEnable = HookEntry.optionEntity.viewHideOption.enableDisableQSNModeDialog
    ) {
        HookEntry.optionEntity.viewHideOption.enableDisableQSNModeDialog = it
    }
    val accountViewHideRightTopRedDotSwitchOption = CustomSwitch(
        context = this,
        title = "隐藏我-右上角消息红点",
        isEnable = HookEntry.optionEntity.viewHideOption.accountOption.enableHideAccountRightTopRedDot
    ) {
        HookEntry.optionEntity.viewHideOption.accountOption.enableHideAccountRightTopRedDot = it
    }
    val accountViewHideOptionSwitchOption = CustomSwitch(
        context = this,
        title = "启用我-隐藏控件",
        isEnable = HookEntry.optionEntity.viewHideOption.accountOption.enableHideAccount
    ) {
        HookEntry.optionEntity.viewHideOption.accountOption.enableHideAccount = it
    }
    val customTextView = CustomTextView(
        context = this,
        mText = "我-屏蔽控件列表",
        isBold = true
    ) {
        val shieldOptionList =
            HookEntry.optionEntity.viewHideOption.accountOption.configurationsOptionList.toList()
        val checkedItems = BooleanArray(shieldOptionList.size)
        if (HookEntry.optionEntity.viewHideOption.accountOption.configurationsSelectedOptionList.isNotEmpty()) {
            safeRun {
                shieldOptionList.forEachIndexed { index, _ ->
                    // 对比 shieldOptionList 和 optionEntity.viewHideOption.accountOption.configurationsSelectedOptionList 有相同的元素就设置为true
                    if (HookEntry.optionEntity.viewHideOption.accountOption.configurationsSelectedOptionList.any { it == shieldOptionList[index] }) {
                        checkedItems[index] = true
                    }

                }
            }
        }
        multiChoiceSelector(shieldOptionList, checkedItems, "屏蔽选项列表") { _, i, isChecked ->
            checkedItems[i] = isChecked
        }.doOnDismiss {
            checkedItems.forEachIndexed { index, b ->
                if (b) {
                    HookEntry.optionEntity.viewHideOption.accountOption.configurationsSelectedOptionList += shieldOptionList[index]
                } else {
                    HookEntry.optionEntity.viewHideOption.accountOption.configurationsSelectedOptionList -= shieldOptionList[index]
                }
            }
        }
    }
    val bookDetailHideOptionSwitch = CustomSwitch(
        context = this,
        title = "启用书籍详情-隐藏控件",
        isEnable = HookEntry.optionEntity.viewHideOption.bookDetailOptions.enableHideBookDetail
    ) {
        HookEntry.optionEntity.viewHideOption.bookDetailOptions.enableHideBookDetail = it
    }
    val bookDetailHideOptionList = CustomTextView(
        context = this,
        mText = "书籍详情-屏蔽控件列表",
        isBold = true
    ) {
        val shieldOptionList =
            HookEntry.optionEntity.viewHideOption.bookDetailOptions.configurationsOptionList.toList()
        val checkedItems = BooleanArray(shieldOptionList.size)
        if (HookEntry.optionEntity.viewHideOption.bookDetailOptions.configurationsSelectedOptionList.isNotEmpty()) {
            safeRun {
                shieldOptionList.forEachIndexed { index, _ ->
                    // 对比 shieldOptionList 和 optionEntity.viewHideOption.accountOption.configurationsSelectedOptionList 有相同的元素就设置为true
                    if (HookEntry.optionEntity.viewHideOption.bookDetailOptions.configurationsSelectedOptionList.any { it == shieldOptionList[index] }) {
                        checkedItems[index] = true
                    }
                }
            }
        }
        multiChoiceSelector(shieldOptionList, checkedItems, "屏蔽选项列表") { _, i, isChecked ->
            checkedItems[i] = isChecked
        }.doOnDismiss {
            checkedItems.forEachIndexed { index, b ->
                if (b) {
                    HookEntry.optionEntity.viewHideOption.bookDetailOptions.configurationsSelectedOptionList += shieldOptionList[index]
                } else {
                    HookEntry.optionEntity.viewHideOption.bookDetailOptions.configurationsSelectedOptionList -= shieldOptionList[index]
                }
            }
        }
    }
    linearLayout.apply {
        addView(hideMainTopBoxOption)
        addView(hideBookshelfDailyReadingOption)
        addView(searchHideAllViewOption)
        addView(enableHideBottomDotOption)
        addView(mainBottomNavigationBarFindOptionSwitch)
        addView(enableDisableQSNModeDialogOption)
        addView(accountViewHideRightTopRedDotSwitchOption)
        addView(accountViewHideOptionSwitchOption)
        addView(customTextView)
        addView(bookDetailHideOptionSwitch)
        addView(bookDetailHideOptionList)
    }
    alertDialog {
        title = "隐藏控件配置"
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