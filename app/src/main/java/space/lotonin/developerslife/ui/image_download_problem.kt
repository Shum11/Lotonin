package space.lotonin.developerslife.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageButton
import space.lotonin.developerslife.R
import space.lotonin.developerslife.data.ImageDownloadProblemClick


class image_download_problem : Fragment() {
    private val listeners = mutableListOf<ImageDownloadProblemClick>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageButton>(R.id.image_download_problem_btn)
            .setOnClickListener{notifyListeners()}
    }

    private fun notifyListeners() {
        listeners.forEach { it.imageDownloadProblem() }
    }

    fun addListeners(listener: ImageDownloadProblemClick) {
        listeners.add(listener)
    }
}