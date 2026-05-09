package com.example.lab7siu

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

class CourseDetailsActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val courseList = remember { mutableStateListOf<Course>() }
            val selectedCourseIds = remember { mutableStateListOf<String>() }
            val db = FirebaseFirestore.getInstance()

            fun fetchCourses() {
                db.collection("Courses")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        courseList.clear()
                        selectedCourseIds.clear()
                        for (doc in querySnapshot) {
                            val course = doc.toObject(Course::class.java)
                            course.courseID = doc.id
                            courseList.add(course)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@CourseDetailsActivity, "Failed to load courses", Toast.LENGTH_SHORT).show()
                    }
            }

            LaunchedEffect(Unit) {
                fetchCourses()
            }

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("All Courses", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                if (courseList.isNotEmpty()) {
                                    IconButton(onClick = {
                                        if (selectedCourseIds.size == courseList.size) {
                                            selectedCourseIds.clear()
                                        } else {
                                            selectedCourseIds.clear()
                                            courseList.forEach { it.courseID?.let { id -> selectedCourseIds.add(id) } }
                                        }
                                    }) {
                                        Icon(Icons.Default.DoneAll, contentDescription = "Select All")
                                    }

                                    if (selectedCourseIds.isNotEmpty()) {
                                        IconButton(onClick = {
                                            val batch = db.batch()
                                            selectedCourseIds.forEach { id ->
                                                val docRef = db.collection("Courses").document(id)
                                                batch.delete(docRef)
                                            }
                                            batch.commit().addOnSuccessListener {
                                                Toast.makeText(this@CourseDetailsActivity, "Selected courses deleted", Toast.LENGTH_SHORT).show()
                                                fetchCourses()
                                            }.addOnFailureListener {
                                                Toast.makeText(this@CourseDetailsActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                                            }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { padding ->
                    if (courseList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No courses found", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(8.dp)
                        ) {
                            items(courseList) { item ->
                                val isSelected = selectedCourseIds.contains(item.courseID)
                                CourseItemWithSelection(
                                    course = item,
                                    isSelected = isSelected,
                                    onToggleSelection = {
                                        item.courseID?.let { id ->
                                            if (isSelected) selectedCourseIds.remove(id) else selectedCourseIds.add(id)
                                        }
                                    },
                                    onItemClick = {
                                        val i = Intent(this@CourseDetailsActivity, UpdateCourse::class.java)
                                        i.putExtra("courseID", item.courseID)
                                        i.putExtra("name", item.courseName)
                                        i.putExtra("duration", item.courseDuration)
                                        i.putExtra("desc", item.courseDescription)
                                        startActivity(i)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseItemWithSelection(
    course: Course,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.courseName ?: "No Name",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Duration: ${course.courseDuration}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.courseDescription ?: "",
                    fontSize = 14.sp,
                    maxLines = 2
                )
            }
        }
    }
}
