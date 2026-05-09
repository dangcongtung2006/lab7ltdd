package com.example.lab7siu

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

class UpdateCourse : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra("courseID")
        val name = intent.getStringExtra("name")
        val duration = intent.getStringExtra("duration")
        val desc = intent.getStringExtra("desc")

        setContent {
            val context = LocalContext.current
            val courseName = remember { mutableStateOf(name ?: "") }
            val courseDuration = remember { mutableStateOf(duration ?: "") }
            val courseDescription = remember { mutableStateOf(desc ?: "") }
            val db = FirebaseFirestore.getInstance()

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Update Course", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = courseName.value,
                            onValueChange = { courseName.value = it },
                            label = { Text("Course Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = courseDuration.value,
                            onValueChange = { courseDuration.value = it },
                            label = { Text("Duration") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = courseDescription.value,
                            onValueChange = { courseDescription.value = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (TextUtils.isEmpty(courseName.value)) {
                                    Toast.makeText(context, "Please enter course name", Toast.LENGTH_SHORT).show()
                                } else {
                                    val updated = Course(
                                        id,
                                        courseName.value,
                                        courseDuration.value,
                                        courseDescription.value
                                    )

                                    id?.let {
                                        db.collection("Courses").document(it)
                                            .set(updated)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Course Updated!", Toast.LENGTH_SHORT).show()
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Update Course", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                id?.let {
                                    db.collection("Courses").document(it)
                                        .delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Course Deleted!", Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete Course", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
