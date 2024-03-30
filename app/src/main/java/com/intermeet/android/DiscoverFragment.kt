package com.intermeet.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.intermeet.android.helperFunc.calculateAgeWithCalendar

class DiscoverFragment : Fragment() {

    private lateinit var textViewName: TextView
    private lateinit var cardView: CardView
    private lateinit var cardView2: CardView
    private lateinit var tvEducation: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvPronouns: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvAboutMeHeader: TextView
    private lateinit var tvAboutMe: TextView
    private lateinit var tvInterestsHeader: TextView
    private lateinit var tvEthnicity: TextView
    private lateinit var relativeLayout: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_discover, container, false)

        textViewName = view.findViewById(R.id.textViewName)
        cardView = view.findViewById(R.id.cardView)
        cardView2 = view.findViewById(R.id.cardView2)
        tvEducation = view.findViewById(R.id.tvEducation)
        tvLocation = view.findViewById(R.id.tvLocation)
        tvPronouns = view.findViewById(R.id.tvPronouns)
        tvGender = view.findViewById(R.id.tvGender)
        tvAboutMeHeader = view.findViewById(R.id.tvAboutMeHeader)
        tvAboutMe = view.findViewById(R.id.tvAboutMe)
        tvInterestsHeader = view.findViewById(R.id.tvInterestsHeader)
        tvEthnicity = view.findViewById(R.id.tvEthnicity)

        relativeLayout = view.findViewById(R.id.relativeLayout)


        cardView.setOnClickListener {
            toggleCardViewsVisibility()
        }

        fetchDataFromFirebase()

        return view
    }
    private fun createCardView(imageUrl: String): CardView {
        val context = requireContext()
        val cardView = CardView(context).apply {
            radius = resources.getDimension(R.dimen.card_corner_radius)
            cardElevation = resources.getDimension(R.dimen.card_elevation)

            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // Set similar margins as the first CardView
                val margin = resources.getDimensionPixelSize(R.dimen.card_margin)
                setMargins(margin, margin, margin, margin)
            }
            this.layoutParams = layoutParams
        }

        val imageView = ImageView(context).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.image_view_height)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        // Use Glide to load the image into the ImageView
        Glide.with(this@DiscoverFragment)
            .load(imageUrl)
            .into(imageView)

        // Add the ImageView to the CardView
        cardView.addView(imageView)

        return cardView
    }

    private fun createTextCardView(prompt: String): CardView {
        val cardView = CardView(requireContext()).apply {
            radius = resources.getDimension(R.dimen.card_corner_radius)
            cardElevation = resources.getDimension(R.dimen.card_elevation)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(resources.getDimensionPixelSize(R.dimen.card_margin))
            }
        }

        val textView = TextView(requireContext()).apply {
            text = prompt
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
            }
        }

        cardView.addView(textView)

        return cardView
    }


    private fun fetchDataFromFirebase() {
        val userId = "a6TWxI1076ahgLhZFOaHmNPRbom2" // Replace with dynamic user ID as needed
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserDataModel::class.java)
                user?.let {
                    textViewName.text = "${it.firstName}, ${calculateAgeWithCalendar(it.birthday)}"
                    tvEducation.text = "${it.school}"
                    tvLocation.text = "${it.longitude}"
                    tvPronouns.text = "${it.pronouns}"
                    tvGender.text = "${it.gender}"
                    tvAboutMe.text = "${it.aboutMeIntro}"
                    tvEthnicity.text ="${it.ethnicity}"
                    tvLocation.text = "${it.latitude}, ${it.longitude}"

                    val imageView1 = view?.findViewById<ImageView>(R.id.imageView1)
                    if (imageView1 != null) {
                        Glide.with(this@DiscoverFragment)
                            .load(it.photoDownloadUrls.first()) // Load the first image URL into the ImageView1
                            .centerCrop()
                            .into(imageView1)
                    }
                    val imageUrls = it.photoDownloadUrls.drop(1) // Drops the first element

                    var belowId = R.id.cardView2 // Initialize with cardView2's ID

                    imageUrls.forEachIndexed { index, imageUrl ->
                        val newCardView = createCardView(imageUrl)
                        val layoutParams = RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            addRule(RelativeLayout.BELOW, belowId)
                            setMargins(resources.getDimensionPixelSize(R.dimen.card_margin))
                        }
                        newCardView.layoutParams = layoutParams
                        relativeLayout.addView(newCardView)

                        belowId = View.generateViewId()
                        newCardView.id = belowId

                        if (it.prompts.size > index) {
                            val textCardView = createTextCardView(it.prompts[index])
                            relativeLayout.addView(textCardView)

                            // Set positioning for the text CardView
                            val textLayoutParams = textCardView.layoutParams as RelativeLayout.LayoutParams
                            textLayoutParams.addRule(RelativeLayout.BELOW, belowId)
                            textCardView.layoutParams = textLayoutParams

                            // Update belowId for the next CardView
                            belowId = View.generateViewId()
                            textCardView.id = belowId
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun toggleCardViewsVisibility() {
        var delayIncrement = 50L // Increment delay by 50ms for each card
        var delay = 0L // Initial delay for the first card

        for (i in 1 until relativeLayout.childCount) {
            val child = relativeLayout.getChildAt(i)
            if (child is CardView) {
                if (child.visibility == View.VISIBLE) {
                    child.postDelayed({
                        val slideOut = AnimationUtils.loadAnimation(context, R.anim.slide_out)
                        child.startAnimation(slideOut)
                        child.visibility = View.GONE
                    }, delay)
                    delay += delayIncrement // Increment delay for the next card
                } else {
                    child.visibility = View.VISIBLE
                    val slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in)
                    child.startAnimation(slideIn)
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = DiscoverFragment()
    }
}