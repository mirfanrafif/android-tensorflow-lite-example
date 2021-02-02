package com.example.hello

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.task.vision.detector.Detection

class DetectionAdapter(val detectionList : List<Detection>) :
        RecyclerView.Adapter<DetectionAdapter.DetectionViewHolder>() {
    class DetectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaText : TextView =  itemView.findViewById(R.id.namaItem)
        val akurasiText : TextView = itemView.findViewById(R.id.akurasiItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionAdapter.DetectionViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.detection_item, parent, false)
        return DetectionViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: DetectionAdapter.DetectionViewHolder, position: Int) {
        holder.namaText.setText(detectionList.get(position).categories.get(0).label)
        holder.akurasiText.setText(detectionList.get(position).categories.get(0).score.toString())
    }

    override fun getItemCount(): Int {
        return detectionList.size
    }
}