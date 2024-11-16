package com.example.flash.ui

import android.app.Application
import android.app.LauncherActivity.ListItem
import android.content.Context
import android.preference.PreferenceDataStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flash.data.InternetItem
import com.example.flash.network.FlashApi
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FlashViewModel(application: Application): AndroidViewModel(application) {
  private val _uiState = MutableStateFlow(FlashUiState())
  val uiState : StateFlow<FlashUiState> = _uiState.asStateFlow()

  val _isVisible = MutableStateFlow<Boolean>(true)
  val isVisible = _isVisible

  val _user = MutableStateFlow<FirebaseUser?>(null)
  val user : MutableStateFlow<FirebaseUser?> get() = _user

  private val _phoneNumber = MutableStateFlow("")
  val phoneNumber : MutableStateFlow<String> get() = _phoneNumber


  var itemUiState : ItemUiState by mutableStateOf(ItemUiState.loading)
    private set

  private val _cartItems = MutableStateFlow<List<InternetItem>>(emptyList())
  val cartItems : StateFlow<List<InternetItem>> get() = _cartItems.asStateFlow()

  private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "cart")
  private val cartItemsKey = stringPreferencesKey("cart_items")

  private val _otp = MutableStateFlow("")
  val otp : MutableStateFlow<String> get() = _otp

  private val _ticks = MutableStateFlow(60L)
  val ticks : MutableStateFlow<Long> get() = _ticks

  private val _logoutClicked = MutableStateFlow(false)
  val logoutClicked : MutableStateFlow<Boolean> get() = _logoutClicked

  private lateinit var timerJob: Job

  private val _verificationId = MutableStateFlow("")
  val verificationId: MutableStateFlow<String> get() = _verificationId

  private val _loading = MutableStateFlow(false)
  val loading :MutableStateFlow<Boolean> get() = _loading

  val database = Firebase.database
  val myRef = database.getReference("users/${auth.currentUser?.uid}/cart")


  private val context = application.applicationContext


  private lateinit var internetJob : Job
//  private lateinit var screenJob : Job


  sealed interface ItemUiState{
    data class Success(val item: List<InternetItem>) : ItemUiState
    object loading : ItemUiState
    object error : ItemUiState
  }

  fun clearAllData(){
    _user.value = null
    _otp.value = ""
    _phoneNumber.value = ""
    _verificationId.value = ""
    resetTimer()
  }

  fun runTimer(){
    timerJob = viewModelScope.launch {
      while(_ticks.value > 0){
        delay(1000)
        _ticks.value -= 1
      }
    }
  }

   fun resetTimer(){
     try {
       timerJob.cancel()
     }catch (exception: Exception){

     }finally {
         _ticks.value = 60
     }
   }

  fun addToDatabase(item:InternetItem){
    myRef.push().setValue(item)
  }

  fun fillCartItems(){
    myRef.addValueEventListener(object : ValueEventListener {
      override fun onDataChange(dataSnapshot: DataSnapshot) {
        // This method is called once with the initial value and again
        // whenever data at this location is updated.
        _cartItems.value = emptyList()
        for(childSnapShop in dataSnapshot.children){
          val item = childSnapShop.getValue(InternetItem::class.java)
          item?.let {
            val newItem = it
            addToCart(newItem)
          }
        }
      }

      override fun onCancelled(error: DatabaseError) {
        // Failed to read value
      }
    })
  }

  fun setLogoutStatus(logoutStatus : Boolean){
    _logoutClicked.value = logoutStatus
  }

  fun setLoading(isLoading : Boolean){
    _loading.value = isLoading
  }

  fun setVerificatonId(verificationId:String){
    _verificationId.value = verificationId
  }

  fun sendOtp(otp:String){
    _otp.value = otp
  }

  fun setUser(user: FirebaseUser){
    _user.value = user
  }

  fun setPhoneNumber(phoneNumber:String){
    _phoneNumber.value = phoneNumber
  }

  private suspend fun loadCartItemsFromDataStore(){
    val fullData = context.dataStore.data.first()
    val cartItemJson = fullData[cartItemsKey]
    if(!cartItemJson.isNullOrEmpty()){
      _cartItems.value = Json.decodeFromString(cartItemJson)
    }

  }

  private suspend fun saveCartItmesToDataStore(){
    context.dataStore.edit { preferences ->
      preferences[cartItemsKey] = Json.encodeToString(_cartItems.value)
    }
  }

  fun addToCart(item:InternetItem){
    _cartItems.value += item
    viewModelScope.launch {
      saveCartItmesToDataStore()
    }
  }

  fun removeFromCart(oldItem:InternetItem){
//    _cartItems.value -= item
//    viewModelScope.launch {
//      saveCartItmesToDataStore()
//    }
    myRef.addListenerForSingleValueEvent(object : ValueEventListener{
      override fun onDataChange(dataSnapshot: DataSnapshot) {
        // This method is called once with the initial value and again
        // whenever data at this location is updated.
        for(childSnapShop in dataSnapshot.children){
          var itemRemoved = false
          val item = childSnapShop.getValue(InternetItem::class.java)
          item?.let {
           if (oldItem.itemName == it.itemName &&  oldItem.itemPrice == it.itemPrice){
             childSnapShop.ref.removeValue()
             itemRemoved  = true
           }
          }
          if (itemRemoved) break
        }
      }

      override fun onCancelled(error: DatabaseError) {
        // Failed to read value
      }
    })
  }

  fun updateClickText(updatedText : String){
    _uiState.update {
      it.copy(
        clickStatus = updatedText
      )
    }
  }


  fun updateSelectedCategory(updatedCategory: Int){
    _uiState.update {
      it.copy(
        selectedCategory =  updatedCategory
      )
    }
  }

  fun getFlashItems(){
     internetJob =  viewModelScope.launch {
       try {
         val listResult = FlashApi.retrofitService.getItems()
         itemUiState = ItemUiState.Success(listResult)
         loadCartItemsFromDataStore()
       }
       catch (exception: Exception) {
         itemUiState = ItemUiState.error
         toggleVisibility()
//         screenJob.cancel()
       }
    }

  }

  fun toggleVisibility(){
    _isVisible.value = false
  }

  init {
      viewModelScope.launch(Dispatchers.IO) {
        delay(3000)
        toggleVisibility()
      }
     getFlashItems()
    fillCartItems()
  }
}