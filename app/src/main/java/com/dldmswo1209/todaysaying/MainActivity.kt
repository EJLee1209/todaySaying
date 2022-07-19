package com.dldmswo1209.todaysaying

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.dldmswo1209.todaysaying.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity() {
    // viewBinding
    var mBinding : ActivityMainBinding? = null
    val binding get() = mBinding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initData()
    }

    private fun initViews(){
        // 현재 페이지에서 다음 페이지로 넘길 때 명언에 여운을 남기는 효과를 주기 위함
        // alpha 값을 조정해서 text 의 투명도를 조절한다
        binding.viewPager.setPageTransformer { page, position ->
            Log.d("testt",position.toString())
            when{
                position.absoluteValue >= 1F -> { // 다음 페이지로 넘어감
                    page.alpha = 0F
                }
                position == 0F -> { // 화면의 정중앙
                    page.alpha = 1F
                }
                else ->{  // 현재 페이지와 다음 페이지의 중간(스크롤 중)
                    page.alpha = 1F - 2 * position.absoluteValue
                }
            }
        }
    }
    private fun initData(){
        val remoteConfig = Firebase.remoteConfig // FirebaseRemoteConfig 객체 생성
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0 // fetch 를 통해 데이터 업데이트를 위한 업데이트 간격 설정
            }
        )
        // 가져오기 및 활성화
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            binding.progressBar.visibility = View.GONE // progressBar 숨김(remote config 에서 가져오기가 끝나면)
            if(it.isSuccessful){
                // remoteConfig 에서 설정한 값을 가져옴
                val quotes = parseQuotesJson(remoteConfig.getString("quotes"))
                val isNameRevealed = remoteConfig.getBoolean("is_name_revealed")
                // remoteConfig 에서 가져온 값을 displayQuotesPager()에 전달
                displayQuotesPager(quotes, isNameRevealed) // 가져온 값들을 토대로 viewPager 어답터 설정

            }
        }
    }
    private fun parseQuotesJson(json: String) : List<Quote>{
        // 매개변수로 json 형태의 문자열을 받고, Quote 객체로 이루어진 리스트를 리턴
        val jsonArray = JSONArray(json)
        var jsonList = emptyList<JSONObject>()
        for(index in 0 until jsonArray.length()){
            val jsonObject = jsonArray.getJSONObject(index)
            jsonObject?.let {
                jsonList = jsonList + it
            }
        }

        return jsonList.map { // jsonList 는 JSONObject 형태이므로 매핑과정이 필요
            Quote(it.getString("quote"), it.getString("name"))
        }
    }
    private fun displayQuotesPager(quotes: List<Quote>, isNameRevealed: Boolean){
        val adapter = QuotesPagerAdapter(quotes, isNameRevealed)
        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(adapter.itemCount/2, false)
    }
}