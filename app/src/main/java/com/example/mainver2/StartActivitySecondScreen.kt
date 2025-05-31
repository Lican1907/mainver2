package com.example.mainver2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class StartActivitySecondScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_second_screen)

        val buttonHomePage = findViewById<ImageButton>(R.id.imageButtonHomePage)
        buttonHomePage.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.howToPlayCard).setOnClickListener { showHowToPlayDialog() }
        findViewById<CardView>(R.id.aboutGameCard).setOnClickListener { showAboutGameDialog() }
        findViewById<CardView>(R.id.privacyRightsCard).setOnClickListener { showPrivacyRightsDialog() }
        findViewById<CardView>(R.id.privacyPreferencesCard).setOnClickListener { showPrivacyPreferencesDialog() }
        findViewById<CardView>(R.id.removeAdsCard).setOnClickListener { showRemoveAdsDialog() }
        findViewById<CardView>(R.id.settingsCard).setOnClickListener { showComingSoonDialog() }
        findViewById<CardView>(R.id.helpCard).setOnClickListener { showComingSoonDialog() }
    }

    private fun showHowToPlayDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.how_to_play_title)
            .setMessage(R.string.how_to_play_message)
            .setPositiveButton(R.string.dialog_positive_button) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showAboutGameDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.about_game_title)
            .setMessage(R.string.about_game_message)
            .setPositiveButton(R.string.dialog_negative_button) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showPrivacyRightsDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.privacy_rights_title)
            .setMessage(R.string.privacy_rights_message)
            .setPositiveButton(R.string.dialog_negative_button) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showPrivacyPreferencesDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.privacy_preferences_title)
            .setMessage(R.string.privacy_preferences_message)
            .setNegativeButton(R.string.dialog_negative_button) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showRemoveAdsDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.remove_ads_title)
            .setMessage(R.string.remove_ads_message)
            .setPositiveButton(R.string.dialog_positive_button) { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://t.me/bricodeofftg")
                }
                startActivity(intent)
            }
            .setNegativeButton(R.string.dialog_later_button) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showComingSoonDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.coming_soon_title)
            .setMessage(R.string.coming_soon_message)
            .setPositiveButton(R.string.dialog_ok_button) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
