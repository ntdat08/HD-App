package com.example.hdapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hdapp.databinding.ActivityPayOutBinding
import com.example.hdapp.model.OrderDetails
import com.example.hdapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class PayOutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPayOutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var clientSecret: String
    private var paymentMethod: String = "cash"
    private lateinit var userId: String
    private lateinit var names: String
    private lateinit var address: String
    private lateinit var numbers: String
    private lateinit var totalAmounts: String
    private lateinit var foodItemName: ArrayList<String>
    private lateinit var foodItemPrice: ArrayList<String>
    private lateinit var foodItemDescription: ArrayList<String>
    private lateinit var foodItemImage: ArrayList<String>
    private lateinit var foodItemIngredient: ArrayList<String>
    private lateinit var foodItemQuantities: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Initialize Stripe
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51RNGoQGbf5ivacEWVhS6HON8C3KSSMhilmBXIHcaUbvKAyEBFyRvlYlLJrVOjmcGtfJwAa1hOWP8NkYwTMv1rmbG00fgChNMq9"
        )

        // Set user data
        setUserData()

        // Get order details from intent
        foodItemName = intent.getStringArrayListExtra("FoodItemName") as ArrayList<String>
        foodItemPrice = intent.getStringArrayListExtra("FoodItemPrice") as ArrayList<String>
        foodItemImage = intent.getStringArrayListExtra("FoodItemImage") as ArrayList<String>
        foodItemDescription = intent.getStringArrayListExtra("FoodItemDescription") as ArrayList<String>
        foodItemIngredient = intent.getStringArrayListExtra("FoodItemIngredient") as ArrayList<String>
        foodItemQuantities = intent.getIntegerArrayListExtra("FoodItemQuantities") as ArrayList<Int>

        totalAmounts = "${calculateTotalAmount()} VND"
        binding.totalAmount.text = totalAmounts

        // Handle payment method selection
        binding.paymentMethodGroup.setOnCheckedChangeListener { _, checkedId ->
            paymentMethod = when (checkedId) {
                R.id.radioCash -> "cash"
                R.id.radioStripe -> "stripe"
                else -> "cash"
            }
        }

        // Back button
        binding.imageView7.setOnClickListener {
            hideKeyboard()
            finish()
        }

        // Place order button
        binding.placeOrder.setOnClickListener {
            hideKeyboard()
            names = binding.name.text.toString().trim()
            address = binding.address.text.toString().trim()
            numbers = binding.number.text.toString().trim()

            if (names.isBlank() || address.isBlank() || numbers.isBlank()) {
                Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT).show()
            } else {
                if (paymentMethod == "stripe") {
                    fetchPaymentIntent()
                } else {
                    placeOrder()
                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun fetchPaymentIntent() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val amount = calculateTotalAmount()
                if (amount <= 0) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PayOutActivity, "Invalid order amount", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                val body = JSONObject().apply {
                    put("amount", amount)
                    put("currency", "vnd")
                }
                Log.d("PayOutActivity", "Request Body: $body")

                val request = Request.Builder()
                    .url("https://food-app-backend-mtso.onrender.com/create-payment-intent")
                    .post(body.toString().toRequestBody(mediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string() ?: ""
                val contentType = response.header("Content-Type", "text/plain")
                val responseCode = response.code

                // Log response for debugging
                Log.d("PayOutActivity", "Response Code: $responseCode")
                Log.d("PayOutActivity", "Content-Type: $contentType")
                Log.d("PayOutActivity", "Response Body: $responseData")

                if (responseCode != 200) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@PayOutActivity,
                            "Server error ($responseCode). Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                if (contentType != null) {
                    if (!contentType.contains("application/json")) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@PayOutActivity,
                                "Unexpected response format. Check backend configuration.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }
                }

                val json = JSONObject(responseData)
                if (!json.has("clientSecret")) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@PayOutActivity,
                            "Missing clientSecret in response",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                clientSecret = json.getString("clientSecret")

                withContext(Dispatchers.Main) {
                    presentPaymentSheet()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PayOutActivity, "Payment error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                Log.e("PayOutActivity", "Payment error", e)
            }
        }
    }

    private fun presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "HD Food App"
            )
        )
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                placeOrder(paymentReceived = true)
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "Payment failed: ${paymentSheetResult.error.message}", Toast.LENGTH_LONG).show()
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun placeOrder(paymentReceived: Boolean = false) {
        userId = auth.currentUser?.uid ?: ""
        val time = System.currentTimeMillis()
        val itemPushKey = databaseReference.child("OrderDetails").push().key
        val orderDetails = OrderDetails(
            userId = userId,
            names = names,
            foodItemName = foodItemName,
            foodItemImage = foodItemImage,
            foodItemPrice = foodItemPrice,
            foodItemQuantities = foodItemQuantities,
            address = address,
            numbers = numbers,
            time = time,
            itemPushKey = itemPushKey,
            orderAccepted = false,
            paymentReceived = paymentReceived,
            totalPrice = totalAmounts,
            paymentMethod = paymentMethod
        )
        val orderReference = databaseReference.child("OrderDetails").child(itemPushKey!!)
        orderReference.setValue(orderDetails).addOnSuccessListener {
            val bottomSheetDialog = CongratsBottomSheet()
            bottomSheetDialog.show(supportFragmentManager, "Test")
            removeItemFromCart()
            addItemToHistory(orderDetails)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to order", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addItemToHistory(orderDetails: OrderDetails) {
        databaseReference.child("Users").child(userId).child("BuyHistory")
            .child(orderDetails.itemPushKey!!)
            .setValue(orderDetails).addOnSuccessListener {}
    }

    private fun removeItemFromCart() {
        val cartItemReference = databaseReference.child("Users").child(userId).child("CartItems")
        cartItemReference.removeValue()
    }

    private fun calculateTotalAmount(): Int {
        var totalAmount = 0
        for (i in 0 until foodItemPrice.size) {
            val price = foodItemPrice[i]
            val priceIntValue = price.filter { it.isDigit() }.takeIf { it.isNotEmpty() }?.toIntOrNull() ?: 0
            val quantity = foodItemQuantities[i]
            totalAmount += priceIntValue * quantity
        }
        return totalAmount
    }

    private fun setUserData() {
        val user = auth.currentUser
        if (user != null) {
            userId = user.uid
            val userReference = databaseReference.child("Users").child(userId)
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userModel = snapshot.getValue(UserModel::class.java)
                        userModel?.let {
                            binding.apply {
                                name.setText(it.name ?: "")
                                address.setText(it.address ?: "")
                                number.setText(it.number ?: "")
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PayOutActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}