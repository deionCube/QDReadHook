package cn.xihan.qdds

import android.content.Context
import cn.xihan.qdds.HookEntry.Companion.isNeedShield
import cn.xihan.qdds.HookEntry.Companion.parseNeedShieldList
import com.alibaba.fastjson2.parseObject
import com.alibaba.fastjson2.toJSONString
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.ArrayListClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.UnitType

/**
 * @项目名 : QDReadHook
 * @作者 : MissYang
 * @创建时间 : 2022/8/28 16:11
 * @介绍 :
 */

/**
 * 屏蔽选项
 * @param versionCode 版本号
 * @param optionValueSet 屏蔽选项值
 */
fun PackageParam.shieldOption(versionCode: Int, optionValueSet: Set<Int>) {
    // 遍历 optionValueSet 包含的值 执行指定方法
    optionValueSet.forEach {
        when (it) {
            0 -> shieldSearchFind(versionCode)
            3 -> shieldSearchRecommend(versionCode)
            4 -> shieldChoice(versionCode)
            5 -> shieldCategory(versionCode)
            6 -> shieldCategoryAllBook(versionCode)
            7 -> shieldFreeRecommend(versionCode)
            8 -> shieldFreeNewBook(versionCode)
            9 -> shieldHotAndRecommend(versionCode)
            10 -> shieldNewBookAndRecommend(versionCode)
            11 -> shieldNewBookRank(versionCode)
            12 -> shieldNewBook(versionCode)
            13 -> shieldDailyReading(versionCode)
        }
    }
    shieldSearch(versionCode, HookEntry.isEnableOption(1), HookEntry.isEnableOption(2))
}

/**
 * 屏蔽每日导读指定的书籍
 * @param isNeedShieldAllData
 */
fun PackageParam.shieldDailyReading(
    versionCode: Int
) {
    /**
     * 上级调用:com.qidian.QDReader.ui.activity.DailyReadingActivity.onCreate bindView()
     */
    val needHookClass = when (versionCode) {
        in 788..800 -> "com.qidian.QDReader.component.api.b1"
        in 804..820 -> "com.qidian.QDReader.component.api.f1"
        else -> null
    }

    needHookClass?.hook {
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
                        result = parseNeedShieldList(list)
                    }
                }
            }
        }
    } ?: loggerE(msg = "屏蔽每日导读不支持的版本号为: $versionCode")
}

/**
 * 屏蔽精选主页面
 */
fun PackageParam.shieldChoice(versionCode: Int) {
    when (versionCode) {
        in 788..808 -> {
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
                            safeRun {
                                result = parseNeedShieldList(it)
                            }
                        }
                    }
                }
            }
        }

        else -> loggerE(msg = "屏蔽精选主页面不支持的版本号为: $versionCode")
    }
}

/**
 * 屏蔽精选-分类
 * 上级调用:com.qidian.QDReader.ui.activity.QDBookCategoryActivity  mLeftAdapter
 */
fun PackageParam.shieldCategory(versionCode: Int) {
    when (versionCode) {
        in 788..808 -> {
            /**
             * 分类
             * 上级调用:com.qidian.QDReader.ui.adapter.x6.onBindContentItemViewHolder if(v1 == 2)
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
                                args(3).set(parseNeedShieldList(it))
                            }
                        }
                    }
                }
            }
        }

        else -> loggerE(msg = "屏蔽分类不支持的版本号为: $versionCode")
    }
}

/**
 * 屏蔽精选-免费-免费推荐
 * 上级调用:com.qidian.QDReader.ui.fragment.QDBookStoreFragment.onViewInject
 * mAdapter
 * onBindContentItemViewHolder
 * if(this.getContentItemViewType(arg8) != 8)
 */
fun PackageParam.shieldFreeRecommend(versionCode: Int) {
    val freeRecommendHookClass: String? = when (versionCode) {
        788 -> "la.a"
        in 792..808 -> "ka.a"
        else -> null
    }
    freeRecommendHookClass?.hook {
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
                        parseNeedShieldList(it)
                    }
                }

            }

        }
    } ?: loggerE(msg = "屏蔽免费-免费推荐不支持的版本号: $versionCode")
}

