package com.example.mainver2

import android.content.Intent
import android.graphics.Color
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
            .setTitle("Как программировать блоками")
            .setMessage("""
                1. Перетаскивайте блоки из палитры в рабочую область
                2. Соединяйте блоки как пазл
                3. Запускайте код
            """.trimIndent())
            .setPositiveButton("Понятно") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showAboutGameDialog() {
        AlertDialog.Builder(this)
            .setTitle("О приложении BlockCoder")
            .setMessage("""
                Версия: 1.0
                
                BlockCoder - это среда для обучения программированию 
                через визуальные блоки.
                
                Особенности:
                - Интуитивный интерфейс
                - Безопасная среда разработки
                - Мгновенный результат
                - Обучение логике и алгоритмам
                
                Разработано с ❤️ для будущих программистов!
            """.trimIndent())
            .setPositiveButton("Закрыть") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showPrivacyRightsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Ваши права")
            .setMessage("""
                Мы серьезно относимся к вашей конфиденциальности:
                
                1. Мы не собираем личные данные
                2. Все данные остаются на устройстве
                3. Нет скрытого отслеживания
                4. Соответствие COPPA (Children's Online Privacy Protection Act)
                
                Полная политика конфиденциальности доступна на нашем сайте.
            """.trimIndent())
            .setPositiveButton("Закрыть") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showPrivacyPreferencesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Конфиденциальность и сбор данных")
            .setMessage("""
            Настройте, какие данные вы разрешаете собирать для улучшения работы приложения:
            
            📊 Аналитика использования
            • Анонимные данные о том, какие функции используются чаще
            • Помогает понять, что нужно улучшить
            • Не содержит личной информации
            
            🎯 Персонализированные подсказки
            • Рекомендации подходящих блоков кода
            • Персональные советы по обучению
            • Анализ вашего прогресса
            
            🔍 Рекомендации проектов
            • Подборка проектов по вашему уровню
            • Персонализированные задания
            • Интересные идеи для творчества
            
            🌐 Данные для разработчиков
            • Анонимные отчеты об ошибках
            • Информация о производительности
            • Помогает исправлять баги быстрее
            
            Все данные:
            • Шифруются при передаче
            • Не содержат личной информации
            • Можно отключить в любой момент
            
            Полная политика конфиденциальности доступна в разделе "Права"
        """.trimIndent())
            .setNegativeButton("Закрыть") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showRemoveAdsDialog() {
        AlertDialog.Builder(this)
            .setTitle("🌟 ПРЕМИУМ ДОСТУП")
            .setMessage("""
            Раскройте полный потенциал приложения:
            
            • 🎨 Эксклюзивные блоки программирования
            • 🚀 Приоритетная техническая поддержка
            • 📡 Расширенные возможности для проектов
            • 🔒 Без рекламы и ограничений
            
            Специальное предложение:
            ⏳ Только сейчас скидка 30%!
            
            Для оформления подписки пишите:
            👉 @bricodeofftg в Telegram
            
            Наши специалисты помогут:
            - Подобрать оптимальный тариф
            - Ответят на все вопросы
            - Оформят доступ моментально
        """.trimIndent())
            .setPositiveButton("✉️ Написать в Telegram") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://t.me/bricodeofftg")
                }
                startActivity(intent)
            }
            .setNegativeButton("Позже") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showComingSoonDialog() {
        AlertDialog.Builder(this)
            .setTitle("Скоро будет доступно")
            .setMessage("Эта функция находится в разработке и появится в следующем обновлении!")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}