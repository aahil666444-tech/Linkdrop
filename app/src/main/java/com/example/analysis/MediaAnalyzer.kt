package com.example.analysis

import com.example.providers.AnalysisResult
import com.example.providers.ProviderRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Media Analysis Architecture Layer.
 * Validates links and coordinates analysis through clean provider adapters.
 */
object MediaAnalyzer {

  suspend fun analyzeUrl(url: String): AnalysisResult = withContext(Dispatchers.IO) {
    val clean = url.trim()
    if (clean.isBlank()) {
      return@withContext AnalysisResult(
        isSuccess = false,
        errorMessage = "Please enter a valid media link."
      )
    }

    if (!clean.startsWith("http://", ignoreCase = true) && !clean.startsWith("https://", ignoreCase = true)) {
      return@withContext AnalysisResult(
        isSuccess = false,
        errorMessage = "Invalid URL. Links must start with http:// or https://"
      )
    }

    val provider = ProviderRegistry.getProviderFor(clean)
    provider.analyze(clean)
  }
}
