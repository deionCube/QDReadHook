package cn.xihan.qdds

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import cn.xihan.qdds.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.highcapable.yukihookapi.hook.xposed.prefs.ui.ModulePreferenceFragment

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window?.statusBarColor = Color.TRANSPARENT
        // ViewPager2 Adapter for the fragments
        val viewPager2Adapter =
            ViewPager2Adapter(supportFragmentManager, lifecycle)
        binding.viewPager2.adapter = viewPager2Adapter
        // Tablayout 和 ViewPager2 绑定
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.main_title)
                1 -> getString(R.string.ads_title)
                2 -> getString(R.string.splash_title)
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }.attach()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}

class SettingsFragment : ModulePreferenceFragment() {
    val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences(
            "${BuildConfig.APPLICATION_ID}_preferences",
            MODE_PRIVATE
        )
    }

    companion object {
        fun newInstance(index: Int): SettingsFragment {
            val args = Bundle()
            args.putInt("index", index)
            val fragment = SettingsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreatePreferencesInModuleApp(savedInstanceState: Bundle?, rootKey: String?) {
        when (arguments?.getInt("index") ?: 0) {
            0 -> setPreferencesFromResource(R.xml.root_preferences, rootKey)
            1 -> setPreferencesFromResource(R.xml.ads_preferences, rootKey)
            2 -> setPreferencesFromResource(R.xml.splash__preferences, rootKey)
        }
    }

    /*
    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "AuthorListData" -> {
                val list = sharedPreferences.getStringSet("authorList", setOf(""))
                list?.let {
                    selector(list.toList(), "已屏蔽的作者列表") { dialog, i ->

                    }
                }
            }
            "" -> {

            }
        }
        return super.onPreferenceTreeClick(preference)
    }

     */

    /*
    override fun onDisplayPreferenceDialog(preference: Preference) {

        when (preference.key) {
            "author" -> {
                InputDialog(
                    "添加需要屏蔽的作者名称",
                    "",
                    "确定",
                    "取消",
                    ""
                ).setCancelable(false).setOkButton { dialog, view, input ->
                    if (input.isNotBlank()) {
                        editStringSet("authorList", input, true)
                        false
                    } else {
                        PopTip.show("不能为空")
                        true
                    }
                }.show()


            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

     */


    /**
     * 给指定 key 的 StringSet 增加或者删除数据
     * @param key 需要操作的 key
     * @param value 需要操作的 value
     * @param isAdd 是否添加
     */
    fun editStringSet(key: String, value: String, isAdd: Boolean) {
        val set = sharedPreferences.getStringSet(key, setOf(""))
        set?.let {
            val newSet = it.toMutableSet()
            if (isAdd) {
                newSet.add(value)
            } else {
                newSet.remove(value)
            }
            sharedPreferences.edit().putStringSet(key, newSet).apply()
        }
    }
}

class ViewPager2Adapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return SettingsFragment.newInstance(position)
    }
}
