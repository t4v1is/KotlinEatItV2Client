package com.example.kotlineatitv2client.ui.fooddetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Model.CommentModel
import com.example.kotlineatitv2client.Model.FoodModel
import com.example.kotlineatitv2client.R
import com.example.kotlineatitv2client.ui.comment.CommentFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog


class FoodDetailFragment : Fragment() {

    private lateinit var foodDetailViewModel: FoodDetailViewModel

    private var img_food: ImageView? = null
    private var btnCart: CounterFab? = null
    private var btnRating: FloatingActionButton? = null
    private var food_name: TextView? = null
    private var food_description: TextView? = null
    private var food_price: TextView? = null
    private var number_button: ElegantNumberButton? = null
    private var ratingBar: RatingBar? = null
    private var btnShowComment: Button? = null
    private var rdi_group_size: RadioGroup? = null

    private var waitingDialog : android.app.AlertDialog?=null

    @SuppressLint("FragmentLiveDataObserve")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodDetailViewModel =
            ViewModelProviders.of(this).get(FoodDetailViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_detail, container, false)
        initViews(root)

        foodDetailViewModel.getMutableLiveDataFood().observe(this, Observer {
            displayInfo(it)
        })

        foodDetailViewModel.getMutableLiveDataComment().observe(this, Observer {
            submitRatingToFirebase(it)
        })
        return root
    }

    private fun submitRatingToFirebase(commentModel: CommentModel?) {
        waitingDialog!!.show()

        FirebaseDatabase.getInstance()
            .getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id!!)
            .push()
            .setValue(commentModel)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    addRatingToFood(commentModel!!.ratingValue.toDouble())

                }
                waitingDialog!!.dismiss()
            }
    }

    private fun addRatingToFood(ratingValue: Double) {
        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF) //Selected menu in category
            .child(Common.categorySelected!!.menu_id!!)// Select menu in category
            .child("foods") //Selected food array
            .child(Common.foodSelected!!.key!!) //Select key
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    waitingDialog!!.dismiss()
                    Toast.makeText(context!!, ""+p0.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(dataSnapshot : DataSnapshot) {
                    if(dataSnapshot.exists()){
                        val foodModel = dataSnapshot.getValue(FoodModel::class.java)
                        foodModel!!.key = Common.foodSelected!!.key
                        //Apply rating
                        val sumRating = foodModel.ratingValue!!.toDouble() + (ratingValue)
                        val ratingCount = foodModel.ratingCount + 1
                        val result = sumRating/ratingCount

                        val updateData = HashMap<String, Any>()
                        updateData["ratingValue"] = result
                        updateData["ratingCount"] = ratingCount

                        //Update data in variable
                        foodModel.ratingCount = ratingCount
                        foodModel.ratingValue = result

                        dataSnapshot.ref
                            .updateChildren(updateData)
                            .addOnCompleteListener{task ->
                                waitingDialog!!.dismiss()
                                if(task.isSuccessful)   {
                                    Common.foodSelected = foodModel
                                    foodDetailViewModel!!.setFoodModel(foodModel)
                                    Toast.makeText(context!!, "Gracias", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }else
                        waitingDialog!!.dismiss()
                }

            })

    }

    private fun displayInfo(it: FoodModel?) {
        Glide.with(context!!).load(it!!.image).into(img_food!!)
        food_name!!.text = StringBuilder(it!!.name!!)
        food_description!!.text = StringBuilder(it!!.desciption!!)
        food_price!!.text = StringBuilder(it!!.price!!.toString())

        ratingBar!!.rating = it!!.ratingValue.toFloat()

        //Dar tamanño
        for(sizeModel in  it!!.size){
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compoundButton, b ->
                if (b)
                    Common.foodSelected!!.userSelectedSize = sizeModel
                calculateTotalPrice()

            }

            val params = LinearLayout.LayoutParams (0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price

            rdi_group_size!!.addView(radioButton)
        }

        // Default first radio button select
        if(rdi_group_size!!.childCount > 0){
            val radioButton = rdi_group_size!!.getChildAt(0) as RadioButton
            radioButton.isChecked = true
        }
    }

    private fun calculateTotalPrice() {
        var totalPrice = Common.foodSelected!!.price.toDouble()
        var displayPrice = 0.0

        //Size
        totalPrice += Common.foodSelected!!.userSelectedSize!!.price!!.toDouble()
        displayPrice = totalPrice * number_button!!.number.toInt()
        displayPrice = Math.round(displayPrice * 100.0)/100.0

        food_price!!.text = StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
    }

    private fun initViews(root: View?) {

        waitingDialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()

        btnCart = root!!.findViewById(R.id.btnCart) as CounterFab
        img_food = root!!.findViewById(R.id.img_food) as ImageView
        btnRating = root!!.findViewById(R.id.btn_rating) as FloatingActionButton
        food_name = root!!.findViewById(R.id.food_name) as TextView
        food_description = root!!.findViewById(R.id.food_description) as TextView
        food_price = root!!.findViewById(R.id.food_price) as TextView
        number_button = root!!.findViewById(R.id.number_button) as ElegantNumberButton
        ratingBar = root!!.findViewById(R.id.ratingBar) as RatingBar
        btnShowComment = root!!.findViewById(R.id.btnShowComment) as Button
        rdi_group_size = root!!.findViewById(R.id.rdi_group_size) as RadioGroup

        //Event
        btnRating!!.setOnClickListener {
            showDialogRating()
        }

        btnShowComment!!.setOnClickListener{
            val commentFragment = CommentFragment.getInstance()
            commentFragment.show(activity!!.supportFragmentManager, "CommentFragment")
        }
    }

    private fun showDialogRating() {
        var builder = AlertDialog.Builder(context!!)
        builder.setTitle("Rating Food")
        builder.setMessage("Por favor llena la informacion")

        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_rating_comment, null)

        val ratingBar = itemView.findViewById<RatingBar>(R.id.rating_bar)
        val edt_comment = itemView.findViewById<EditText>(R.id.edt_comment)

        builder.setView(itemView)

        builder.setNegativeButton("CANCELAR"){dialogInterface, i -> dialogInterface.dismiss() }

        builder.setPositiveButton("OK"){dialogInterface, i ->
            val commentModel = CommentModel()
            commentModel.name = Common.currentUser!!.name
            commentModel.uid = Common.currentUser!!.uid
            commentModel.comment = edt_comment.text.toString()
            commentModel.ratingValue = ratingBar.rating
            val serverTimeStamp = HashMap<String, Any>()
            serverTimeStamp["timeStamp"] = ServerValue.TIMESTAMP
            commentModel.commentTimeStamp = (serverTimeStamp)

            foodDetailViewModel!!.setCommentModel(commentModel)

        }

        val dialog = builder.create()
        dialog.show()
    }

}