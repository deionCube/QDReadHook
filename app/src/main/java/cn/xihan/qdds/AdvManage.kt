package cn.xihan.qdds

import android.content.Context
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.java.StringType
import com.highcapable.yukihookapi.hook.type.java.UnitType

/**
 * @项目名 : QDReadHook
 * @作者 : MissYang
 * @创建时间 : 2022/8/28 16:15
 * @介绍 :
 */
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
 * 移除我-中心广告
 */
fun PackageParam.removeAccountCenterAd(versionCode: Int) {
    when (versionCode) {
        in 758..792 -> {
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
 * 广告配置弹框
 */
fun Context.showAdvOptionDialog() {
    val linearLayout = CustomLinearLayout(this, isAutoWidth = false)
    val enableRemoveBookshelfFloatOption = CustomSwitch(
        context = this,
        title = "移除书架右下角浮窗",
        isEnable = HookEntry.optionEntity.advOption.enableRemoveBookshelfFloat
    ) {
        HookEntry.optionEntity.advOption.enableRemoveBookshelfFloat = it
    }
    val enableRemoveBookshelfBottomAdOption = CustomSwitch(
        context = this,
        title = "移除底部导航栏中心广告",
        isEnable = HookEntry.optionEntity.advOption.enableRemoveBookshelfBottomAd
    ) {
        HookEntry.optionEntity.advOption.enableRemoveBookshelfBottomAd = it
    }
    val enableRemoveAccountCenterAdOption = CustomSwitch(
        context = this,
        title = "移除账号中心广告",
        isEnable = HookEntry.optionEntity.advOption.enableRemoveAccountCenterAd
    ) {
        HookEntry.optionEntity.advOption.enableRemoveAccountCenterAd = it
    }
    val enableDisableCheckUpdateOption = CustomSwitch(
        context = this, title = "禁用检查更新", isEnable = HookEntry.optionEntity.advOption.enableDisableCheckUpdate
    ) {
        HookEntry.optionEntity.advOption.enableDisableCheckUpdate = it
    }
    val enableDisableAdvOption = CustomSwitch(
        context = this, title = "禁用TX广告", isEnable = HookEntry.optionEntity.advOption.enableDisableAdv
    ) {
        HookEntry.optionEntity.advOption.enableDisableAdv = it
    }
    linearLayout.addView(enableRemoveBookshelfFloatOption)
    linearLayout.addView(enableRemoveBookshelfBottomAdOption)
    linearLayout.addView(enableRemoveAccountCenterAdOption)
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