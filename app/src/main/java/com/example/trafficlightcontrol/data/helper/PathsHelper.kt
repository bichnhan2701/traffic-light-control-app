package com.example.trafficlightcontrol.data.helper

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
/* =========================
 *  Paths helper
 * ========================= */

object Paths {
    fun root(db: FirebaseDatabase, id: String): DatabaseReference =
        db.getReference("/traffic/intersections/$id")

    fun desired(db: FirebaseDatabase, id: String): DatabaseReference =
        root(db, id).child("desired")

    fun reported(db: FirebaseDatabase, id: String): DatabaseReference =
        root(db, id).child("reported")

    fun connEsp(db: FirebaseDatabase, id: String): DatabaseReference =
        root(db, id).child("connection/esp")

    fun infoOffset(db: FirebaseDatabase): DatabaseReference =
        db.getReference(".info/serverTimeOffset")

    fun logs(db: FirebaseDatabase, id: String): DatabaseReference =
        root(db, id).child("logs")
}