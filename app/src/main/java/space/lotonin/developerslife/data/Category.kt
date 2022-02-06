package space.lotonin.developerslife.data

import space.lotonin.developerslife.R

enum class Category (val urlParam:String, val resourceId: Int, val id: Int) {
    LATEST("random", R.string.latest,0),
    HOT("hot", R.string.hot,1),
    TOP("top", R.string.top, 2);

    companion object {
        private val mapIds = values().associateBy(Category::id)

        fun byId(id: Int) = mapIds[id] ?: LATEST
    }
}
