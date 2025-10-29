package com.aihackathonkarisacikartim.god2

import com.settings.SessionManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import com.aihackathonkarisacikartim.god2.SupabaseManager
import com.aihackathonkarisacikartim.god2.TodoItem
import com.aihackathonkarisacikartim.god2.UserDetails
import com.aihackathonkarisacikartim.god2.BuildConfig

// Supabase configuration from BuildConfig (secure)
private val SUPABASE_URL = if (BuildConfig.SUPABASE_URL.isNotEmpty()) {
    BuildConfig.SUPABASE_URL
} else {
    throw IllegalStateException("Supabase URL is not configured. Please add SUPABASE_URL to local.properties")
}

private val SUPABASE_KEY = if (BuildConfig.SUPABASE_ANON_KEY.isNotEmpty()) {
    BuildConfig.SUPABASE_ANON_KEY
} else {
    throw IllegalStateException("Supabase key is not configured. Please add SUPABASE_ANON_KEY to local.properties")
}

// Supabase istemci bağlantısı (Kaldırıldı, SupabaseManager kullanılacak)
/*
val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_KEY
) {
    install(Postgrest)
    install(Auth)
}
*/

// Model sınıfları (SupabaseManager.kt'ye taşındı)
/*
@Serializable
data class TodoItem(...)

@Serializable
data class User(...)

@Serializable
data class UserDetails(...)
*/

// Veritabanı işlemleri için yardımcı sınıf (SupabaseManager.kt'ye taşındı)
/*
class SupabaseManager {
    // ... tüm içerik silindi ...
}
*/

// Giriş Ekranı
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val supabaseManager = remember { SupabaseManager() }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Giriş Yap",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // E-posta alanı
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta Adresi") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        // Şifre alanı
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        // Hata mesajı
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Giriş butonu
        Button(
            onClick = {
                isLoading = true
                errorMessage = null
                
                coroutineScope.launch {
                    try {
                        val result = supabaseManager.login(email, password)
                        isLoading = false
                        
                        if (result.isSuccess) {
                            onLoginSuccess()
                        } else {
                            // Daha kullanıcı dostu hata mesajları
                            val exception = result.exceptionOrNull()
                            errorMessage = when {
                                exception?.message?.contains("Invalid login credentials") == true -> 
                                    "E-posta adresi veya şifre hatalı"
                                exception?.message?.contains("Email not confirmed") == true ->
                                    "E-posta adresi doğrulanmamış"
                                else -> exception?.message ?: "Giriş başarısız"
                            }
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Bağlantı hatası: ${e.message}"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Giriş Yap")
            }
        }
        
        // Kayıt ol butonu
        TextButton(
            onClick = { onNavigateToRegister() },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Hesabınız yok mu? Kayıt olun")
        }
    }
}

