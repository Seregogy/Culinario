package com.culinario.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.culinario.R
import com.culinario.pages.SignInPage
import com.culinario.pages.SignUpPage
import com.culinario.pages.UserCreatePage
import com.culinario.viewmodel.LoginViewModel
import com.culinario.viewmodel.UserCreateViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import kotlinx.coroutines.launch

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun LoginScreen(onLogin: () -> Unit) {
    GoogleAuthProvider.create(credentials = GoogleAuthCredentials(serverId = stringResource(R.string.oauth_id)))

    val loginAndPasswordLoginSelected = remember { mutableStateOf(false) }

    Scaffold { _ ->
        BackHandler {
            if (loginAndPasswordLoginSelected.value)
                loginAndPasswordLoginSelected.value = false
        }

        if (loginAndPasswordLoginSelected.value.not()) {
            MainRegistrationPage(
                loginAndPasswordLoginSelected
            ) {
                onLogin()
            }
        } else {
            LoginAndPasswordRegistrationPage {
                onLogin()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainRegistrationPage(
    selectionState: MutableState<Boolean> = mutableStateOf(false),
    onLogin: () -> Unit = { }
) {
    val loginViewModel = LoginViewModel()

    var createUserPage by remember { mutableStateOf(false) }
    var newUserId by remember { mutableStateOf("") }

    Column (
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (createUserPage) {
            UserCreatePage(
                Modifier,
                UserCreateViewModel(newUserId, LocalContext.current)
            ) {
                onLogin()
            }
        }

        Box (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.loginscreenbg),
                contentDescription = "Splash screen background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .padding(25.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "Вход",
                fontWeight = FontWeight.W700,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )

            GoogleButtonUiContainerFirebase(
                onResult = { result ->
                    if (result.isSuccess) {
                        loginViewModel.onGoogleAuth(Firebase.auth.currentUser!!) { user, isNewUser ->
                            if (isNewUser) {
                                createUserPage = true
                                newUserId = user.uid
                            } else {
                                onLogin()
                            }
                        }
                    }
                },
                linkAccount = false,
                filterByAuthorizedAccounts = false
            ) {
                GoogleSignInButton(
                    onClick = {
                        this.onClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "Войти через Google"
                )
            }

            TextButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    selectionState.value = true
                }
            ) {
                Text(
                    text = "Войти по логину и паролю"
                )
            }
        }

    }
}

@Composable
private fun LoginAndPasswordRegistrationPage(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = 1,
        pageCount = { 2 }
    )

    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxSize()
    ) { page ->
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            when (page) {
                0 -> {
                    SignInPage(
                        modifier = Modifier
                            .padding(top = 150.dp)
                    ) {
                        onLogin()
                    }
                }

                1 -> {
                    SignUpPage(
                        modifier = Modifier
                            .padding(top = 150.dp),
                        onBackToSignIn = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    ) {
                        onLogin()
                    }
                }

                else -> {
                    Text(
                        text = "Presentation page №$page",
                        style = MaterialTheme.typography.displayLarge,
                        modifier = modifier
                    )
                }
            }
        }

    }
}