package com.eundmswlji.todolist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

//activity에서 데이터를 관리하게 되면 화면을 돌렸을 때 activity가 파괴되고 다시 창조된다.
//따라서 화면을 돌리기전에 데이터들은 다 사라지게된다.
//그런데 viewModel에서 데이터를 관리하게되면
//생명주기와 관계없이 데이터를 관리 하고 있기 때문에
//생명주기에따른 데이터 소실 등을 신경쓰지 않아도된다.
//결론 : 뷰모델에서 데이터를 관리하고, activity에서는 ui만 관리한다.

class MyViewModel : ViewModel() {
    val liveTodoData = MutableLiveData<List<DocumentSnapshot>>()
    val db = Firebase.firestore

    private val datalist = arrayListOf<QueryDocumentSnapshot>()

    init {
        reload()
    }

    fun reload() {
        datalist.clear()
        // Add a new document with a generated ID
        val userid = FirebaseAuth.getInstance().currentUser?.uid
        db.collection(userid.toString()) //파이어베이스에서 만든 컬렉션 이름기
            //실시간으로 데이터가져오기
            .addSnapshotListener { result, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                liveTodoData.value = result?.documents
//
//                for (document in result!!) {
//                    datalist.add(document)
//                }
//                liveTodoData.value = datalist
            }
    }


    //한번만 가져오기
//            .get() //컬렉션 가져오기
//            .addOnSuccessListener { result ->
//                for (document in result) {
//                    val todo =
//                        Todo(document.data["text"] as String, document.data["isDone"] as Boolean)
//                    datalist.add(todo)
//                }
//                liveTodoData.value = datalist
//            }
//            .addOnFailureListener { e ->
//            }


    fun addTodo(todo: Todo) {

        val userid = FirebaseAuth.getInstance().currentUser?.uid
        db.collection(userid.toString()).add(todo)


//    datalist.add(todo)
//    liveTodoData.value = datalist
    }

    fun deleteTodo(todo: DocumentSnapshot) {
        val userid = FirebaseAuth.getInstance().currentUser?.uid
        db.collection(userid.toString()).document(todo.id).delete()
//    datalist.remove(todo)
//    liveTodoData.value = datalist

    }

    fun doneTodo(todo: DocumentSnapshot) {
        val userid = FirebaseAuth.getInstance().currentUser?.uid
        val isDone = todo.getBoolean("isDone") ?: false
        db.collection(userid.toString()).document(todo.id).update("isDone", !isDone)


//    todo["isDone"] = !todo.getBoolean("isDone")!!
//    liveTodoData.value = datalist

    }
}