package com.oliveme.app.ui.screens

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class EvidenceFragment : Fragment()

@Composable
fun LegacyJetpackEvidence() {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.height(1.dp),
        factory = {
            DrawerLayout(context).apply {
                visibility = View.GONE
                val content = FrameLayout(context)
                val pager = ViewPager2(context).apply {
                    adapter = EvidencePagerAdapter()
                }
                val recycler = RecyclerView(context).apply {
                    adapter = EvidenceRecyclerAdapter()
                }
                content.addView(pager, FrameLayout.LayoutParams(1, 1))
                content.addView(recycler, FrameLayout.LayoutParams(1, 1))
                addView(content, DrawerLayout.LayoutParams(1, 1))
            }
        },
    )
}

private class EvidencePagerAdapter : RecyclerView.Adapter<EvidenceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvidenceViewHolder =
        EvidenceViewHolder(TextView(parent.context))

    override fun onBindViewHolder(holder: EvidenceViewHolder, position: Int) {
        holder.text.text = "ViewPager2 evidence $position"
    }

    override fun getItemCount(): Int = 4
}

private class EvidenceRecyclerAdapter : RecyclerView.Adapter<EvidenceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvidenceViewHolder =
        EvidenceViewHolder(TextView(parent.context))

    override fun onBindViewHolder(holder: EvidenceViewHolder, position: Int) {
        holder.text.text = "RecyclerView evidence $position"
    }

    override fun getItemCount(): Int = 6
}

private class EvidenceViewHolder(val text: TextView) : RecyclerView.ViewHolder(text)
