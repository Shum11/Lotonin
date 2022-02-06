package space.lotonin.developerslife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import space.lotonin.developerslife.data.Category
import space.lotonin.developerslife.databinding.ActivityMainBinding
import space.lotonin.developerslife.ui.content


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sectionsPagerAdapter: ScreenSlidePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setupPagerAdapter()
        setupButtons()
    }
    fun init() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupPagerAdapter() {
        sectionsPagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager, lifecycle, this)
        val viewPager: ViewPager2 = binding.viewPager
        val tabs: TabLayout = binding.TabLayout

        viewPager.adapter = sectionsPagerAdapter
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = "${Category.values()[(position)]}"
        }.attach()


        //for activity recreate
        sectionsPagerAdapter.fragments.clear()
        supportFragmentManager.fragments.forEach {
            if (it is content) {
                sectionsPagerAdapter.fragments.add(it.category.id, it)
            }
        }
    }

    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            sectionsPagerAdapter.fragments[binding.viewPager.currentItem]?.next()
        }
        binding.btnPrevious.setOnClickListener {
            sectionsPagerAdapter.fragments[binding.viewPager.currentItem]?.prev()
        }
    }
}