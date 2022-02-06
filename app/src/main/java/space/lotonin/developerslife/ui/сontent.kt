package space.lotonin.developerslife.ui

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.glide.transformations.BlurTransformation
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import space.lotonin.developerslife.R
import space.lotonin.developerslife.data.*
import space.lotonin.developerslife.databinding.FragmentContentBinding
import java.io.IOException

private const val cache_size = 100

class content : Fragment(), ImageDownloadProblemClick  {
    private var _binding: FragmentContentBinding? = null
    private val binding get() = _binding!!

    lateinit var category: Category

    private var internetProblem = internet_problem()
    private var imageDownloadProblem = image_download_problem()
    private var contentProblem = content_problem()
    private var serverProblem = server_problem()

    private val downloader = Downloader()

    private var cache = ArrayList<Image>()
    private val currentContent : Image get() = cache[currentImageNumber]
    private var currentImageNumber = 0
    private var currentPage = 0

    private var state: State = State.INIT

    companion object {
        private const val ARG_CATEGORY = "section_category"
        private const val CACHE_KEY = "cache"
        private const val STATE_KEY = "state"
        private const val CURRENT_IMAGE_NUMBER_KEY = "current_image_number"
        private const val CURRENT_PAGE_KEY = "current_page"

        @JvmStatic
        fun newInstance(category: Category): content {
            return content().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CATEGORY, category)
                }
                this.category = category
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            category = (it.getSerializable(ARG_CATEGORY) ?: Category.LATEST) as Category
        }

        if (savedInstanceState != null) {
            cache = savedInstanceState.getParcelableArrayList(CACHE_KEY) ?: cache
            currentImageNumber = savedInstanceState.getInt(CURRENT_IMAGE_NUMBER_KEY)
            currentPage = savedInstanceState.getInt(CURRENT_PAGE_KEY)
            state = (savedInstanceState.getSerializable(STATE_KEY) ?: state) as State
        }
    }

    private fun initState() {
        when(state) {
            State.LOADING -> {
                if(currentImageNumber < cache_size) updateFragmentState()
                else getNewContent()
            }
            State.PROBLEM_IMAGE_DOWNLOAD -> childFragmentManager.findFragmentByTag(
                getFragmentTag(imageDownloadProblem)
            )?.let {
                imageDownloadProblem = it as image_download_problem
                updateContentDescriptionText()
            }

            State.PROBLEM_NO_CONTENT -> childFragmentManager.findFragmentByTag(
                getFragmentTag(contentProblem)
            )?.let {
                contentProblem = it as content_problem
                hideContentDescription()
            }

            State.PROBLEM_INTERNET -> childFragmentManager.findFragmentByTag(
                getFragmentTag(internetProblem)
            )?.let {
                internetProblem = it as internet_problem
                hideContentDescription()
            }

            State.PROBLEM_SERVER_ERROR -> childFragmentManager.findFragmentByTag(
                getFragmentTag(serverProblem)
            )?.let {
                serverProblem = it as server_problem
                hideContentDescription()
            }

            State.INIT -> getNewContent()

            else -> updateFragmentState()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initState()
        registerNetworkCallback(requireContext())
        imageDownloadProblem.addListeners(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_IMAGE_NUMBER_KEY, currentImageNumber)
        outState.putInt(CURRENT_PAGE_KEY, currentPage)
        outState.putParcelableArrayList(CACHE_KEY, cache)
        outState.putSerializable(STATE_KEY, state)
        super.onSaveInstanceState(outState)
    }
    

    private fun registerNetworkCallback(context: Context) {
        val manager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    activity?.runOnUiThread {
                        onNetworkAvailable()
                    }
                }
            })
    }

    private fun onNetworkAvailable() {
        if(state == State.PROBLEM_INTERNET)
            getNewContent()
    }

    private fun isCurrentContentHasGif(): Boolean = currentContent.gifURL.isNotBlank()

    private fun updateContentDescriptionText() {
        binding.contentDescription.text = currentContent.description
        showContentDescription()
    }

    private fun updateContentImageWithPreview() {
        showLoading()
        val loadedContentNumber = currentImageNumber
        Glide.with(this).load(currentContent.previewURL)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(10)))
            .into(object : CustomTarget<Drawable>(){
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    if(currentImageNumber != loadedContentNumber) return
                    if(isCurrentContentHasGif()) updateContentGifImage(resource)
                    else updateContentImage(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun updateContentImage(image: Drawable) {
        Glide.with(this).load(image).into(binding.contentView)
    }
    private fun updateContentGifImage(placeholder: Drawable? = null) {
        if(placeholder == null) showLoading()
        Glide.with(this).asGif()
            .listener(object: RequestListener<GifDrawable?> {
                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    hideAllProblemAndLoading()
                    resource?.start()
                    state = State.OK

                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    showImageDownloadProblem()
                    return false
                }
            })
            .load(currentContent.gifURL)
            .placeholder(placeholder)
            .fitCenter()
            .into(binding.contentView)
    }
    fun updateFragmentState() {
        updateContentDescriptionText()
        updateContentImageWithPreview()
    }
    private fun decrementCurrentImageNumber() {
        if(currentImageNumber!=0) currentImageNumber --
    }
    private fun showContentDescription() {
        binding.contentDescription.visibility = View.VISIBLE
    }
    private fun hideContentDescription() {
        binding.contentDescription.visibility = View.INVISIBLE
    }
    private fun hideContentDescriptionAndImage() {
        hideContentDescription()
        hideContentImage()
    }
    private fun hideContentImage(){
        binding.contentView.setImageDrawable(null)
    }
    private fun hideAllProblemAndLoading(){
        hideLoading()
        hideAllProblem()
    }
    private fun hideAllProblem() {
        hideImageDownloadProblem()
        hideContentProblem()
        hideInternetProblem()
        hideServerErrorProblem()
    }
    private fun showLoading() {
        hideAllProblem()
        binding.loading.visibility = View.VISIBLE
        state = State.LOADING
    }
    private fun hideLoading() {
        binding.loading.visibility = View.GONE
    }
    private fun showInternetProblem() {
        if (state != State.PROBLEM_INTERNET) {
            hideAllProblemAndLoading()
            hideContentDescriptionAndImage()
            addFragment(internetProblem)
            state = State.PROBLEM_INTERNET
        }
    }
    private fun hideInternetProblem() {
        if(state == State.PROBLEM_INTERNET)
            removeFragment(internetProblem)
    }
    private fun showContentProblem() {
        if(!contentProblem.isAdded) {
            hideAllProblemAndLoading()
            hideContentDescriptionAndImage()
            addFragment(contentProblem)
            state = State.PROBLEM_NO_CONTENT
        }
    }
    private fun hideContentProblem() {
        if(contentProblem.isAdded)
            removeFragment(contentProblem)
    }
    private fun  showServerErrorProblem() {
        if(!serverProblem.isAdded) {
            hideAllProblemAndLoading()
            hideContentDescriptionAndImage()
            addFragment(serverProblem)
            state = State.PROBLEM_SERVER_ERROR
        }
    }
    private fun hideServerErrorProblem() {
        if (serverProblem.isAdded)
            removeFragment(serverProblem)
    }
    private fun showImageDownloadProblem() {
        if(!imageDownloadProblem.isAdded) {
            hideAllProblemAndLoading()
            hideContentImage()
            addFragment(imageDownloadProblem)
            state = State.PROBLEM_IMAGE_DOWNLOAD
        }
    }
    private fun hideImageDownloadProblem() {
        if (imageDownloadProblem.isAdded)
            removeFragment(imageDownloadProblem)
    }
    private fun addFragment(fr: Fragment) {
        childFragmentManager.beginTransaction()
            .add(R.id.content, fr, getFragmentTag(fr))
            .commitNow()
    }
    private fun removeFragment(fr: Fragment) {
        childFragmentManager.beginTransaction().remove(fr).commitNow()
    }
    fun next() {
        if(cache.size !=0 && cache.size != currentImageNumber) currentImageNumber++
        if(cache.size > currentImageNumber) {
            updateFragmentState()
        }else {
            if(category != Category.LATEST && cache.size !=0) currentPage++
            getNewContent()
        }
    }
    fun prev() {
        if (currentImageNumber==0) Toast.makeText(
            context,
            getString(R.string.first_image),
            Toast.LENGTH_SHORT
        ).show()
        else{
            decrementCurrentImageNumber()
            updateFragmentState()
        }
    }
    private fun getNewContent() {
        hideContentDescriptionAndImage()
        showLoading()
        downloader.getData(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    processInternetError()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    if(response.isSuccessful && response.body !=null) {
                        val jsonObj = JSONObject(response.body!!.string())
                        if(!processResponse(jsonObj)) {
                            processInternetError()
                            return@runOnUiThread
                        }
                        if (cache.size>0) updateFragmentState()
                        else processImageError()
                    }else processServerError()
                }
            }
        }, category, currentPage)
    }
    private fun processDownloadError() {
        if (cache.size <= currentImageNumber) currentImageNumber = cache.size + 1
        decrementCurrentImageNumber()
        if(category != Category.LATEST) if (currentPage !=0) currentPage--
    }
    private fun processInternetError() {
        processDownloadError()
        showInternetProblem()
    }
    private fun processServerError() {
        processDownloadError()
        showServerErrorProblem()
    }

    private fun processImageError() {
        showContentProblem()
        currentPage = 0
        decrementCurrentImageNumber()
    }

    fun processResponse(json: JSONObject): Boolean {
        if(category == Category.LATEST) {
            val image =  Image.JsonObjToImage(json)
            if (image.gifURL.isEmpty()) return false
            return cache.add(image)
        }else{
            val jsonArray = json.getJSONArray("result")
            for (i in 0 until jsonArray.length()) {
                cache.add(Image.JsonObjToImage(jsonArray.getJSONObject(i)))
            }
            return true
        }
    }
    private fun getFragmentTag(fr: Fragment) = "${fr.javaClass.simpleName}$currentPage"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun imageDownloadProblem() {
        updateFragmentState()
    }

}
