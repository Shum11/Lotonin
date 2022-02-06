package space.lotonin.developerslife

import android.app.Application
import space.lotonin.developerslife.data.Downloader

class App : Application() {
    val downloader by lazy { Downloader() }

}