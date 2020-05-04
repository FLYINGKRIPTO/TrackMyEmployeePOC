package trackemployee.io.workmanager.ui.base

import androidx.recyclerview.widget.RecyclerView
import android.view.View

open class BaseViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    open fun bind() {}
}