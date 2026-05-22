package com.example.codetrackai

import com.example.codetrackai.Task
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
// 🌟 IMPORTS FIXED: In teeno imports ke miss hone se getValue/setValue ka error aa raha था
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    // 🌟 STREAK ADD-ON: LeetCode specific streak variables bina purane features ko alter kiye
    var leetcodeStreak by mutableStateOf(0)
    var lastLeetCodeSolvedDate by mutableStateOf("")

    // 🌟 TOPIC INSIGHTS ADD-ON: AI dynamic analysis categories save karne ke liye states
    var strongTopics by mutableStateOf(listOf<String>())
    var weakTopics by mutableStateOf(listOf<String>())

    // 🌟 APP DYNAMIC STREAK MATRIX ADD-ON: CodeTrackAI specific tracking parameters
    var appStreakCount by mutableStateOf(0)
    var activeDatesList by mutableStateOf<List<String>>(emptyList())

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

                // 🌟 NAYA FEATURE SYNC: Firestore se direct local states populate karo bina baki data structure ko chede
                val lcStreak = document.getLong("leetcodeStreak")?.toInt() ?: 0
                val lcDate = document.getString("lastLeetCodeSolvedDate") ?: ""

                // Firestore se arrays pull karne ka safe backup (bina break kiye)
                val strongList = document.get("strongTopics") as? List<*>
                val weakList = document.get("weakTopics") as? List<*>

                // App Matrix integration synchronization data
                val appStrk = document.getLong("appStreakCount")?.toInt() ?: 0
                val appDatesRaw = document.get("activeDatesList") as? List<*>

                userName = name
                problemsSolved = solved
                streak = strk
                totalLeetCodeSolved = leetcode
                totalCodeforcesSolved = codeforces
                leetcodeHandle = lcHandle
                codeforcesHandle = cfHandle

                // Nayi values assignment
                leetcodeStreak = lcStreak
                lastLeetCodeSolvedDate = lcDate

                strongList?.let { strongTopics = it.map { item -> item.toString() } }
                weakList?.let { weakTopics = it.map { item -> item.toString() } }

                // App Streak mapping safely
                appStreakCount = appStrk
                appDatesRaw?.let { activeDatesList = it.map { item -> item.toString() } }

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
                    val updates = mutableMapOf<String, Any>(
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

    // Sirf LeetCode screen ki alag isolated streak calculate karne ke liye
    fun syncLeetCodeStreakOnly() {
        val currentUser = auth.currentUser ?: return
        val lcUser = leetcodeHandle.trim()
        if (lcUser.isEmpty()) return

        viewModelScope.launch {
            try {
                val lcResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.leetCodeApi.getLeetCodeStats(lcUser)
                }
                val newLcSolved = lcResponse.totalSolved ?: 0

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = dateFormat.format(Calendar.getInstance().time)

                var finalStreak = leetcodeStreak
                var finalDate = lastLeetCodeSolvedDate

                if (newLcSolved > totalLeetCodeSolved) {
                    if (lastLeetCodeSolvedDate != todayStr) {
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        val yesterdayStr = dateFormat.format(calendar.time)

                        finalStreak = if (lastLeetCodeSolvedDate == yesterdayStr) {
                            leetcodeStreak + 1
                        } else {
                            1
                        }
                        finalDate = todayStr
                    }
                } else {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    val yesterdayStr = dateFormat.format(calendar.time)

                    if (lastLeetCodeSolvedDate != todayStr && lastLeetCodeSolvedDate != yesterdayStr) {
                        finalStreak = 0
                    }
                }

                totalLeetCodeSolved = newLcSolved
                leetcodeStreak = finalStreak
                lastLeetCodeSolvedDate = finalDate

                val updates = mapOf(
                    "totalLeetCodeSolved" to totalLeetCodeSolved,
                    "leetcodeStreak" to leetcodeStreak,
                    "lastLeetCodeSolvedDate" to lastLeetCodeSolvedDate,
                    "problemsSolved" to (totalLeetCodeSolved + totalCodeforcesSolved)
                )

                withContext(Dispatchers.IO) {
                    dbFirestore.collection("users").document(currentUser.uid).update(updates)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 🌟 NAYA GEMINI BACKEND ACTION: Isolated background request dynamic response parse karne ke liye
    fun fetchAiTopicInsights(apiKey: String) {
        if (leetcodeHandle.isEmpty() && codeforcesHandle.isEmpty()) return
        if (apiKey.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val systemPrompt = "You are a competitive programming analyzer. Analyze this profile: LeetCode Solved = $totalLeetCodeSolved, Codeforces Solved = $totalCodeforcesSolved, Codeforces Rating = $cfRating. Output exactly two lines of plain text, no bullet points, no markdown bolding. Line 1: 3 strong topics comma separated. Line 2: 3 weak topics comma separated. Example format:\\nArrays,Greedy,Math\\nGraphs,DP,Trees"

                val jsonRequestBody = """
                    {
                      "contents": [{
                        "parts":[{"text": "$systemPrompt"}]
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
                                val cleanText = textSegment.substring(firstQuote + 1, lastQuote)
                                    .replace("\\n", "\n")
                                    .replace("\\\"", "\"")

                                if (cleanText.isNotBlank() && cleanText.contains("\n")) {
                                    val lines = cleanText.lines().map { it.trim() }.filter { it.isNotEmpty() }
                                    if (lines.size >= 2) {
                                        withContext(Dispatchers.Main) {
                                            strongTopics = lines[0].split(",").map { it.trim() }
                                            weakTopics = lines[1].split(",").map { it.trim() }
                                        }

                                        // Backup to Firestore
                                        val currentUser = auth.currentUser
                                        if (currentUser != null) {
                                            dbFirestore.collection("users").document(currentUser.uid).update(
                                                mapOf("strongTopics" to strongTopics, "weakTopics" to weakTopics)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveDataToFirebase() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

            if (!activeDatesList.contains(todayStr)) {
                val updatedDatesList = activeDatesList.toMutableList().apply { add(todayStr) }
                activeDatesList = updatedDatesList

                // 🌟 FIX 1: Yesterday's date generation logic simplified and type-fixed
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

                val lastDate = activeDatesList.getOrNull(activeDatesList.size - 2)

                // 🌟 FIX 2: String comparison corrected (Comparing String with String)
                if (lastDate == yesterdayStr) {
                    appStreakCount += 1
                } else if (lastDate != todayStr) {
                    appStreakCount = 1
                }
            }

            dbFirestore.collection("users").document(currentUser.uid).update(
                mapOf(
                    "appStreakCount" to appStreakCount,
                    "activeDatesList" to activeDatesList
                )
            )
        }
        repo.saveUserData(userName, problemsSolved, totalLeetCodeSolved, totalCodeforcesSolved, tasks)
    }

    fun loadDataFromFirebase() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

            if (!activeDatesList.contains(todayStr)) {
                val updatedDatesList = activeDatesList.toMutableList().apply { add(todayStr) }
                activeDatesList = updatedDatesList

                // 🌟 FIX 1: Yesterday's date generation logic simplified and type-fixed
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

                val lastDate = activeDatesList.getOrNull(activeDatesList.size - 2)

                // 🌟 FIX 2: String comparison corrected
                if (lastDate == yesterdayStr) {
                    appStreakCount += 1
                } else if (lastDate != todayStr) {
                    appStreakCount = 1
                }

                dbFirestore.collection("users").document(currentUser.uid).update(
                    mapOf(
                        "appStreakCount" to appStreakCount,
                        "activeDatesList" to activeDatesList
                    )
                )
            }
        }

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
    // 🌟 NAYA FEATURE: Delete Task Function
    fun deleteTask(index: Int) {
        // Safety Check: Pehle confirm karo ki index list ki boundary ke andar hai
        if (index in tasks.indices) {
            val updatedList = tasks.toMutableList()
            updatedList.removeAt(index)
            tasks = updatedList
            // Firebase ko updated list turant sync karo taaki server se bhi delete ho jaye
            saveDataToFirebase()
        }
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