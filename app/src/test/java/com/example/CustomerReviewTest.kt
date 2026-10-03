package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CustomerReviewEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CustomerReviewTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveCustomerReview() = runBlocking {
        val review = CustomerReviewEntity(
            customerName = "Rahul Verma",
            customerMobile = "9876543210",
            vehicleNumber = "KA01AB1234",
            vehicleModel = "Royal Enfield Classic 350",
            serviceType = "Periodic Service",
            rating = 5,
            aspectPunctuality = 5,
            aspectCleanliness = 5,
            aspectPricing = 5,
            tags = "Transparent Pricing, Genuine Spares",
            comment = "Great workshop in Bangalore. Very transparent.",
            jobCardNumber = "JC-2026-101"
        )

        val id = db.customerReviewDao().insertReview(review)
        assertTrue(id > 0)

        val reviews = db.customerReviewDao().getAllReviews().first()
        assertEquals(1, reviews.size)
        assertEquals("Rahul Verma", reviews[0].customerName)
        assertEquals(5, reviews[0].rating)
        assertEquals("KA01AB1234", reviews[0].vehicleNumber)
    }

    @Test
    fun testAdminResponseToCustomerReview() = runBlocking {
        val review = CustomerReviewEntity(
            customerName = "Anita Rao",
            vehicleNumber = "KA03CD5678",
            serviceType = "Ceramic Coating",
            rating = 4,
            comment = "Good finish on paint, timely delivery."
        )

        val id = db.customerReviewDao().insertReview(review)

        // Admin replies to the review
        db.customerReviewDao().updateAdminResponse(
            id = id,
            response = "Thank you Anita! Glad you liked the ceramic finish."
        )

        val reviews = db.customerReviewDao().getAllReviews().first()
        assertEquals(1, reviews.size)
        assertEquals("Thank you Anita! Glad you liked the ceramic finish.", reviews[0].adminResponse)
        assertTrue(reviews[0].adminRespondedAt > 0L)
    }

    @Test
    fun testDeleteReview() = runBlocking {
        val review = CustomerReviewEntity(
            customerName = "Test Client",
            vehicleNumber = "KA05EF9999",
            rating = 3,
            comment = "Average experience"
        )

        val id = db.customerReviewDao().insertReview(review)
        var reviews = db.customerReviewDao().getAllReviews().first()
        assertEquals(1, reviews.size)

        db.customerReviewDao().deleteReview(id)
        reviews = db.customerReviewDao().getAllReviews().first()
        assertEquals(0, reviews.size)
    }
}
