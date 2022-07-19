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