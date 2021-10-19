package com.eundmswlji.todolist

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eundmswlji.todolist.databinding.ActivityMainBinding
import com.eundmswlji.todolist.databinding.ItemTodoBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var providers: ArrayList<AuthUI.IdpConfig>
    val model: MyViewModel by viewModels()

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //firebase가 제공하는 이메일로그인 ui 가져오기
        providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        //현재 로그인이 안 되어 있으면 로그인 액티비티로 이동
        if (FirebaseAuth.getInstance().currentUser == null) {
            logIn()
        }

        //4. 뷰모델 만들기

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter =
                TodoAdapter(
                    layoutInflater,
                    emptyList(),
                    onClickDeleteIcon = {
                        model.deleteTodo(it)

                    },
                    onClickDoneIcon = {
                        model.doneTodo(it)

                    })
        }

        binding.btnAdd.setOnClickListener {
            model.addTodo(Todo(binding.editText.text.toString()))
        }

        //관찰
        model.liveTodoData.observe(this, Observer {
            (binding.recyclerView.adapter as TodoAdapter).setData(it)
        })

    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            model.reload()
            // ...
        } else {
            // Sign in failed.
            finish() //로그인 실패 시 todo액티비티 finish
        }
    }

    fun logIn() {
        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener { }
        logIn()
    }

    //활동에 관한 옵션 메뉴를 지정하려면 onCreateOptionsMenu()를 재정의합니다
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menu_logout -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

data class Todo(val text: String, var isDone: Boolean = false)


//1. adapter에서도 binding 사용하기

class TodoAdapter(
    val inflater: LayoutInflater,
    var list: List<DocumentSnapshot>,
    val onClickDeleteIcon: (todo: DocumentSnapshot) -> Unit, //2. 람다
    val onClickDoneIcon: (todo: DocumentSnapshot) -> Unit
) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    inner class TodoViewHolder(val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = inflater.inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(ItemTodoBinding.bind(view))
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.binding.txtTodo.text = list[position].getString("text")

        //작업 완료 일때 글자체 처리
        if (list[position].getBoolean("isDone") == true) {
            holder.binding.txtTodo.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG //3. 삭선넣기 & 이탤릭체
                setTypeface(null, Typeface.ITALIC)
            }
        } else {
            holder.binding.txtTodo.apply {
                paintFlags = 0
                setTypeface(null, Typeface.NORMAL)
            }
        }

        holder.binding.txtTodo.setOnClickListener {
            onClickDoneIcon.invoke(list[position])
        }

        //삭제처리
        holder.binding.imgDelete.setOnClickListener {
            onClickDeleteIcon.invoke(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setData(new: List<DocumentSnapshot>) {
        list = new
        notifyDataSetChanged()
    }
}
