package edu.cram.mentoriapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.UserView
import edu.cram.mentoriapp.R

class MentorAdapter(
    private val mentors: List<UserView>,
    private val onItemClick: (UserView) -> Unit
) : RecyclerView.Adapter<MentorAdapter.MentorViewHolder>() {

    inner class MentorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mentorName: TextView = itemView.findViewById(R.id.mentor_name)
        private val mentorSemester: TextView = itemView.findViewById(R.id.mentor_semester)

        fun bind(mentor: UserView) {
            mentorName.text = mentor.fullName
            mentorSemester.text = "Semestre: ${mentor.semester}"

            itemView.setOnClickListener { onItemClick(mentor) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mentor_item, parent, false)
        return MentorViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentorViewHolder, position: Int) {
        holder.bind(mentors[position])
    }

    override fun getItemCount(): Int = mentors.size
}
