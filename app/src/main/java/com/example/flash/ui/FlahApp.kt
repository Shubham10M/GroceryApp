package com.example.flash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.flash.data.InternetItem
import com.google.firebase.auth.FirebaseAuth

enum class FlashAppScreen(val title: String){
    Start("FlashCart"),
    Items("Choose Items"),
    Cart("Your Cart")
}
val auth = FirebaseAuth.getInstance()
var canNavigateBack = false
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashApp(
    flashViewModel: FlashViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),

){

    val user by flashViewModel.user.collectAsState()
     auth.currentUser?.let { flashViewModel.setUser(it) }
    val isVisible by flashViewModel.isVisible.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val logoutClicked by flashViewModel.logoutClicked.collectAsState()
    val currentScreen  = FlashAppScreen.valueOf(
        backStackEntry?.destination?.route ?: FlashAppScreen.Start.name
    )

canNavigateBack = navController.previousBackStackEntry != null
val cartItems by flashViewModel.cartItems.collectAsState()

    if(isVisible){
        OfferScreen()
    }
    else if(user == null){
        LoginUi(flashViewModel = flashViewModel);
    }
    else{
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Blue,
                    ),
                    title = {
                        Row (
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ){
                            Row {
                                Text(
                                    text = currentScreen.title,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (currentScreen == FlashAppScreen.Cart) {
                                    Text(
                                        text = "(${cartItems.size})",
                                        fontSize = 20.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.clickable {
//                                    auth.signOut()
//                                    flashViewModel.clearAllData()
                                    flashViewModel.setLogoutStatus(true)
                                }
                            ) {
                               Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                   contentDescription = "logout", modifier = Modifier.size(24.dp))
                                Text(text = "Logout",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(
                                        end = 14.dp,
                                        start = 4.dp
                                    )
                                )
                            }
                        }

                    },
                    navigationIcon = {
                        if (canNavigateBack) {
                            IconButton(onClick = {
                                navController.navigateUp()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back Button"
                                )
                            }
                        }
                    },

                    )
            },
            bottomBar = {
                FlashAppBar(
                    navController = navController,
                    currentScreen = currentScreen,
                    cartItems = cartItems
                )
            }
        ) {
            NavHost(navController = navController,
                startDestination = FlashAppScreen.Start.name,
                Modifier.padding(it)
            ) {
                composable(route = FlashAppScreen.Start.name){
                    StartScreen(
                        flashViewModel = flashViewModel,
                        onCategoryClicked = {
                            flashViewModel.updateSelectedCategory(it)
                            navController.navigate(FlashAppScreen.Items.name)
                        }
                    )
                }
                composable(route = FlashAppScreen.Items.name){
                    InternetItemsScreen(
                        flashViewModel = flashViewModel,
                        itemUiState = flashViewModel.itemUiState
                    )
                }

                composable(route = FlashAppScreen.Cart.name){
                    CartScreen(
                        flashViewModel = flashViewModel,
                        onHomeButtonClicked = {
                            navController.navigate(FlashAppScreen.Start.name){
                                popUpTo(0)
                            }
                        }
                        )
                }
            }
        }

        if(logoutClicked){
            AlertCheck(onYesButtonPressed = {
                flashViewModel.setLogoutStatus(false)
                auth.signOut()
                flashViewModel.clearAllData()
            }, onNoButtonPressed = {
                flashViewModel.setLogoutStatus(false)
            })
        }

    }

}

@Composable
fun FlashAppBar(navController: NavHostController,
                currentScreen: FlashAppScreen,
                cartItems : List<InternetItem>
                ){
   Row(
       horizontalArrangement = Arrangement.SpaceBetween,
       modifier = Modifier
           .fillMaxWidth()
           .padding(
               horizontal = 70.dp, vertical = 10.dp
           )
   ) {
     Column(
         horizontalAlignment = Alignment.CenterHorizontally,
         verticalArrangement = Arrangement.spacedBy(4.dp),
         modifier = Modifier.clickable {
          navController.navigate(FlashAppScreen.Start.name){
              popUpTo(0)
          }
         }
         ) {
         Icon(imageVector = Icons.Outlined.Home,
             contentDescription = "Home" )
         Text(text = "Home", fontSize = 10.sp)
     }
       Column(
           horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.spacedBy(4.dp),
           modifier = Modifier.clickable {
               if(currentScreen != FlashAppScreen.Cart){
                   navController.navigate(FlashAppScreen.Cart.name){
                       popUpTo(0)
                   }
               }

           }
       ) {
           Box {
               Icon(
                   imageVector = Icons.Outlined.ShoppingCart,
                   contentDescription = "Cart"
               )
               if (cartItems.isNotEmpty())
               Card(
                   modifier = Modifier
                       .align(
                           alignment = Alignment.TopEnd,
                       )
                       .width(14.dp)
                       .height(16.dp),
                   colors = CardDefaults.cardColors(
                       containerColor = Color.Red
                   )


               ) {
                   Text(
                       text = cartItems.size.toString(),
                       fontWeight = FontWeight.ExtraBold,
                       color = Color.White,
                       fontSize = 10.sp,
                       textAlign = TextAlign.Center,
                       modifier = Modifier.padding(
                           horizontal = 4.dp
                       )
                       )
               }
           }
           Text(text = "Cart", fontSize = 10.sp)
       }
   }
}

@Composable
fun AlertCheck(
    onYesButtonPressed : () -> Unit,
    onNoButtonPressed : () -> Unit
){
    AlertDialog(
        title = {
            Text(text = "Logout?", fontWeight = FontWeight.Bold)
        },
        containerColor = Color.White,
        text = {
            Text(text = "Are you sure you want to Logout?")
        },
        onDismissRequest = {
            onNoButtonPressed()
        },
        dismissButton = {
            TextButton(onClick = { onNoButtonPressed() })  {
                Text(text = "No")
            }
        },
        confirmButton = {
            TextButton(onClick = {  onYesButtonPressed() }) {
              Text(text = "Yes")
            }
        })
}