// Kayıt Ekranı
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val supabaseManager = remember { SupabaseManager() }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kayıt Ol",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // E-posta alanı
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta Adresi") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        // Kullanıcı adı alanı
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Kullanıcı adı") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        
        // Şifre alanı
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        // Şifre onay alanı
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Şifreyi Onayla") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = password != confirmPassword && confirmPassword.isNotEmpty()
        )
        
        // Hata mesajı
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Kayıt butonu
        Button(
            onClick = {
                if (password != confirmPassword) {
                    errorMessage = "Şifreler eşleşmiyor"
                    return@Button
                }
                
                if (!email.contains("@")) {
                    errorMessage = "Geçerli bir e-posta adresi giriniz"
                    return@Button
                }
                
                isLoading = true
                errorMessage = null
                
                coroutineScope.launch {
                    try {
                        val result = supabaseManager.register(email, username, password)
                        isLoading = false
                        
                        if (result.isSuccess) {
                            onRegisterSuccess()
                        } else {
                            // Daha kullanıcı dostu hata mesajları
                            val exception = result.exceptionOrNull()
                            errorMessage = when {
                                exception?.message?.contains("already registered") == true -> 
                                    "Bu e-posta adresi zaten kayıtlı"
                                else -> exception?.message ?: "Kayıt başarısız"
                            }
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Bağlantı hatası: ${e.message}"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = !isLoading && email.isNotBlank() && username.isNotBlank() && 
                    password.isNotBlank() && password == confirmPassword
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Kayıt Ol")
            }
        }
        
        // Giriş yap butonu
        TextButton(
            onClick = { onNavigateToLogin() },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Zaten hesabınız var mı? Giriş yapın")
        }
    }
}

// TodoList kompozisyonu (Supabase'e bağlı)
@Composable
fun TodoList() {
    var items by remember { mutableStateOf<List<TodoItem>>(listOf()) }
    var newTodoText by remember { mutableStateOf("") }
    val supabaseManager = remember { SupabaseManager() }
    val coroutineScope = rememberCoroutineScope()
    
    // Todoları yükle
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            items = supabaseManager.getTodos()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Görev Listesi", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Yeni todo ekleme alanı
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTodoText,
                onValueChange = { newTodoText = it },
                label = { Text("Yeni görev ekle") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            
            Button(
                onClick = {
                    if (newTodoText.isBlank()) return@Button
                    
                    coroutineScope.launch {
                        val result = supabaseManager.addTodo(newTodoText)
                        if (result.isSuccess) {
                            withContext(Dispatchers.IO) {
                                items = supabaseManager.getTodos()
                            }
                            newTodoText = ""
                        } else {
                            println("Todo ekleme hatası: ${result.exceptionOrNull()?.message}")
                        }
                    }
                }
            ) {
                Text("Ekle")
            }
        }
        
        // Todo listesi
        LazyColumn {
            items(
                items,
                key = { item -> item.id },
            ) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.is_completed,
                        onCheckedChange = { isChecked ->
                            coroutineScope.launch {
                                val updatedTodo = item.copy(is_completed = isChecked)
                                val result = supabaseManager.updateTodo(updatedTodo)
                                if (result.isSuccess) {
                                    withContext(Dispatchers.IO) {
                                        items = supabaseManager.getTodos()
                                    }
                                } else {
                                    println("Todo güncelleme hatası: ${result.exceptionOrNull()?.message}")
                                }
                            }
                        }
                    )
                    
                    Text(
                        text = item.name,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        style = if (item.is_completed) {
                            MaterialTheme.typography.bodyLarge.copy(
                                color = Color.Gray
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge
                        }
                    )
                    
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val result = supabaseManager.deleteTodo(item.id)
                                if (result.isSuccess) {
                                    withContext(Dispatchers.IO) {
                                        items = supabaseManager.getTodos()
                                    }
                                } else {
                                    println("Todo silme hatası: ${result.exceptionOrNull()?.message}")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Sil"
                        )
                    }
                }
            }
        }
    }
}

// Ana Uygulama Ekranı - AuthState'e göre giriş/kayıt/ana içerik gösterir
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    var authState by remember { mutableStateOf("loading") }
    var currentUser by remember { mutableStateOf<UserInfo?>(null) }
    val supabaseManager = remember { SupabaseManager() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Mevcut kullanıcıyı kontrol et
    LaunchedEffect(Unit) {
        currentUser = supabaseManager.getCurrentUser()
        authState = if (currentUser != null) "authenticated" else "login"
    }
    
    when (authState) {
        "loading" -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        "login" -> {
            LoginScreen(
                onLoginSuccess = {
                    coroutineScope.launch {
                        currentUser = supabaseManager.getCurrentUser()
                        authState = "authenticated"
                    }
                },
                onNavigateToRegister = { authState = "register" }
            )
        }
        "register" -> {
            RegisterScreen(
                onRegisterSuccess = {
                    authState = "login"
                },
                onNavigateToLogin = { authState = "login" }
            )
        }
        "authenticated" -> {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Üst çubuk
                TopAppBar(
                    title = { Text(currentUser?.email ?: "Ana Ekran") },
                    actions = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    // Uygulama context'ini coroutineScope dışında alıyoruz
                                    val sessionManager = SessionManager(context)
                                    
                                    val result = supabaseManager.logout(sessionManager)
                                    if (result.isSuccess) {
                                        currentUser = null
                                        authState = "login"
                                    } else {
                                        println("Çıkış hatası: ${result.exceptionOrNull()?.message}")
                                    }
                                }
                            }
                        ) {
                            Text("Çıkış Yap")
                        }
                    }
                )
                
                // Ana içerik - Todo listesi
                TodoList()
            }
        }
    }
} 