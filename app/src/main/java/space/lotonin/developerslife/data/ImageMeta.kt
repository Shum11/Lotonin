package space.lotonin.developerslife.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
open class ImageMeta(val description: String, val gifURL: String) : Parcelable {
    companion object {
        fun jsonObjectToImageMeta(json: JSONObject): ImageMeta {
            val description = getString("description", json)
            val gifURL = getString("gifURL", json)


            return ImageMeta(description, gifURL)
        }

        private fun getString(name: String, json: JSONObject): String {
            return try {
                json.getString(name)
            } catch (e: JSONException) {
                ""
            }
        }

        private fun getBoolean(name: String, json: JSONObject): Boolean {
            return try {
                json.getBoolean(name)
            } catch (e: JSONException) {
                false
            }
        }

        private fun getInt(name: String, json: JSONObject): Int {
            return try {
                json.getInt(name)
            } catch (e: JSONException) {
                0
            }
        }
    }


}