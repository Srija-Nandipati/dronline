package com.example.doctoronline.model

data class AppointmentModel(
    val appointmentId: String? = null,
    val screenShotUrl: String? = null,
    val doctorType: String? = null,
    val date: String? = null,
    val time: String? = null,
    val ageGroup: String? = null,
    val amount: String? = null
)