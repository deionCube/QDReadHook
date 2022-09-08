package cn.xihan.qdds

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.alibaba.fastjson2.parseObject
import com.alibaba.fastjson2.toJSONString
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.UnitType

/**
 * @项目名 : QDReadHook
 * @作者 : MissYang
 * @创建时间 : 2022/8/28 16:12
 * @介绍 :
 */
/**
 * 搜索页面一刀切
 */
fun PackageParam.hideSearchAllView(versionCode: Int) {
    when (versionCode) {
        in 788..800 -> {
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
 * 移除书架右下角浮窗
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
        in 772..850 -> {
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
 * 隐藏底部小红点
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
        in 772..850 -> {
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
                        val i = getView<LinearLayout>(instance, "i")
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
fun PackageParam.accountViewHide(versionCode: Int) {
    when (versionCode) {
        in 792..800 -> {
            /**
             * 我-隐藏控件
             */
            findClass("com.qidian.QDReader.ui.fragment.QDUserAccountFragment").hook {
                injectMember {
                    method {
                        name = "lambda\$loadData\$3"
                        param("com.qidian.QDReader.repository.entity.UserAccountDataBean".clazz)
                        returnType = UnitType
                    }
                    beforeHook {
                        args[0]?.let {
                            val items = getParam<MutableList<*>>(it, "Items")
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
 * 我-隐藏控件配置
 */
fun Context.showHideOptionDialog() {
    val linearLayout = CustomLinearLayout(this, isAutoWidth = false)

    /**
     * 搜索隐藏所有控件
     */
    val searchHideAllViewOption = CustomSwitch(
        context = this,
        title = "搜索页面一刀切",
        isEnable = HookEntry.optionEntity.viewHideOption.enableSearchHideAllView
    ){
        HookEntry.optionEntity.viewHideOption.enableSearchHideAllView = it
    }
    val enableHideBottomDotOption = CustomSwitch(
        context = this, title = "隐藏底部小红点", isEnable = HookEntry.optionEntity.mainOption.enableHideBottomDot
    ) {
        HookEntry.optionEntity.mainOption.enableHideBottomDot = it
    }
    val mainBottomNavigationBarFindOptionSwitch = CustomSwitch(
        context = this,
        title = "隐藏底部导航栏-发现",
        isEnable = HookEntry.optionEntity.viewHideOption.enableHideMainBottomNavigationBarFind
    ) {
        HookEntry.optionEntity.viewHideOption.enableHideMainBottomNavigationBarFind = it
    }
    val accountViewHideOptionSwitch = CustomSwitch(
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
    linearLayout.addView(searchHideAllViewOption)
    linearLayout.addView(enableHideBottomDotOption)
    linearLayout.addView(mainBottomNavigationBarFindOptionSwitch)
    linearLayout.addView(accountViewHideOptionSwitch)
    linearLayout.addView(customTextView)

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