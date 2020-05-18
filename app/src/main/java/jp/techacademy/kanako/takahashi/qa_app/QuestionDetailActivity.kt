package jp.techacademy.kanako.takahashi.qa_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_question_send.*
import java.util.HashMap

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoritesRef: DatabaseReference  //追加

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    //favoritesRefをリスナーセット
    private val mFavoritesListener = object : ChildEventListener {

    //ChildAddedはすでにデータがある場合にしか呼ばれない
    //からこのメソッドが呼ばれたときは登録済み
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            favoritesbutton.text = "★登録済み"
            favoritesbutton.setBackgroundColor(Color.YELLOW)
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

    }


    override fun onResume() {
        super.onResume()

        val user = FirebaseAuth.getInstance().currentUser

        //お気に入りボタンの表示
        if (user == null) {     //ログインしていない場合
            //お気に入りボタン非表示
            favoritesbutton.visibility = View.GONE

        } else {               //ログインしている場合

            //追加
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            mFavoritesRef = dataBaseReference.child(FavoritesPATH).child(user!!.uid).child(mQuestion.questionUid)
            val data = HashMap<String, String>()

            mFavoritesRef.addChildEventListener(mFavoritesListener)  //追加

            // ボタン表示
            favoritesbutton.visibility = View.VISIBLE;

            //お気に入りボタン押した時
            favoritesbutton.setOnClickListener {
                if (favoritesbutton.text == "☆お気に入り") {
                    //表示を切り替え
                    favoritesbutton.text = "★登録済み"
                    favoritesbutton.setBackgroundColor(Color.YELLOW)

                    //Fiewbaseに登録
                    data["genre"] = mQuestion.genre.toString()
                    mFavoritesRef.setValue(data)

                } else if (favoritesbutton.text == "★登録済み") {
                    //表示切り替え
                    favoritesbutton.text = "☆お気に入り"
                    favoritesbutton.setBackgroundColor(Color.LTGRAY)

                    //登録削除
                    mFavoritesRef.removeValue()
                }
            }

        }

    }

}
