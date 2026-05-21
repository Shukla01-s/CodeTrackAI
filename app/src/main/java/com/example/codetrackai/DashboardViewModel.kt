package com.example.codetrackai

import com.example.codetrackai.Task
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
// 🌟 IMPORTS FIXED: In teeno imports ke miss hone se getValue/setValue ka error aa raha tha
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codetrackai.data.AppDatabase
import com.example.codetrackai.data.UserEntity
import com.example.codetrackai.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()
    private val dbFirestore = FirebaseFirestore.getInstance()
    private val localDb = AppDatabase.getDatabase(application)
    private val userDao = localDb.userDao()

    private var firestoreListener: ListenerRegistration? = null

    var userName by mutableStateOf("Loading...")
    var problemsSolved by mutableStateOf(0)
    var streak by mutableStateOf(0)
    var totalLeetCodeSolved by mutableStateOf(0)
    var totalCodeforcesSolved by mutableStateOf(0)

    // Profile Handles storage state variables
    var leetcodeHandle by mutableStateOf("")
    var codeforcesHandle by mutableStateOf("")
    var cfRating by mutableStateOf(0)
    var cfRank by mutableStateOf("Unrated")
    var calculatedRating by mutableStateOf(0)
    var isProfileLoading by mutableStateOf(false)
    var ratingHistory = mutableStateListOf<Float>()

    var syncState by mutableStateOf<SyncState>(SyncState.Idle)
    var aiState by mutableStateOf<AiState>(AiState.Idle)
    var tasks by mutableStateOf(listOf<Task>())
        private set

    var taskInput by mutableStateOf("")

    init {
        loadAndSyncData()
    }

    private fun loadAndSyncData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            userName = "Guest"
            return
        }
        val userId = currentUser.uid

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cachedUser = userDao.getUserProfile(userId).firstOrNull()
                withContext(Dispatchers.Main) {
                    cachedUser?.let {
                        userName = it.name
                        problemsSolved = it.problemsSolved
                        streak = it.streak
                        totalLeetCodeSolved = it.totalLeetCodeSolved
                        totalCodeforcesSolved = it.totalCodeforcesSolved
                        leetcodeHandle = it.leetcodeHandle
                        codeforcesHandle = it.codeforcesHandle
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        firestoreListener = dbFirestore.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null || document == null || !document.exists()) return@addSnapshotListener

                val name = document.getString("name") ?: "User"
                val solved = document.getLong("problemsSolved")?.toInt() ?: 0
                val strk = document.getLong("streak")?.toInt() ?: 0
                val leetcode = document.get("totalLeetCodeSolved")?.toString()?.toIntOrNull() ?: 0
                val codeforces = document.get("totalCodeforcesSolved")?.toString()?.toIntOrNull() ?: 0

                val lcHandle = document.getString("leetcodeHandle") ?: document.getString("leetcode") ?: ""
                val cfHandle = document.getString("codeforcesHandle") ?: document.getString("codeforces") ?: ""

                userName = name
                problemsSolved = solved
                streak = strk
                totalLeetCodeSolved = leetcode
                totalCodeforcesSolved = codeforces
                leetcodeHandle = lcHandle
                codeforcesHandle = cfHandle

                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        userDao.insertOrUpdateUser(
                            UserEntity(
                                uid = userId, name = name, problemsSolved = solved,
                                streak = strk, totalLeetCodeSolved = leetcode, totalCodeforcesSolved = codeforces,
                                leetcodeHandle = lcHandle, codeforcesHandle = cfHandle
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    fun fetchProfileScreenDetails() {
        val lcUser = leetcodeHandle.trim()
        val cfUser = codeforcesHandle.trim()
        if (lcUser.isEmpty() && cfUser.isEmpty()) return

        isProfileLoading = true

        viewModelScope.launch {
            try {
                var totalBasePoints = 0f

                if (lcUser.isNotEmpty()) {
                    val lcResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.leetCodeApi.getLeetCodeStats(lcUser)
                    }
                    val lcSolved = lcResponse.totalSolved ?: 0
                    totalLeetCodeSolved = lcSolved
                    totalBasePoints += (lcSolved * 5)
                }

                if (cfUser.isNotEmpty()) {
                    val cfInfoResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.codeforcesApi.getCodeforcesInfo(cfUser)
                    }
                    if (cfInfoResponse.status == "OK" && !cfInfoResponse.result.isNullOrEmpty()) {
                        val cfInfo = cfInfoResponse.result[0]
                        cfRating = cfInfo.rating ?: 0
                        cfRank = cfInfo.rank ?: "Unrated"
                        totalBasePoints += (cfRating * 0.8f)
                    }

                    val cfStatusRes = withContext(Dispatchers.IO) {
                        RetrofitClient.codeforcesApi.getCodeforcesStatus(cfUser)
                    }
                    if (cfStatusRes.status == "OK" && cfStatusRes.result != null) {
                        totalCodeforcesSolved = cfStatusRes.result.count { it.verdict == "OK" }
                    }
                }

                problemsSolved = totalLeetCodeSolved + totalCodeforcesSolved
                calculatedRating = if (totalBasePoints == 0f) 1000 else totalBasePoints.toInt()

                ratingHistory.clear()
                ratingHistory.addAll(listOf(
                    calculatedRating * 0.85f,
                    calculatedRating * 0.92f,
                    calculatedRating * 0.97f,
                    calculatedRating.toFloat()
                ))

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val updates = mapOf(
                        "totalLeetCodeSolved" to totalLeetCodeSolved,
                        "totalCodeforcesSolved" to totalCodeforcesSolved,
                        "problemsSolved" to problemsSolved
                    )
                    withContext(Dispatchers.IO) {
                        dbFirestore.collection("users").document(currentUser.uid).update(updates)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isProfileLoading = false
            }
        }
    }

    fun syncCodingProfiles(leetcode: String, codeforces: String) {
        val currentUser = auth.currentUser ?: return
        if (leetcode.isEmpty() && codeforces.isEmpty()) return

        syncState = SyncState.Loading

        viewModelScope.launch {
            try {
                var lcCount = totalLeetCodeSolved
                var cfCount = totalCodeforcesSolved

                if (leetcode.isNotEmpty()) {
                    val lcResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.leetCodeApi.getLeetCodeStats(leetcode.trim())
                    }
                    lcCount = lcResponse.totalSolved ?: 0
                }

                if (codeforces.isNotEmpty()) {
                    val cfResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.codeforcesApi.getCodeforcesStatus(codeforces.trim())
                    }
                    if (cfResponse.status == "OK" && cfResponse.result != null) {
                        cfCount = cfResponse.result.count { it.verdict == "OK" }
                    }
                }

                val totalCalculated = lcCount + cfCount
                val updates = mapOf(
                    "totalLeetCodeSolved" to lcCount,
                    "totalCodeforcesSolved" to cfCount,
                    "problemsSolved" to totalCalculated
                )

                withContext(Dispatchers.IO) {
                    dbFirestore.collection("users").document(currentUser.uid).update(updates)
                }
                syncState = SyncState.Success(lcCount, cfCount)
            } catch (e: Exception) {
                syncState = SyncState.Error(e.localizedMessage ?: "Syncing Profiles Failed")
            }
        }
    }

    fun saveDataToFirebase() {
        repo.saveUserData(userName, problemsSolved, totalLeetCodeSolved, totalCodeforcesSolved, tasks)
    }

    fun loadDataFromFirebase() {
        repo.getUserData { data ->
            data?.let {
                userName = (it["name"] as? String) ?: "User"
                problemsSolved = ((it["problemsSolved"] as? Long) ?: 0L).toInt()
                totalLeetCodeSolved = ((it["totalLeetCodeSolved"] as? Long) ?: 0L).toInt()
                totalCodeforcesSolved = ((it["totalCodeforcesSolved"] as? Long) ?: 0L).toInt()

                val taskList = it["tasks"] as? List<Map<String, Any>>
                val newList = mutableListOf<Task>()
                taskList?.forEach { map ->
                    val title = map["title"] as? String ?: ""
                    val isCompleted = map["isCompleted"] as? Boolean ?: false
                    newList.add(Task(title, isCompleted))
                }
                tasks = newList
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        firestoreListener?.remove()
        auth.signOut()
        onLogoutSuccess()
    }

    fun addTask() {
        if (taskInput.isNotBlank()) {
            val updatedList = tasks.toMutableList()
            updatedList.add(Task(taskInput))
            tasks = updatedList
            taskInput = ""
        }
    }

    fun toggleTask(index: Int) {
        if (index < 0 || index >= tasks.size) return
        val updatedList = tasks.toMutableList()
        val task = updatedList[index]
        updatedList[index] = task.copy(isCompleted = !task.isCompleted)
        tasks = updatedList
    }

    fun getTaskProgress(): Float {
        if (tasks.isEmpty()) return 0f
        val completed = tasks.count { it.isCompleted }
        return completed.toFloat() / tasks.size
    }

    fun askGeminiAssistant(userQuery: String, apiKey: String) {
        if (userQuery.isEmpty()) return
        viewModelScope.launch {
            aiState = AiState.Loading
            try {
                val responseText = withContext(Dispatchers.IO) {
                    val url = java.net.URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true

                    val systemContext = "You are CodeTrackAI assistant, a highly capable software engineering and computer science expert. Answer the user's question directly, cleanly, and accurately based on their query.\\n\\nUser Question: "
                    val fullPrompt = systemContext + userQuery

                    val escapedPrompt = fullPrompt
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t")

                    val jsonRequestBody = """
                        {
                          "contents": [{
                            "parts":[{"text": "$escapedPrompt"}]
                          }]
                        }
                    """.trimIndent()

                    conn.outputStream.use { os ->
                        val input = jsonRequestBody.toByteArray(charset("utf-8"))
                        os.write(input, 0, input.size)
                    }

                    if (conn.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                        val rawResponse = conn.inputStream.bufferedReader().use { it.readText() }
                        val keyword = "\"text\":"

                        if (rawResponse.contains(keyword)) {
                            val parts = rawResponse.split("\"text\":")
                            if (parts.size > 1) {
                                val textSegment = parts[1].trim()
                                val firstQuote = textSegment.indexOf("\"")
                                val lastQuote = textSegment.indexOf("\"", firstQuote + 1)

                                if (firstQuote != -1 && lastQuote != -1) {
                                    var rawPayload = textSegment.substring(firstQuote + 1, lastQuote)
                                    rawPayload = rawPayload
                                        .replace("\\u003c", "<").replace("\\u003e", ">")
                                        .replace("\\n", "\n").replace("\\t", "\t")
                                        .replace("\\\"", "\"").replace("\\\'", "'")
                                        .replace("\\\\", "\\").replace("&lt;", "<")
                                        .replace("&gt;", ">")
                                    rawPayload
                                } else "Response Parsing Failed"
                            } else "Response Layout Changed"
                        } else "No text block in AI response"
                    } else "API Error Code: ${conn.responseCode}"
                }
                aiState = AiState.Success(responseText)
            } catch (e: Exception) {
                aiState = AiState.Error(e.localizedMessage ?: "Network Error")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        firestoreListener?.remove()
    }
}

// 🌟 CLASSES FIXED: Ye dono classes file ke bottom par define rehni zaroori hain
sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()
    data class Success(val response: String) : AiState()
    data class Error(val error: String) : AiState()
}

sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val leetCodeSolved: Int, val codeforcesSolved: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}