/**
 * 屏蔽精选-新书
 */
fun PackageParam.shieldNewBook(versionCode: Int) {
    when (versionCode) {
        in 792..850 -> {
            /**
             * 精选-新书
             */
            findClass("com.qidian.QDReader.repository.entity.newbook.NewBookCard").hook {
                injectMember {
                    method {
                        name = "buildData"
                        returnType = UnitType
                    }
                    afterHook {
                        safeRun {
                            val goldRecBean = getParam<Any>(instance, "goldRecBean")
                            goldRecBean?.let {
                                val items = getParam<MutableList<*>>(goldRecBean, "items")
                                items?.let {
                                    parseNeedShieldList(items)
                                }
                            }
                            val bookShortageBean =
                                getParam<Any>(instance, "bookShortageBean")
                            bookShortageBean?.let {
                                val items = getParam<MutableList<*>>(bookShortageBean, "items")
                                items?.let {
                                    parseNeedShieldList(items)
                                }
                            }

                            val monthBannerListBean =
                                getParam<Any>(instance, "monthBannerListBean")
                            monthBannerListBean?.let {
                                val items =
                                    getParam<MutableList<*>>(monthBannerListBean, "items")
                                items?.let {
                                    parseNeedShieldList(items)
                                }
                            }
                            val newBookAIRecommendBean =
                                getParam<Any>(instance, "newBookAIRecommendBean")
                            newBookAIRecommendBean?.let {
                                val items =
                                    getParam<MutableList<*>>(newBookAIRecommendBean, "items")
                                items?.let {
                                    parseNeedShieldList(items)
                                }
                            }
                            val newBookRankBean =
                                getParam<Any>(instance, "newBookRankBean")
                            newBookRankBean?.let {
                                val items = getParam<MutableList<*>>(newBookRankBean, "items")
                                items?.let {
                                    parseNeedShieldList(items)
                                }
                            }
                            val newBookRecommendBean =
                                getParam<Any>(instance, "newBookRecommendBean")
                            /*
                            newBookRecommendBean?.let {
                                val items =
                                    getParam<MutableList<*>>(newBookRecommendBean, "items")
                                items?.let {
                                    val iterator = it.iterator()
                                    while (iterator.hasNext()) {
                                        val item = iterator.next().toJSONString()
                                        val jb = item.parseObject()
                                        val recData = jb.getJSONObject("recData")
                                        if (recData != null){
                                            val items = recData.getJSONArray("items") as? MutableList<*>
                                            items?.let {
                                                parseNeedShieldList(items)
                                            }
                                        }
                                    }
                                }
                            }

                             */
                            val newBookTagBean =
                                getParam<Any>(instance, "newBookTagBean")
                            newBookTagBean?.let {
                                val items = getParam<MutableList<*>>(newBookTagBean, "items")
                                items?.let {
                                    parseNeedShieldList(items)
                                }
                            }

                        }
                    }
                }
            }

            /**
             * 精选-新书-新书入库/新书强推
             */
            findClass("com.qidian.QDReader.repository.entity.newbook.RecPageData").hook {
                injectMember {
                    method {
                        name = "getItems"
                        returnType = ListClass
                    }
                    afterHook {
                        val list = result as? MutableList<*>
                        list?.let {
                            parseNeedShieldList(it)
                        }
                    }
                }
            }
        }

        else -> loggerE(msg = "屏蔽精选-新书不支持的版本号为: $versionCode")
    }
}

/**
 * 屏蔽免费-新书入库
 */
