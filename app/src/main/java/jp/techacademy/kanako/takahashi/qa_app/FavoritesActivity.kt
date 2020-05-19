package jp.techacademy.kanako.takahashi.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar

class FavoritesActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

       // mToolbar.title = "お気に入り"

    }
}
