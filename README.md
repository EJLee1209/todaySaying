# todaySaying
오늘의 명언 앱

# 완성 화면
![](https://velog.velcdn.com/images/dldmswo1209/post/460df5dc-16dd-41ce-8e17-5ab9e726dc2c/image.png)

## activity_main 레이아웃
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"/>

</FrameLayout>
```
화면을 스와이프 해서 다음 명언을 볼 수 있게 만들기 위해서 ViewPager2를 사용했다. 
Firebase의 remote config를 사용해서 명언을 불러올 것이기 때문에 불러오는 동안 ProgressBar를 띄우기 위해 ProgressBar도 추가했다.
## item_quote 레이아웃
```
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <TextView
        android:id="@+id/quoteTextView"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:textSize="30dp"
        android:maxLines="6"
        android:ellipsize="end"
        android:layout_marginHorizontal="40dp"
        app:layout_constraintVertical_bias="0.4"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@+id/nameTextView"
        app:layout_constraintVertical_chainStyle="packed"
        android:gravity="end|center_vertical"
        tools:text="나는 생각한다 고로 나는 존재한다."/>

    <TextView
        android:id="@+id/nameTextView"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        tools:text="데카르트"
        android:textSize="20dp"
        android:maxLines="1"
        android:ellipsize="end"
        android:layout_marginTop="15dp"
        android:gravity="end|center_vertical"
        app:layout_constraintTop_toBottomOf="@id/quoteTextView"
        app:layout_constraintStart_toStartOf="@id/quoteTextView"
        app:layout_constraintEnd_toEndOf="@id/quoteTextView"
        app:layout_constraintBottom_toBottomOf="parent"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```
ViewPager에 리사이클러뷰를 통해 명언을 보여줄 것이기 때문에 어떤 형태로 보여줄 것인지 item_quote.xml이 필요하다.
## Quote.kt(data class)
```
// 명언, 명언을 남긴 위인의 이름
data class Quote(
    val quote: String,
    val name: String
)

```
## QuotePagerAdapter.kt
```
package com.dldmswo1209.todaysaying

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// ViewPager 에 리사이클러뷰를 적용시키기 위한 어답터
class QuotesPagerAdapter(
    private val quotes: List<Quote>,
    private val isNameRevealed: Boolean
): RecyclerView.Adapter<QuotesPagerAdapter.QuoteViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        QuoteViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_quote, parent, false)
        )

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val actualPosition = position % quotes.size // 실제 position을 계산 후
        // 만약 quotes에 5개의 명언이 있고 사용자가 viewPager 의 4번째 페이지(마지막 명언)에서 다음 페이지로 넘긴 경우
        // position = 5, quotes.size = 5
        // 5%5 = 0 -> 0번째 페이지(최초 페이지)를 보여줌
        holder.bind(quotes[actualPosition], isNameRevealed) // bind
    }

    override fun getItemCount() = Int.MAX_VALUE // 명언을 무한으로 보여주는 것처럼 만들기 위해서

    inner class QuoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val quoteTextView: TextView = itemView.findViewById(R.id.quoteTextView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)


        @SuppressLint("SetTextI18n")
        fun bind(quote: Quote, isNameRevealed: Boolean){
            quoteTextView.text = "\"${quote.quote}\""

            if(isNameRevealed) {
                nameTextView.text = "- ${quote.name}"
                nameTextView.visibility = View.VISIBLE
            }else{
                nameTextView.visibility = View.GONE
            }
        }
    }

}
```
## MainActivity.kt
```
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
```
### 앱을 만들면서 배운점
ViewPager2에 RecyclerView를 적용하는 방법에 대해서 복습할 수 있었고, Firebase의 RemoteConfig의 사용법을 익힐 수 있었다.

