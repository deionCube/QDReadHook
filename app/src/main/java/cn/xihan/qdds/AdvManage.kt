package cn.xihan.qdds

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.StringType
import com.highcapable.yukihookapi.hook.type.java.UnitType

/**
 * @项目名 : QDReadHook
 * @作者 : MissYang
 * @创建时间 : 2022/8/28 16:15
 * @介绍 :
 */
/**
 * 禁用书架活动弹框
 * 上级调用:com.qidian.QDReader.component.config.QDAppConfigHelper$Companion.getBKTData
 * activityPopupBean.getData()
 */
fun PackageParam.disableBookshelfActivityPopup(versionCode: Int) {
    when (versionCode) {
        in 804..812 -> {
            findClass("com.qidian.QDReader.repository.entity.config.ActivityPopupBean").hook {
                injectMember {
                    method {
                        name = "getData"
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

        else -> loggerE(msg = "移除书架活动弹框不支持版本号：$versionCode")
    }
}

/**
 * 禁用书架右下角浮窗
 */
fun PackageParam.disableBookshelfFloatWindow(versionCode: Int) {
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
                        val imgAdIconClose = instance.getView<ImageView>(
                            "imgAdIconClose"
                        )
                        imgAdIconClose?.visibility = View.GONE
                        val layoutImgAdIcon = instance.getView<LinearLayout>(
                            "layoutImgAdIcon"
                        )
                        layoutImgAdIcon?.visibility = View.GONE

                        val imgBookShelfActivityIcon = instance.getView<ImageView>(
                            "imgBookShelfActivityIcon"
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
                        val layoutImgAdIcon = instance.getView<LinearLayout>(
                            "layoutImgAdIcon"
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
 * 移除底部导航栏中心广告
 * 上级调用位置:com.qidian.QDReader.ui.activity.MainGroupActivity.checkAdTab()
 */
fun PackageParam.disableBottomNavigationCenterAd(versionCode: Int) {
//    val hookClassName = when (versionCode) {
//        in 758..796 -> "com.qidian.QDReader.ui.activity.MainGroupActivity\$t"
//        in 800..850 -> "com.qidian.QDReader.ui.activity.MainGroupActivity\$a"
//        else -> null
//    }
//    hookClassName?.hook {
//        injectMember {
//            method {
//                name = "c"
//            }
//            intercept()
//        }
//    } ?: loggerE(msg = "移除底部导航栏中心广告不支持的版本号为: $versionCode")

    when (versionCode) {
        in 758..850 -> {
            findClass("com.qidian.QDReader.ui.activity.MainGroupActivity").hook {
                injectMember {
                    method {
                        name = "checkAdTab"
                        emptyParam()
                        returnType = UnitType
                    }
                    intercept()
                }
            }
        }

        else -> loggerE(msg = "移除底部导航栏中心广告不支持的版本号为: $versionCode")
    }
}

/**
 * 移除我-中心广告
 */
fun PackageParam.disableAccountCenterAd(versionCode: Int) {
    when (versionCode) {
        in 758..850 -> {
            findClass("com.qidian.QDReader.ui.fragment.QDAccountFragment").hook {
                injectMember {
                    method {
                        name = "loadADData"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.ui.fragment.QDUserAccountFragment").hook {
                injectMember {
                    method {
                        name = "loadADData"
                        returnType = UnitType
                    }
                    intercept()
                }
            }
        }

        else -> loggerE(msg = "移除我-中心广告不支持的版本号为: $versionCode")
    }
}

/**
 * Hook 禁用广告
 */
fun PackageParam.disableAd(versionCode: Int) {
    when (versionCode) {
        in 758..850 -> {
            findClass("com.qq.e.comm.constants.CustomPkgConstants").hook {
                injectMember {
                    method {
                        name = "getAssetPluginName"
                        emptyParam()
                        returnType = StringType
                    }
                    replaceTo("")
                }
            }

            findClass("com.qq.e.comm.b").hook {
                injectMember {
                    method {
                        name = "a"
                        param(ContextClass)
                        returnType = BooleanType
                    }
                    replaceToFalse()
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

            /*
            findClass("com.qidian.QDReader.component.api.a").hook {
                injectMember {
                    method {
                        name = "b"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

             */

            // TODO 首页横幅广告:/argus/api/v2/adv/getadvlistbatch

        }

        else -> loggerE(msg = "禁用广告不支持的版本号为: $versionCode")
    }
}

/**
 * 禁用检查更新
 * 上级调用:com.qidian.QDReader.ui.activity.MainGroupActivity.onCreate(android.os.Bundle)
 */
fun PackageParam.disableUpdate(versionCode: Int) {
    /**
     * 也可全局搜索 "UpgradeCommon"、"checkUpdate:"
     */
    val neddHookClass = when (versionCode) {
        in 758..788 -> "com.qidian.QDReader.util.z4"
        in 792..796 -> "com.qidian.QDReader.util.i5"
        in 800..812 -> "com.qidian.QDReader.util.l5"
        else -> null
    }
    neddHookClass?.hook {
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

    /**
     * 上级调用:com.qidian.QDReader.ui.activity.MainGroupActivity.checkUpdate()
     */
    val neddHookClass2 = when (versionCode) {
        in 758..808 -> "w4.h"
        812 -> "t4.h"
        else -> null
    }
    neddHookClass2?.hook {
        injectMember {
            method {
                name = "l"
                returnType = UnitType
            }
            intercept()
        }
    }

    when (versionCode) {
        in 758..812 -> {

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
 * 广告配置弹框
 */
fun Context.showAdvOptionDialog() {
    val linearLayout = CustomLinearLayout(this, isAutoWidth = false)
    val enableDisableBookshelfActivityPopupOption = CustomSwitch(
        context = this,
        title = "禁用书架活动弹框",
        isEnable = HookEntry.optionEntity.advOption.enableDisableBookshelfActivityPopup
    ) {
        HookEntry.optionEntity.advOption.enableDisableBookshelfActivityPopup = it
    }

    val enableDisableBookshelfFloatOption = CustomSwitch(
        context = this,
        title = "禁用书架右下角浮窗",
        isEnable = HookEntry.optionEntity.advOption.enableDisableBookshelfFloat
    ) {
        HookEntry.optionEntity.advOption.enableDisableBookshelfFloat = it
    }
    val enableDisableBookshelfBottomAdOption = CustomSwitch(
        context = this,
        title = "禁用底部导航栏中心广告",
        isEnable = HookEntry.optionEntity.advOption.enableDisableBookshelfBottomAd
    ) {
        HookEntry.optionEntity.advOption.enableDisableBookshelfBottomAd = it
    }
    val enableDisableAccountCenterAdOption = CustomSwitch(
        context = this,
        title = "禁用账号中心广告",
        isEnable = HookEntry.optionEntity.advOption.enableDisableAccountCenterAd
    ) {
        HookEntry.optionEntity.advOption.enableDisableAccountCenterAd = it
    }
    val enableDisableCheckUpdateOption = CustomSwitch(
        context = this,
        title = "禁用检查更新",
        isEnable = HookEntry.optionEntity.advOption.enableDisableCheckUpdate
    ) {
        HookEntry.optionEntity.advOption.enableDisableCheckUpdate = it
    }

    val enableDisableAdvOption = CustomSwitch(
        context = this,
        title = "禁用TX广告",
        isEnable = HookEntry.optionEntity.advOption.enableDisableAdv
    ) {
        HookEntry.optionEntity.advOption.enableDisableAdv = it
    }
    linearLayout.addView(enableDisableBookshelfActivityPopupOption)
    linearLayout.addView(enableDisableBookshelfFloatOption)
    linearLayout.addView(enableDisableBookshelfBottomAdOption)
    linearLayout.addView(enableDisableAccountCenterAdOption)
    linearLayout.addView(enableDisableCheckUpdateOption)
    linearLayout.addView(enableDisableAdvOption)
    alertDialog {
        title = "广告配置"
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