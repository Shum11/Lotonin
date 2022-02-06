package space.lotonin.developerslife.data

import android.os.Parcelable
import org.json.JSONException
import org.json.JSONObject
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    val description: String,
    val gifURL: String,
    val previewURL: String,
    val id: Int,
    val votes: Int
):
        Parcelable {
            companion object{
                fun JsonObjToImage(json: JSONObject): Image {
                    val description = getString("description", json)
                    val gifURL = getString("gefURL", json)
                    val previewURL = getString("previewURL", json)
                    val id = getInt("id", json)
                    val votes = getInt("votes", json)

                    return Image(description, gifURL, previewURL, id, votes)
                }

                private fun getInt(name: String, json: JSONObject): Int {
                    return try{
                        json.getInt(name)
                    }
                    catch (e: JSONException){
                        0
                    }
                }

                private fun getString(name: String, json: JSONObject): String {
                    return try{
                        json.getString(name)
                    }
                    catch (e: JSONException){
                        ""
                    }
                }
                private fun getBoolean(name : String, json :JSONObject) : Boolean {
                    return try {
                        json.getBoolean(name)
                    }catch (e: JSONException){
                        false
                    }
                }


            }
}