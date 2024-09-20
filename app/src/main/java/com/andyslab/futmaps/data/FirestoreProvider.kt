package com.andyslab.futmaps.data

import com.andyslab.futmaps.domain.entities.FutLocation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirestoreProvider {
    val firestore: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    lateinit var futLocations: Set<FutLocation>
}