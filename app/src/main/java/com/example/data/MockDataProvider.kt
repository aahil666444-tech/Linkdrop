package com.example.data

import com.example.model.ActiveDownloadTask
import com.example.model.BatchItem
import com.example.model.BatchStatus
import com.example.model.MediaType
import com.example.model.PlatformType
import com.example.model.RecentDownloadItem

object MockDataProvider {

  // Clean empty defaults: real history and downloads come only from real operations
  val initialRecentDownloads = emptyList<RecentDownloadItem>()
  val fullHistoryItems = emptyList<RecentDownloadItem>()
  val sampleActiveDownloads = emptyList<ActiveDownloadTask>()

  // Direct public downloadable items for batch testing
  val sampleBatchItems = listOf(
    BatchItem(
      id = "b_1",
      url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
      title = "ForBiggerBlazes.mp4",
      platform = PlatformType.OTHER,
      mediaType = MediaType.VIDEO,
      status = BatchStatus.READY,
      estimatedSize = "15.0 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80"
    ),
    BatchItem(
      id = "b_2",
      url = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1280&q=80",
      title = "Alpine_Nature.jpg",
      platform = PlatformType.OTHER,
      mediaType = MediaType.IMAGE,
      status = BatchStatus.READY,
      estimatedSize = "1.8 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80"
    ),
    BatchItem(
      id = "b_3",
      url = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=1280&q=80",
      title = "Cute_Cat.jpg",
      platform = PlatformType.OTHER,
      mediaType = MediaType.IMAGE,
      status = BatchStatus.READY,
      estimatedSize = "2.1 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600&auto=format&fit=crop&q=80"
    ),
    BatchItem(
      id = "b_4",
      url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
      title = "BigBuckBunny.mp4",
      platform = PlatformType.OTHER,
      mediaType = MediaType.VIDEO,
      status = BatchStatus.READY,
      estimatedSize = "158.0 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=600&auto=format&fit=crop&q=80"
    )
  )
}