fun PackageParam.shieldFreeNewBook(versionCode: Int) {
    when (versionCode) {
        in 788..808 -> {
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
                                    parseNeedShieldList(list)
                                }
                            }
                            itemList?.let { list ->
                                safeRun {
                                    parseNeedShieldList(list)
                                }
                            }
                        }
                    }
                }
            }

            findClass("com.qidian.QDReader.ui.activity.QDNewBookInStoreActivity").hook {
                injectMember {
                    method {
                        name = "loadData\$lambda-6"
                        param(
                            "com.qidian.QDReader.ui.activity.QDNewBookInStoreActivity".clazz,
                            "com.qidian.QDReader.repository.entity.NewBookInStore".clazz
                        )
                    }
                    beforeHook {
                        args[1]?.let {
                            val categoryIdList = getParam<MutableList<*>>(it, "CategoryIdList")
                            val itemList = getParam<MutableList<*>>(it, "ItemList")
                            categoryIdList?.let { list ->
                                safeRun {
                                    parseNeedShieldList(list)
                                }
                            }
                            itemList?.let { list ->
                                safeRun {
                                    parseNeedShieldList(list)
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
 * 上级调用:com.qidian.QDReader.ui.activity.QDNewBookInStoreActivity.initView() 在刷新前修改List数据
 * getMAdapter
 */
fun PackageParam.shieldHotAndRecommend(versionCode: Int) {
    when (versionCode) {
        in 788..850 -> {
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
                                parseNeedShieldList(it)
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
                                result = it
                            }
                        }
                    }
                }
            }
        }

        in 792..800 -> {
            findClass("com.qidian.QDReader.ui.fragment.SanJiangPagerFragment").hook {
                injectMember {
                    method {
                        name = "s"
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
                                result = it
                            }
                        }
                    }
                }
            }
        }

        in 804..808 -> {
            /**
             *上级调用:com.qidian.QDReader.ui.fragment.SanJiangPagerFragment mAdapter
             */
            findClass("com.qidian.QDReader.ui.adapter.lb").hook {
                injectMember {
                    method {
                        name = "q"
                        param(ListClass)
                        returnType = UnitType
                    }
                    beforeHook {
                        val list = args[0] as? MutableList<*>
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
                                args(0).set(it)
                            }
                        }
                    }
                }
            }
        }

        else -> loggerE(msg = "屏蔽新书强推、三江推荐不支持的版本号: $versionCode")
    }
}

/**
 * 屏蔽新书排行榜
 */
fun PackageParam.shieldNewBookRank(versionCode: Int) {
    when (versionCode) {
        808 -> {
            findClass("com.qidian.QDReader.ui.fragment.RankingFragment").hook {
                injectMember {
                    method {
                        name = "lambda\$loadBookList\$4"
                        param(
                            BooleanType,
                            BooleanType,
                            IntType,
                            "com.qidian.QDReader.repository.entity.RankListData".clazz
                        )
                        returnType = UnitType
                    }
                    beforeHook {
                        args[3]?.let {
                            val rankBookList = getParam<MutableList<*>>(it, "rankBookList")
                            rankBookList?.let {
                                parseNeedShieldList(rankBookList)
                            }
                        }
                    }
                }

            }
        }
        else -> loggerE(msg = "屏蔽新书排行榜不支持的版本号: $versionCode")
    }
}

/**
 * 屏蔽分类-全部作品
 */
