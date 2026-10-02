package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.database.AppDatabase
import com.example.database.DownloadHistoryEntity
import com.example.providers.ProviderRegistry
import com.example.settings.SettingsManager
import com.example.storage.StorageHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var db: AppDatabase

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("LinkDrop", appName)
  }

  @Test
  fun `storage helper format bytes and sanitize filename`() {
    assertEquals("0 B", StorageHelper.formatBytes(0))
    assertEquals("1 KB", StorageHelper.formatBytes(1024))
    assertEquals("1 MB", StorageHelper.formatBytes(1024 * 1024))
    assertEquals("42.6 MB", StorageHelper.formatBytes((42.6 * 1024 * 1024).toLong()))

    val cleanName = StorageHelper.sanitizeFilename("Test/Video:Name?*123.mp4", "mp4")
    assertFalse(cleanName.contains("/"))
    assertFalse(cleanName.contains(":"))
    assertFalse(cleanName.contains("?"))
    assertTrue(cleanName.endsWith(".mp4"))
  }

  @Test
  fun `room database insert retrieve and delete history`() = runBlocking {
    val dao = db.downloadHistoryDao()

    val entity1 = DownloadHistoryEntity(
      id = "test_1",
      filename = "Sample_Video.mp4",
      sourceUrl = "https://example.com/video.mp4",
      platform = "Other",
      mediaType = "VIDEO",
      quality = "1080p",
      fileSize = 44000000L,
      formattedSize = "44 MB",
      downloadDate = 1000L,
      localUriString = "content://media/external/video/media/1"
    )

    val entity2 = DownloadHistoryEntity(
      id = "test_2",
      filename = "Sample_Photo.jpg",
      sourceUrl = "https://example.com/photo.jpg",
      platform = "Instagram",
      mediaType = "IMAGE",
      quality = "Original",
      fileSize = 3500000L,
      formattedSize = "3.5 MB",
      downloadDate = 2000L,
      localUriString = "content://media/external/images/media/2"
    )

    dao.insert(entity1)
    dao.insert(entity2)

    val list = dao.getAllHistory().first()
    assertEquals(2, list.size)
    // Newest first
    assertEquals("test_2", list[0].id)
    assertEquals("test_1", list[1].id)

    // Delete single item
    dao.deleteById("test_1")
    val afterDelete = dao.getAllHistory().first()
    assertEquals(1, afterDelete.size)
    assertEquals("test_2", afterDelete[0].id)

    // Clear all
    dao.clearAll()
    val afterClear = dao.getAllHistory().first()
    assertTrue(afterClear.isEmpty())
  }

  @Test
  fun `provider registry invalid URL validation`() = runBlocking {
    val emptyResult = ProviderRegistry.analyzeUrl("")
    assertFalse(emptyResult.isSuccess)
    assertNotNull(emptyResult.errorMessage)

    val invalidScheme = ProviderRegistry.analyzeUrl("ftp://not.supported/file.mp4")
    assertFalse(invalidScheme.isSuccess)
    assertTrue(invalidScheme.errorMessage!!.contains("http://") || invalidScheme.errorMessage!!.contains("https://"))
  }

  @Test
  fun `settings manager persistence`() {
    val settingsManager = SettingsManager(context)
    assertEquals("1080p Full HD", settingsManager.settings.value.defaultVideoQuality)
    assertFalse(settingsManager.settings.value.wifiOnly)

    settingsManager.updateWifiOnly(true)
    assertTrue(settingsManager.settings.value.wifiOnly)

    settingsManager.updateDefaultVideoQuality("4K Ultra HD (2160p)")
    assertEquals("4K Ultra HD (2160p)", settingsManager.settings.value.defaultVideoQuality)
  }
}
