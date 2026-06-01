package com.oliveme.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.oliveme.app.data.repository.AppGraph
import com.oliveme.app.ui.screens.MainScreen
import com.oliveme.app.ui.theme.OliveMeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.init(this)
        val user = currentUser()
        setContent {
            OliveMeTheme {
                MainScreen(
                    user = user,
                    onDiagnosis = { startActivity(diagnosisIntent(user)) },
                    onMap = { startActivity(mapIntent(user)) },
                    onMyPage = { startActivity(myPageIntent(user)) },
                    onResult = { startActivity(resultIntent(user)) },
                    onNotice = { Toast.makeText(this, "새 알림이 없습니다.", Toast.LENGTH_SHORT).show() },
                    onConsult = { Toast.makeText(this, "컬러 상담은 준비 중입니다.", Toast.LENGTH_SHORT).show() },
                    onLogout = {
                        startActivity(android.content.Intent(this, LoginActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }
}