fun PackageParam.shieldCategoryAllBook(versionCode: Int) {
    when (versionCode) {
        in 788..808 -> {
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
                                    parseNeedShieldList(it)
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
        in 788..850 -> {
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
    versionCode: Int,
    isNeedShieldBookRank: Boolean,
    isNeedShieldTagRank: Boolean
) {
    if (isNeedShieldBookRank) {
        /**
         * 上级调用: com.qidian.QDReader.ui.activity.QDSearchListActivity.bindView() mAdapter
         */
        val needHookClass: String? = when (versionCode) {
            788 -> "o9.d"
            in 792..808 -> "n9.d"
            else -> null
        }
        /**
         * 屏蔽热门作品榜更多
         */
        needHookClass?.hook {
            injectMember {
                method {
                    name = "o"
                }
                beforeHook {
                    val list = args[0] as? MutableList<*>
                    list?.let {
                        safeRun {
                            args(0).set(parseNeedShieldList(it))
                        }
                    }
                }
            }
        } ?: loggerE(msg = "屏蔽热门作品榜更多不支持的版本号: $versionCode")
    }
    when (versionCode) {
        in 788..850 -> {
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
                                        parseNeedShieldList(it)
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
                                        parseNeedShieldList(it)
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
        in 788..850 -> {
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
 * 屏蔽相关配置弹框
 */
fun Context.showShieldOptionDialog() {
    val linearLayout = CustomLinearLayout(this, isAutoWidth = false)
    val customTextView = CustomTextView(
        context = this, mText = "屏蔽选项列表", isBold = true
    ) {
        val shieldOptionList = listOf(
            "搜索-发现(热词)",
            "搜索-热门作品榜",
            "搜索-人气标签榜",
            "搜索-为你推荐",
            "精选-主页面",
            "精选-分类",
            "精选-分类-全部作品",
            "精选-免费-免费推荐",
            "精选-免费-新书入库",
            "精选-畅销精选、主编力荐等更多",
            "精选-新书强推、三江推荐",
            "精选-新书强推-新书排行榜",
            "精选-新书",
            "每日导读"
        )
        val checkedItems = BooleanArray(shieldOptionList.size)
        if (HookEntry.optionEntity.shieldOption.shieldOptionValueSet.isNotEmpty()) {
            safeRun {
                shieldOptionList.forEachIndexed { index, _ ->
                    if (index in HookEntry.optionEntity.shieldOption.shieldOptionValueSet) {
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
                    HookEntry.optionEntity.shieldOption.shieldOptionValueSet += index
                } else {
                    HookEntry.optionEntity.shieldOption.shieldOptionValueSet -= index
                }
            }
        }
    }
    val authorNameOptionCustomEdit = CustomEditText(
        context = this,
        title = "填入需要屏蔽的完整作者名称",
        message = "使用 \";\" 分隔",
        value = HookEntry.optionEntity.shieldOption.authorList.joinToString(";")
    ) {
        HookEntry.optionEntity.shieldOption.authorList = HookEntry.parseKeyWordOption(it)
    }
    val bookNameOptionCustomEdit = CustomEditText(
        context = this,
        title = "填入需要屏蔽的书名关键词",
        message = "注意:单字威力巨大!!!\n使用 \";\" 分隔",
        value = HookEntry.optionEntity.shieldOption.bookNameList.joinToString(";")
    ) {
        HookEntry.optionEntity.shieldOption.bookNameList = HookEntry.parseKeyWordOption(it)
    }
    val bookTypeSwitch = CustomSwitch(
        context = this,
        title = "启用书类型增强屏蔽",
        isEnable = HookEntry.optionEntity.shieldOption.enableBookTypeEnhancedBlocking
    ) {
        HookEntry.optionEntity.shieldOption.enableBookTypeEnhancedBlocking = it
    }
    val bookTypeOptionCustomEdit = CustomEditText(
        context = this,
        title = "填入需要屏蔽的书类型",
        message = "使用 \";\" 分隔",
        value = HookEntry.optionEntity.shieldOption.bookTypeList.joinToString(";")
    ) {
        HookEntry.optionEntity.shieldOption.bookTypeList = HookEntry.parseKeyWordOption(it)
    }
    linearLayout.addView(customTextView)
    linearLayout.addView(authorNameOptionCustomEdit)
    linearLayout.addView(bookNameOptionCustomEdit)
    linearLayout.addView(bookTypeSwitch)
    linearLayout.addView(bookTypeOptionCustomEdit)
    alertDialog {
        title = "屏蔽相关配置"
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