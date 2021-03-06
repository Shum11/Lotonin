package space.lotonin.developerslife

import space.lotonin.developerslife.Adapter.ScreenSlidePagerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import space.lotonin.developerslife.data.Category
import space.lotonin.developerslife.databinding.ActivityMainBinding
import space.lotonin.developerslife.ui.MemFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager, lifecycle, this)
        val viewPager: ViewPager2 = binding.viewPager
        val tabs: TabLayout = binding.tabs

        viewPager.adapter = sectionsPagerAdapter
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = "${Category.values()[(position)]}"
        }.attach()

        supportFragmentManager.fragments.forEach {
            if (it is MemFragment) {
                sectionsPagerAdapter.fragments.add(it.categoryNumber - 1, it)
            }
        }
        binding.btnNext.setOnClickListener {
            sectionsPagerAdapter.fragments[viewPager.currentItem]!!.nextImage()
        }
        binding.btnPrevious.setOnClickListener {
            sectionsPagerAdapter.fragments[viewPager.currentItem]!!.prevImage()
        }
    }

}